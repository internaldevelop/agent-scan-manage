package com.toolkit.scantaskmng.bean.dto;

import com.toolkit.scantaskmng.bean.po.TaskExecuteResultsPo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务结果集
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TaskResultsDto extends TaskExecuteResultsPo {

    private String task_id;  // 任务id
    private String task_name;  // 任务名称
    private String assets_name;  // 检查目标
    private String assets_ip;  // 目标IP
    private String description;  // 任务描述
    private String policy_name;  // 策略名称

}
