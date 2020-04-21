package com.toolkit.scantaskmng.service;

import com.alibaba.fastjson.JSONObject;
import com.toolkit.scantaskmng.dao.mybatis.PoliciesMapper;
import com.toolkit.scantaskmng.dao.mybatis.TasksMapper;
import com.toolkit.scantaskmng.global.bean.ResponseBean;
import com.toolkit.scantaskmng.global.response.ResponseHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NodesManageService {
    @Autowired
    private ResponseHelper responseHelper;
    @Autowired
    private TasksMapper tasksMapper;
    @Autowired
    private PoliciesMapper policiesMapper;
    @Autowired
    private TaskRunStatusService taskRunStatusService;

    @Autowired
    private ExecutePolicyThread execThread;

    public NodesManageService() {
    }

    public ResponseBean runTask(String projectUuid, String taskUuid, String userUuid) {
        // 执行策略
        JSONObject taskInfo = new JSONObject();
        taskInfo.put("project_uuid", projectUuid);
        taskInfo.put("task_uuid", taskUuid);
        taskInfo.put("user_uuid", userUuid);
        TaskRunQueue.add(taskInfo);
        Thread thread = new Thread(execThread);
        thread.start();

        return responseHelper.success();

    }
}
