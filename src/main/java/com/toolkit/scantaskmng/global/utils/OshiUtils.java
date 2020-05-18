package com.toolkit.scantaskmng.global.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import oshi.hardware.*;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

import java.util.Arrays;

public class OshiUtils {

    public static JSONObject getDiskInfo(OperatingSystem os, boolean detailFlag) {
        JSONObject jsonObject = new JSONObject();
        try {
            FileSystem fileSystem = os.getFileSystem();
            OSFileStore[] fileStores = fileSystem.getFileStores();
            if (detailFlag) {
                jsonObject.put("fileStores", fileStores);
            }

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
            jsonObject.put("usedPercentTotal", String.format(" %.2f", usedPercentTotal));
            jsonObject.put("freePercentTotal", String.format(" %.2f", (100 - usedPercentTotal)));

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

    public static Object getNetWorkInfos(NetworkIF[] networkIFs){
        JSONArray netWorkArray = new JSONArray();
        for (NetworkIF net : networkIFs) {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("netWorkName", net.getName() + "(" + net.getDisplayName() + ")");
            jsonObj.put("macAddress", net.getMacaddr());
            jsonObj.put("mtu", net.getMTU());
            jsonObj.put("speed", FormatUtil.formatValue(net.getSpeed(), "bps"));
            jsonObj.put("IPv4", Arrays.toString(net.getIPv4addr()));
            jsonObj.put("IPv6", Arrays.toString(net.getIPv6addr()));
            String bytesRecv = FormatUtil.formatBytes(net.getBytesRecv());
            jsonObj.put("bytesRecv", net.getBytesRecv());
//            String bytesSent = FormatUtil.formatBytes(net.getBytesSent());
            jsonObj.put("bytesSent", net.getBytesSent());
            jsonObj.put("timeStamp", net.getTimeStamp());
            long packetsRecv = net.getPacketsRecv();
            jsonObj.put("packetsRecv", packetsRecv);
            long packetsSent = net.getPacketsSent();
            jsonObj.put("packetsSent", packetsSent);

            netWorkArray.add(jsonObj);
        }

        return netWorkArray;
    }

    public static JSONObject getMemoryInfo(GlobalMemory memory, boolean detailFlag){
        JSONObject jsonObj = new JSONObject();
        long total = memory.getTotal();
        long available = memory.getAvailable();

        double freePercent = 100D * available / total;
        double usedPercent = 100 - freePercent;

        if (detailFlag) {
            jsonObj.put("swapTotal", memory.getSwapTotal());
            jsonObj.put("swapUsed", memory.getSwapUsed());
            jsonObj.put("swapPagesIn", memory.getSwapPagesIn());
            jsonObj.put("swapPagesOut", memory.getSwapPagesOut());
            jsonObj.put("pageSize", memory.getPageSize());
            jsonObj.put("total", total);
            jsonObj.put("available", available);
        }
        jsonObj.put("usedPercentTotal", String.format(" %.2f", usedPercent));
        jsonObj.put("freePercentTotal", String.format(" %.2f", freePercent));
        return jsonObj;
    }


    public static Object getCPUInfo(CentralProcessor processor, boolean detailFlag) {
        JSONObject jsonObj = new JSONObject();
        double used = processor.getSystemCpuLoad();

        double usedPercent = used * 100;
        double freePercent = 100 - usedPercent;

        if (detailFlag) {
            jsonObj.put("logicalProcessorCount", processor.getLogicalProcessorCount());
            jsonObj.put("physicalProcessorCount", processor.getPhysicalProcessorCount());
            jsonObj.put("physicalPackageCount", processor.getPhysicalPackageCount());
            jsonObj.put("processorID", processor.getProcessorID());
            jsonObj.put("interrupts", processor.getInterrupts());
            jsonObj.put("systemUptime", processor.getSystemUptime());
            jsonObj.put("systemSerialNumber", processor.getSystemSerialNumber());
            jsonObj.put("processorCpuLoadTicks", processor.getProcessorCpuLoadTicks());
            jsonObj.put("contextSwitches", processor.getContextSwitches());
            jsonObj.put("systemCpuLoadTicks", processor.getSystemCpuLoadTicks());
            jsonObj.put("name", processor.getName());
            jsonObj.put("identifier", processor.getIdentifier());
            jsonObj.put("vendor", processor.getVendor());
            jsonObj.put("stepping", processor.getStepping());
            jsonObj.put("vendorFreq", processor.getVendorFreq());
            jsonObj.put("family", processor.getFamily());
            jsonObj.put("systemLoadAverage", processor.getSystemLoadAverage());
            jsonObj.put("systemCpuLoad", processor.getSystemCpuLoad());
            jsonObj.put("processorCpuLoadBetweenTicks", processor.getProcessorCpuLoadBetweenTicks());
            jsonObj.put("model", processor.getModel());
        }
        jsonObj.put("usedPercentTotal", String.format(" %.2f", usedPercent));
        jsonObj.put("freePercentTotal", String.format(" %.2f", freePercent));

        return jsonObj;
    }
}
