package com.toolkit.scantaskmng.bean.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * 资产使用率
 */
@Component
@Data
public class AssetPerfDataPo {
    private int id;
    private String uuid;
    private String asset_uuid;
    private String cpu_used_percent;
    private String memory_used_percent;
    private String disk_used_percent;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private java.sql.Timestamp create_time;

}
