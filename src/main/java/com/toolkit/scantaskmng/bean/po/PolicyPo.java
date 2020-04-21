package com.toolkit.scantaskmng.bean.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * PolicyProps 对应 policies 表
 * 各字段名称一致，各字段定义略，可参考 policies 表的各字段注释
 */
@Component
@Data
public class PolicyPo {
    private int id;
    private String uuid;
    private String name;
    private String code;
    private String group_uuid;
    private String type;
    private int risk_level;
    private String solutions;
    private String create_user_uuid;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private java.sql.Timestamp create_time;
    private int status;
    private int os_type;
    private int baseline;
    private String lv1_require;
    private String lv2_require;
    private String lv3_require;
    private String lv4_require;
    private int run_mode;
    private String run_contents;
    private int consume_time;
    private String asset_uuid;
}
