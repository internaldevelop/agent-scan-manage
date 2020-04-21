package com.toolkit.scantaskmng.global.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import oshi.hardware.Baseboard;
import oshi.hardware.ComputerSystem;
import oshi.hardware.Firmware;
import oshi.hardware.NetworkIF;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

import java.util.Arrays;

public class OshiUtils {

    public static JSONObject getDiskInfo(OperatingSystem os) {
        JSONObject jsonObject = new JSONObject();
        try {
            FileSystem fileSystem = os.getFileSystem();
            OSFileStore[] fileStores = fileSystem.getFileStores();
            jsonObject.put("fileStores", fileStores);

            long allTotal = 0;
            long usedTotal = 0;
            for (OSFileStore fs : fileStores) {
                allTotal += fs.getTotalSpace();
                usedTotal += fs.getUsableSpace();
            }

            jsonObject.put("allTotal", allTotal);
            jsonObject.put("usedTotal", usedTotal);
            jsonObject.put("freeTotal", allTotal - usedTotal);
            Double usedPercentTotal = 100D * usedTotal / allTotal;   //总磁盘使用率
            jsonObject.put("usedPercentTotal", usedPercentTotal);
            jsonObject.put("freePercentTotal", 100 - usedPercentTotal);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public static JSONObject getComputerSystem(ComputerSystem computerSystem) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("manufacturer", computerSystem.getManufacturer());
        jsonObject.put("model", computerSystem.getModel());
        jsonObject.put("serialnumber", computerSystem.getModel());

        Firmware firmware = computerSystem.getFirmware();
        jsonObject.put("firmware", firmware);

        Baseboard baseboard = computerSystem.getBaseboard();
        jsonObject.put("baseboard", baseboard);

        return jsonObject;
    }

    public static JSONArray getNetWorkInfos(NetworkIF[] networkIFs){
        JSONArray netWorkArray = new JSONArray();

        for (NetworkIF net : networkIFs) {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("netWorkName", net.getName() + "(" + net.getDisplayName() + ")");
            jsonObj.put("macAddress", net.getMacaddr());
            jsonObj.put("mtu", net.getMTU());
            jsonObj.put("speed", FormatUtil.formatValue(net.getSpeed(), "bps"));
            jsonObj.put("IPv4", Arrays.toString(net.getIPv4addr()));
            jsonObj.put("IPv6", Arrays.toString(net.getIPv6addr()));

            netWorkArray.add(jsonObj);
        }


        return netWorkArray;
    }

}
