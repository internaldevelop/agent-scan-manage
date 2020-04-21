package com.toolkit.scantaskmng.bean.dto;

import lombok.Data;

/**
 * 任务结果集
 */
@Data
public class TaskResultsStatisticsDto {

    private String os_type;  // 系统名称

    private String policie_name;  // 策略名称

    private int num; // 数量

}
