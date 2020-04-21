package com.toolkit.scantaskmng.bean.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class TaskRunStatusDto implements Serializable {
    String execute_uuid;        // 任务执行的 UUID
    String project_uuid;        // 项目 UUID
    String task_uuid;           // 任务 UUID
    int run_status;             // 任务状态
    int total_jobs_count;       // 要执行的子任务总数量
    int done_jobs_count;        // 已完成的子任务数量
    int remain_time;            // 预计剩余执行时间（单位：ms）
    int total_time;             // 预计总计执行时间（单位：ms）
    double   done_rate;         // 完成比例（单位：%，按执行时间计算）
    // exception
    String fail_policy_uuid;    // policy failed to execute
    String fail_policy_name;    // policy failed to execute
    String fail_policy_group_uuid;  // failed policy group
//    String fail_policy_group_name;  // failed policy group
}
