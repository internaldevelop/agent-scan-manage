package com.toolkit.scantaskmng.dao.mybatis;

import com.toolkit.scantaskmng.bean.po.TaskExecuteActionPo;
import com.toolkit.scantaskmng.bean.po.TaskPo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface TaskExecActionsMapper {
    /**
     * 读取所有有效的任务执行记录信息
     * @return 所有有效的任务执行记录信息的集合
     */
    @Select("SELECT * FROM exec_actions ex WHERE ex.status>=0 ")
    List<TaskExecuteActionPo> allTaskExecActions();

    /**
     * 添加一条任务执行记录信息
     * @param executeActionPo 除了id ，其它字段都包含
     * @return 影响记录数量，>0 表示成功，否则失败
     */
    @Insert("INSERT INTO exec_actions( " +
            "uuid, task_uuid, project_uuid, " +
            "user_uuid, comment, status, " +
            "exec_time) " +
            "VALUES ( " +
            "#{uuid}, #{task_uuid}, #{project_uuid}, " +
            "#{user_uuid},#{comment}, #{status}, " +
            "#{exec_time, jdbcType=TIMESTAMP}) "
    )
    int addTaskExecAction(TaskExecuteActionPo executeActionPo);
}
