package com.toolkit.scantaskmng.bean.dto;

import com.toolkit.scantaskmng.bean.po.TaskPo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务信息表，其中包括部分资产信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TaskInfosDto extends TaskPo {
    // 资产信息
    private String asset_uuid;  // 主机uuid
    private String asset_name;  // 主机名称
    private String asset_ip;  // 主机ip
    private String asset_port;  // 主机port
    private String asset_os_type;  // 系统类型
    private String asset_os_ver;  // 系统版本
    private String asset_login_user;  // 资产的登录用户名
    private String asset_login_pwd;  // 资产的登录密码
    // 创建该任务用户信息
    private String user_name;   // 创建该任务的用户姓名
    private String user_account;   // 创建该任务的用户账户
}
