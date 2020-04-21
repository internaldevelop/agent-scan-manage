package com.toolkit.scantaskmng.bean.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class TaskExecuteActionPo {
    private int id;
    private String uuid;
    private String task_uuid;
    private String project_uuid;
    private String user_uuid;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private java.sql.Timestamp exec_time;
    private String comment;
    private int status;
}
