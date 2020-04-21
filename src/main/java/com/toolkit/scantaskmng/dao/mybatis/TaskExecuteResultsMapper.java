package com.toolkit.scantaskmng.dao.mybatis;

import com.toolkit.scantaskmng.bean.dto.TaskResultsDto;
import com.toolkit.scantaskmng.bean.dto.TaskResultsStatisticsDto;
import com.toolkit.scantaskmng.bean.po.TaskExecuteResultsPo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface TaskExecuteResultsMapper {
    /**
     * 新增一条测试结果记录，只包含任务策略启动的信息，不包含测试或分析结果
     * @param resultPo
     * @return
     */
    @Insert("INSERT INTO task_execute_results( \n" +
            "uuid, task_uuid, start_time, \n" +
            "process_flag, policy_uuid, exec_action_uuid, \n" +
            "create_time) \n" +
            "VALUES ( \n" +
            "#{uuid}, #{task_uuid}, #{start_time, jdbcType=TIMESTAMP}, \n" +
            "#{process_flag}, #{policy_uuid}, #{exec_action_uuid}, \n" +
            "#{create_time, jdbcType=TIMESTAMP}) ")
    int addExecuteRecord(TaskExecuteResultsPo resultPo);

    /**
     * 更新测试或分析结果
     * @param resultPo
     * @return
     */
    @Update("UPDATE task_execute_results t SET \n" +
            "end_time=#{end_time, jdbcType=TIMESTAMP}, \n" +
            "results=#{results}, process_flag=#{process_flag}, \n" +
            "risk_level=#{risk_level}, risk_desc=#{risk_desc}, solutions=#{solutions} \n" +
            "WHERE \n" +
            "t.uuid=#{uuid} "
    )
    int updateExecResult(TaskExecuteResultsPo resultPo);

    /**
     * 读取任务结果
     * @return 成功时返回 TaskExecuteResultsProps 的列表，失败时返回 null1
     */
    @Select("SELECT\n" +
            "	ter.*,\n" +
            "	t. NAME AS task_name,\n" +
            "	t.description AS description,\n" +
            "	t.id AS task_id,\n" +
            "	a. NAME AS assets_name,\n" +
            "	a.ip AS assets_ip,\n" +
            "   p.name AS policy_name\n" +
            " FROM\n" +
            "	task_execute_results ter\n" +
            " INNER JOIN tasks t ON ter.task_uuid = t.uuid\n" +
            " INNER JOIN assets a ON t.asset_uuid = a.uuid\n" +
            " INNER JOIN policies p ON ter.policy_uuid = p.uuid\n" +
            " where t.`name` LIKE '%${taskNameIpType}%' OR a.ip LIKE '%${taskNameIpType}%' OR p.name LIKE '%${taskNameIpType}%'")
    List<TaskResultsDto> allTaskResults(@Param("taskNameIpType") String taskNameIpType);

    @Select("SELECT\n" +
            "	p.`name` AS policy_name,\n" +
            "	a.os_type AS os_type,\n" +
            "	COUNT(1) AS num\n" +
            " FROM\n" +
            "	task_execute_results ter\n" +
            " INNER JOIN tasks t ON ter.task_uuid = t.uuid\n" +
            " INNER JOIN assets a ON t.asset_uuid = a.uuid\n" +
            " INNER JOIN policies p ON ter.policy_uuid = p.uuid\n" +
            " GROUP BY\n" +
            "	p.id,\n" +
            "	a.os_type")
    List<TaskResultsStatisticsDto> getResultsStatistics();

    @Select("SELECT\n" +
            "	p.`name` AS policy_name,\n" +
            "	COUNT(1) AS num\n" +
            " FROM\n" +
            "	task_execute_results ter\n" +
            " INNER JOIN tasks t ON ter.task_uuid = t.uuid\n" +
            " INNER JOIN policies p ON ter.policy_uuid = p.uuid\n" +
            " GROUP BY\n" +
            "	p.id")
    List<TaskResultsStatisticsDto> getResultsPolicieStatistics();

    @Select("SELECT\n" +
            "	a.os_type AS os_type,\n" +
            "	COUNT(1) AS num\n" +
            " FROM\n" +
            "	task_execute_results ter\n" +
            " INNER JOIN tasks t ON ter.task_uuid = t.uuid\n" +
            " INNER JOIN assets a ON t.asset_uuid = a.uuid\n" +
            " GROUP BY\n" +
            "	a.os_type")
    List<TaskResultsStatisticsDto> getResultsSysStatistics();
}
