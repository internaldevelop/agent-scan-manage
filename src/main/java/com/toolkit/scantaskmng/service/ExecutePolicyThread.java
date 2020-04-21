package com.toolkit.scantaskmng.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.toolkit.scantaskmng.bean.dto.TaskRunStatusDto;
import com.toolkit.scantaskmng.bean.po.PolicyPo;
import com.toolkit.scantaskmng.bean.po.TaskExecuteActionPo;
import com.toolkit.scantaskmng.bean.po.TaskExecuteResultsPo;
import com.toolkit.scantaskmng.bean.po.TaskPo;
import com.toolkit.scantaskmng.dao.mybatis.PoliciesMapper;
import com.toolkit.scantaskmng.dao.mybatis.TaskExecActionsMapper;
import com.toolkit.scantaskmng.dao.mybatis.TaskExecuteResultsMapper;
import com.toolkit.scantaskmng.dao.mybatis.TasksMapper;
import com.toolkit.scantaskmng.global.enumeration.*;
import com.toolkit.scantaskmng.global.response.ResponseHelper;
import com.toolkit.scantaskmng.global.utils.MyFileUtils;
import com.toolkit.scantaskmng.global.utils.MyUtils;
import com.toolkit.scantaskmng.global.utils.SpringBeanUtil;
import com.toolkit.scantaskmng.global.utils.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// 使用 @Component 注解，组件化，以使 batchExecutePolicy 函数在不同线程间同步
// 避免动态新建 ExecutePolicyThread 对象时，不同对象无法简单用 synchronized 做到同步
@Component
public class ExecutePolicyThread implements Runnable{
    private String projectUuid;
    private String taskUuid;
    private String userUuid;
    private String executeUuid;
    private JSONArray policyArray;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TaskRunStatusService taskRunStatusService;
    @Autowired
    private TaskExecuteResultsMapper taskExecuteResultsMapper;
    @Autowired
    private TasksMapper tasksMapper;
    @Autowired
    private PoliciesMapper policiesMapper;
    @Autowired
    private TaskExecActionsMapper execActionsMapper;

    public ExecutePolicyThread() {
    }

