package com.toolkit.scantaskmng.service;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.toolkit.scantaskmng.global.bean.ResponseBean;
import com.toolkit.scantaskmng.global.response.ResponseHelper;
import com.toolkit.scantaskmng.global.utils.OshiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.util.Arrays;
import java.util.List;


@Component
@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler" })
public class AssetInfoDataService {
    protected static Logger logger = LoggerFactory.getLogger(AssetInfoDataService.class);

    @Autowired
    ResponseHelper responseHelper;

    synchronized public Object fetchAssetInfo(String types) {
        SystemInfo si = new SystemInfo();

        HardwareAbstractionLayer hal = si.getHardware();

        boolean bAll = false;
        List<String> typeList = null;
        if (types.isEmpty()) {
            bAll = true;
        } else {
            typeList = Arrays.asList(types.split(","));
        }

        JSONObject jsonInfo = new JSONObject();
        OperatingSystem os = si.getOperatingSystem();

        jsonInfo.put("os", System.getProperty("os.name"));

        jsonInfo.put("ComputerSystem", OshiUtils.getComputerSystem(hal.getComputerSystem()));

        if (bAll || typeList.contains("CPU")) {
            jsonInfo.put("CPU", hal.getProcessor());
        }

        if (bAll || typeList.contains("Memory")) { // 内存
            jsonInfo.put("Memory", hal.getMemory());
        }

        if (bAll || typeList.contains("Sensors")) { // 传感器
            jsonInfo.put("Sensors", hal.getSensors());
        }

        if (bAll || typeList.contains("Power")) { // 电源
            jsonInfo.put("Power", hal.getPowerSources());
        }

        if (bAll || typeList.contains("Disks")) { // 磁盘
            jsonInfo.put("Disks", OshiUtils.getDiskInfo(os));
        }

        if (bAll || typeList.contains("Network")) { // 网络接口
            jsonInfo.put("Network", OshiUtils.getNetWorkInfos(hal.getNetworkIFs()));
        }

        if (bAll || typeList.contains("NetworkParam")) { // 网络参数
            jsonInfo.put("NetworkParam", os.getNetworkParams());
        }

        if (bAll || typeList.contains("USB")) { // USB
            jsonInfo.put("USB", hal.getUsbDevices(true));
        }

        return jsonInfo;
    }

}