    @Override
    public void run() {
        try {
            // 批处理执行策略
            ErrorCodeEnum errorCode = batchExecutePolicy();
            if (errorCode != ErrorCodeEnum.ERROR_OK && errorCode != ErrorCodeEnum.ERROR_FAIL_EXEC_POLICY) {
                // set the task status to interrupted if any errors.
                TaskRunStatusDto taskRunStatusDto = taskRunStatusService.getTaskRunStatus(this.taskUuid, this.projectUuid);
                taskRunStatusDto.setRun_status(TaskRunStatusEnum.INTERRUPTED.getStatus());
                taskRunStatusService.setTaskRunStatus(taskRunStatusDto);
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
        }
    }

    private ErrorCodeEnum analyzeTask() {
        // 获取任务执行状态
        TaskRunStatusDto taskRunStatusDto = taskRunStatusService.getTaskRunStatus(this.taskUuid, this.projectUuid);
        if (taskRunStatusDto == null)
            return ErrorCodeEnum.ERROR_TASK_RUN_STATUS_NOT_FOUND;

        // 获取指定的任务
        TaskPo taskPo = tasksMapper.getTaskByUuid(this.taskUuid);
        if (taskPo == null)
            return ErrorCodeEnum.ERROR_TASK_NOT_FOUND;

        // 提取策略组
        JSONArray policyGroups = JSONArray.parseArray(taskPo.getPolicy_groups());
        if (policyGroups == null)
            return ErrorCodeEnum.ERROR_GROUP_NOT_FOUND;

        // 构造策略集合
        JSONArray policyArray = new JSONArray();
        boolean isWindows = SystemUtils.isWindows();
        for ( Iterator iter = policyGroups.iterator(); iter.hasNext(); ) {
            JSONObject jsonGroup = (JSONObject)iter.next();
            // 策略组需和操作系统匹配，本版本暂时用策略组的base_line来代表OS类型
            if ((jsonGroup.getIntValue("baseline") == BaseLineEnum.WINDOWS.getType() && !isWindows) ||
                    (jsonGroup.getIntValue("baseline") == BaseLineEnum.LINUX.getType() && isWindows) )
                continue;

            String policyGroupUuid = jsonGroup.getString("uuid");
            List<PolicyPo> policyPoList = policiesMapper.getPoliciesByGroup(policyGroupUuid);
            if (policyPoList == null || policyPoList.size() == 0) {
                // 如果找不到该组的策略，继续寻找下一个组
                continue;
//                return responseHelper.error(ErrorCodeEnum.ERROR_POLICY_NOT_FOUND, policyGroups);
            }

            // 找到的策略加到该分组对象中
            policyArray.addAll(policyPoList);
        }

        this.policyArray = policyArray;

        return ErrorCodeEnum.ERROR_OK;
    }

    private ErrorCodeEnum createExecuteRecord() {
        this.executeUuid = MyUtils.generateUuid();
        TaskExecuteActionPo executeActionPo = new TaskExecuteActionPo();
        executeActionPo.setUuid(this.executeUuid);
        executeActionPo.setProject_uuid(this.projectUuid);
        executeActionPo.setTask_uuid(this.taskUuid);
        executeActionPo.setUser_uuid(this.userUuid);
        executeActionPo.setStatus(GeneralStatusEnum.VALID.getStatus());
        executeActionPo.setExec_time(MyUtils.getCurrentSystemTimestamp());
        int count = execActionsMapper.addTaskExecAction(executeActionPo);
        if (count == 1)
            return ErrorCodeEnum.ERROR_OK;
        else
            return ErrorCodeEnum.ERROR_INTERNAL_ERROR;
    }

    private ErrorCodeEnum saveTaskExecuteStartStatus() {
        TaskRunStatusDto taskRunStatusDto = taskRunStatusService.getTaskRunStatus(this.taskUuid, this.projectUuid);
        if (taskRunStatusDto == null) {
            // 如果找不到该任务的运行信息，则需新建此任务的运行信息
            taskRunStatusDto = new TaskRunStatusDto();
            taskRunStatusDto.setTask_uuid(this.taskUuid);
        }
        taskRunStatusDto.setExecute_uuid(this.executeUuid);
        taskRunStatusDto.setProject_uuid(this.projectUuid);
        taskRunStatusDto.setRun_status(TaskRunStatusEnum.RUNNING.getStatus());
        taskRunStatusDto.setDone_jobs_count(0);
        taskRunStatusDto.setTotal_jobs_count(this.policyArray.size());
        int totalTime = 0;
        for (Iterator iter = this.policyArray.iterator(); iter.hasNext(); ) {
            PolicyPo policyPo = (PolicyPo) iter.next();
            totalTime += policyPo.getConsume_time();
        }
        taskRunStatusDto.setRemain_time(totalTime);
        taskRunStatusDto.setTotal_time(totalTime);
        taskRunStatusDto.setDone_rate(0.0);
        if (!taskRunStatusService.setTaskRunStatus(taskRunStatusDto))
            return ErrorCodeEnum.ERROR_INTERNAL_ERROR;

        return ErrorCodeEnum.ERROR_OK;
    }

    /**
     * synchronized 在不同线程间同步本处理，让每个任务顺序执行
     * 避免 python 脚本执行时运行时环境冲突的问题
     * @return
     */
    synchronized public ErrorCodeEnum batchExecutePolicy() {
        // 打印线程信息，检查锁的效果
        Thread selfThread = Thread.currentThread();
        logger.info("===> 运行策略执行的线程：" + selfThread.getName());

        // 从队列中取任务
        JSONObject taskInfo = TaskRunQueue.fetch();
        if (taskInfo == null) {
            return ErrorCodeEnum.ERROR_TASK_NOT_FOUND;
        }
        this.projectUuid = taskInfo.getString("project_uuid");
        this.taskUuid = taskInfo.getString("task_uuid");
        this.userUuid = taskInfo.getString("user_uuid");
        logger.info("project UUID: " + this.projectUuid + "\ttask UUID: " + this.taskUuid + "\tuser UUID: " + this.userUuid);
        // 解析任务，包含哪些策略
        ErrorCodeEnum errorCode = analyzeTask();
        if (errorCode != ErrorCodeEnum.ERROR_OK)
            return errorCode;

        // 创建一条任务的执行记录
        errorCode = createExecuteRecord();
        if (errorCode != ErrorCodeEnum.ERROR_OK)
            return errorCode;

        // 设置任务运行状态为运行中
        errorCode = saveTaskExecuteStartStatus();
        if (errorCode != ErrorCodeEnum.ERROR_OK)
            return errorCode;

        // 枚举每个策略
        TaskRunStatusDto taskRunStatusDto = taskRunStatusService.getTaskRunStatus(this.taskUuid, this.projectUuid);
        for (Iterator iter = this.policyArray.iterator(); iter.hasNext(); ) {
            PolicyPo policyPo = (PolicyPo) iter.next();

            // 执行策略，如果执行失败，则中断返回
            if (executePolicy(this.taskUuid, policyPo) != ErrorCodeEnum.ERROR_OK) {
                taskRunStatusDto.setFail_policy_group_uuid(policyPo.getGroup_uuid());
                taskRunStatusDto.setFail_policy_uuid(policyPo.getUuid());
                taskRunStatusDto.setFail_policy_name(policyPo.getName());
                taskRunStatusDto.setRun_status(TaskRunStatusEnum.INTERRUPTED.getStatus());
                taskRunStatusService.setTaskRunStatus(taskRunStatusDto);
                return ErrorCodeEnum.ERROR_FAIL_EXEC_POLICY;
            }

            // 记录一个子任务完成后的任务执行状态
            taskRunStatusDto.setRemain_time(taskRunStatusDto.getRemain_time() - policyPo.getConsume_time());
            taskRunStatusDto.setDone_jobs_count(taskRunStatusDto.getDone_jobs_count() + 1);
            // 百分比取到小数点后一位
            double doneRate = 1000 * (taskRunStatusDto.getTotal_time() - taskRunStatusDto.getRemain_time()) /
                    taskRunStatusDto.getTotal_time() / 10.0;
            taskRunStatusDto.setDone_rate(doneRate);
            taskRunStatusService.setTaskRunStatus(taskRunStatusDto);
        }

        // 记录任务全部完成后的任务执行状态
        taskRunStatusDto.setDone_jobs_count(taskRunStatusDto.getTotal_jobs_count());
        taskRunStatusDto.setRemain_time(0);
        taskRunStatusDto.setDone_rate(100);
        taskRunStatusDto.setRun_status(TaskRunStatusEnum.FINISHED.getStatus());
        taskRunStatusService.setTaskRunStatus(taskRunStatusDto);

        return ErrorCodeEnum.ERROR_OK;
    }

    private String initPolicyExecRecord(String taskUuid, PolicyPo policyPo) {

        TaskExecuteResultsPo resultPo = new TaskExecuteResultsPo();

        String resultUuid = MyUtils.generateUuid();
        resultPo.setUuid(resultUuid);

        resultPo.setExec_action_uuid(this.executeUuid);
        resultPo.setTask_uuid(taskUuid);
        resultPo.setPolicy_uuid(policyPo.getUuid());

        java.sql.Timestamp currentTime = MyUtils.getCurrentSystemTimestamp();
        resultPo.setStart_time(currentTime);
        resultPo.setCreate_time(currentTime);
        resultPo.setProcess_flag(TaskRunStatusEnum.RUNNING.getStatus());
        int rv = taskExecuteResultsMapper.addExecuteRecord(resultPo);
        if (rv < 1)
            return "";

        return resultUuid;
    }

    private ErrorCodeEnum saveResult(String resultUuid, String results) {
//        MyFileUtils.save(result,"temp.txt", false);
        TaskExecuteResultsPo resultPo = new TaskExecuteResultsPo();

        JSONObject jsonResult = JSONObject.parseObject(results);
        int riskLevel = jsonResult.getIntValue("risk_level");
        String riskDesc = jsonResult.getString("risk_desc");
        String solution = jsonResult.getString("solution");
        String info = jsonResult.getString("info");
        if (info == null) info = "";

        resultPo.setUuid(resultUuid);
        resultPo.setRisk_level(riskLevel);
        resultPo.setRisk_desc(riskDesc);
        resultPo.setSolutions(solution);
        resultPo.setProcess_flag(TaskRunStatusEnum.FINISHED.getStatus());
        resultPo.setEnd_time(MyUtils.getCurrentSystemTimestamp());
        resultPo.setResults(info);
        int rv = taskExecuteResultsMapper.updateExecResult(resultPo);
        if (rv < 1)
            return ErrorCodeEnum.ERROR_INTERNAL_ERROR;

        return ErrorCodeEnum.ERROR_OK;
    }

    public ErrorCodeEnum executePolicy(String taskUuid, PolicyPo policyPo) {
        // 启动任务前创建记录
        String resultUuid = initPolicyExecRecord(taskUuid, policyPo);
        if (resultUuid.length() == 0) {
            return ErrorCodeEnum.ERROR_INTERNAL_ERROR;
        }

        ErrorCodeEnum errorCode;
        if (policyPo.getRun_mode() == PolicyRunModeEnum.PYTHON_SCRIPTS.getMode()) {
            // 执行 python 脚本
            errorCode = execPython(resultUuid, policyPo.getRun_contents());
            if (errorCode != ErrorCodeEnum.ERROR_OK)
                return errorCode;
        } else if (policyPo.getRun_mode() == PolicyRunModeEnum.COMMAND.getMode()) {
            // 执行系统命令或工具命令
            // 本版本不支持
        }

        return ErrorCodeEnum.ERROR_OK;
    }

    private ErrorCodeEnum execPython(String resultUuid, String pyScript) {
        // 保存 python 内容为临时文件
        MyFileUtils.save(pyScript, "tempexec.py");

        // Build the script execute command line
        boolean isWindows = SystemUtils.isWindows();
        String pythonCmd = isWindows ? "python" : "python3";
        String[] args1 = new String[]{ pythonCmd, "tempexec.py" };

        try {
            Process proc = Runtime.getRuntime().exec(args1);
            BufferedReader output = MyUtils.getProcReader(proc);
            String line;
            String results = "";
            boolean bValidInfo = false;
            while ((line = output.readLine()) != null) {
                // 运行进程输出信息中包含了命令自身的输出，需略过
                if (!bValidInfo && line.length() > 8 && line.substring(0, 8).equals("{'info':"))
                    bValidInfo = true;
                if (!bValidInfo)
                    continue;

                results += line + "\n";
            }
            output.close();

            // 结果如果为空，则报告执行错误
            if (results.isEmpty()) {
                System.out.println("Execute python results is empty. Thread is: " + Thread.currentThread().getName());
                return ErrorCodeEnum.ERROR_FAIL_EXEC_POLICY;
            }

            int exitVal = proc.waitFor();
            System.out.println("Exited with error code: " + exitVal + ". Thread is: " + Thread.currentThread().getName());
            if (saveResult(resultUuid, results) != ErrorCodeEnum.ERROR_OK)
                return ErrorCodeEnum.ERROR_INTERNAL_ERROR;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return ErrorCodeEnum.ERROR_OK;
    }

    private ErrorCodeEnum execCommand(String resultUuid, String cmdInfo) {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process proc = runtime.exec("cmd.exe /c " + cmdInfo);
            BufferedReader output = MyUtils.getProcReader(proc);

            String line;
            String results = "";
            while ((line = output.readLine()) != null) {
                results += line + "\n";
            }
            output.close();

            int exitVal = proc.waitFor();
            System.out.println("Exited with error code " + exitVal);
            if (saveResult(resultUuid, results) != ErrorCodeEnum.ERROR_OK)
                return ErrorCodeEnum.ERROR_INTERNAL_ERROR;
        } catch (IOException e) {
            e.printStackTrace();
            return ErrorCodeEnum.ERROR_FAIL_EXEC_POLICY;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return ErrorCodeEnum.ERROR_FAIL_EXEC_POLICY;
        }

        return ErrorCodeEnum.ERROR_OK;
    }


}
