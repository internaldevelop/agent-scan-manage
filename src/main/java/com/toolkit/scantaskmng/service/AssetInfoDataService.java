package com.toolkit.scantaskmng.service;

import com.alibaba.fastjson.JSONObject;
import com.toolkit.scantaskmng.global.enumeration.ErrorCodeEnum;
import com.toolkit.scantaskmng.global.response.ResponseHelper;
import com.toolkit.scantaskmng.global.response.ResponseStrBean;
import com.toolkit.scantaskmng.global.utils.OshiUtils;
import com.toolkit.scantaskmng.global.utils.StringUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
public class AssetInfoDataService {
    public Logger logger = LoggerFactory.getLogger(this.getClass());

    private ScheduledExecutorService threadPoolTaskScheduler = Executors.newScheduledThreadPool(2);

    public static List<TimerTask> timerTaskList = new ArrayList<>();  // 资源任务

    public static List<TimerTask> networkTaskList = new ArrayList<>();  // 流量任务

    RestTemplate restTemplate = new RestTemplate();

    @Value("${main.service.ip}")
    public String mainServiceIp;

    @Autowired
    ResponseHelper responseHelper;

    private static String MAIN_SERVICE_NAME = "embed-terminal-dev";  //主服务名  fw-bend-server  embed-terminal

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Data
    private class TimerTask {
        private ScheduledFuture<?> future;
        private String assetUuid;
    }

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

    /**
     * 启动任务
     * @param assetUuid
     * @param infoTypes
     * @param secondTimeStr 每secondTimeStr秒执行一次
     * @return
     */
    public boolean startTask(String assetUuid, String infoTypes, String secondTimeStr) {
        // 未指定信息类别时，默认收集CPU使用率和内存使用率
        if (!StringUtils.isValid(infoTypes))
            infoTypes = "CPU,Memory,Disks";

        // 每secondTime秒执行一次
        long secondTime = 3;
        if (StringUtils.isValid(secondTimeStr)) {
            secondTime = Long.parseLong(secondTimeStr);
            secondTime = secondTime > 3 ? secondTime : 3;
        }

        if (!StringUtils.isValid(assetUuid))
            assetUuid = "localhost";

        if ("".equals(infoTypes))
        rmList(timerTaskList, assetUuid);  //先停止assetUuid任务


        // 创建一个 Runnable ，设置：任务和项目的 UUID
        MyRunnable runnable = new MyRunnable();
        runnable.setAssetUuid(assetUuid);
        runnable.setInfoTypes(infoTypes);

        //0延时，每secondTime秒执行下future的任务
        ScheduledFuture<?> future = threadPoolTaskScheduler.scheduleAtFixedRate(runnable, 0, secondTime, TimeUnit.SECONDS);

        if (future == null)
            return false;

        TimerTask timerTask = new TimerTask();
        timerTask.setAssetUuid(assetUuid);
        timerTask.setFuture(future);
        timerTaskList.add(timerTask);
        logger.info(assetUuid + "开始执行");

        return true;
    }

    /**
     * 启动获取流量数据任务
     * @param assetUuid
     * @param infoTypes
     * @param secondTimeStr 每secondTimeStr秒执行一次
     * @return
     */
    public boolean startNetworkTask(String assetUuid, String infoTypes, String secondTimeStr) {
        // 未指定信息类别时，默认收集CPU使用率和内存使用率
        if (!StringUtils.isValid(infoTypes))
            infoTypes = "Network";

        // 每secondTime秒执行一次
        long secondTime = 3;
        if (StringUtils.isValid(secondTimeStr)) {
            secondTime = Long.parseLong(secondTimeStr);
            secondTime = secondTime > 3 ? secondTime : 3;
        }

        if (!StringUtils.isValid(assetUuid))
            return false;

        rmList(networkTaskList, assetUuid);  //先停止assetUuid任务


        // 创建一个 Runnable ，设置：任务和项目的 UUID
        NetworkRunnable runnable = new NetworkRunnable();
        runnable.setAssetUuid(assetUuid);
        runnable.setInfoTypes(infoTypes);

        //0延时，每secondTime秒执行下future的任务
        ScheduledFuture<?> future = threadPoolTaskScheduler.scheduleAtFixedRate(runnable, 0, secondTime, TimeUnit.SECONDS);

        if (future == null)
            return false;

        TimerTask timerTask = new TimerTask();
        timerTask.setAssetUuid(assetUuid);
        timerTask.setFuture(future);
        networkTaskList.add(timerTask);

        return true;
    }

    // 停止任务
    public boolean stopTask(String assetUuid) {
        if (!StringUtils.isValid(assetUuid))
            return false;
        return rmList(timerTaskList, assetUuid);

    }

    // 停止任务
    public boolean stopNetWorkTask(String assetUuid) {
        if (!StringUtils.isValid(assetUuid))
            return false;
        return rmList(networkTaskList, assetUuid);
    }

    // 循环删除list元素
    public boolean rmList (List<TimerTask> tList, String assetUuid) {
        Iterator<TimerTask> it = tList.iterator();

        boolean flag = false;
        while(it.hasNext()){
            TimerTask tTask = it.next();

            if (assetUuid.equals(tTask.assetUuid)) {
                // 如果任务和资产的 UUID 匹配，则取消该任务计划
                ScheduledFuture<?> future = tTask.future;
                if (future != null)
                    future.cancel(true);
                // 移除任务计划
                it.remove();
                flag = true;
            }

        }
        return flag;
    }

    @Data
    private class MyRunnable implements Runnable {
        private String assetUuid ;
        private String infoTypes;

        @Override
        public void run() {
            try {
                Object responseObj = fetchAssetInfo(this.infoTypes);

                if (responseObj != null) {
                    String url = "http://" + mainServiceIp + ":10110/" + MAIN_SERVICE_NAME + "/resources/setdata?datas={datas}&asset_uuid={asset_uuid}";

                    Map<String, Object> param = new HashMap<>();
                    param.put("datas", responseObj);
                    param.put("asset_uuid", assetUuid);

                    ResponseEntity<ResponseStrBean> responseEntity = restTemplate.getForEntity(url, ResponseStrBean.class, param);
                    ResponseStrBean responseBean = (ResponseStrBean)responseEntity.getBody();

                    // 将节点的资产实时信息通过 websocket 广播到客户端
                    if (!ErrorCodeEnum.ERROR_OK.name().equals(responseBean.getCode())) {
                        stopTask(assetUuid);  // 返回值不正确  停掉
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Data
    private class NetworkRunnable implements Runnable {
        private String assetUuid ;
        private String infoTypes;

        @Override
        public void run() {
            try {
                Object responseObj = fetchAssetInfo(this.infoTypes);
                JSONObject jsonObj = (JSONObject) JSONObject.toJSON(responseObj);
                Object networkInfo = jsonObj.get("Network");

                if (responseObj != null && networkInfo != null) {
                    String url = "http://" + mainServiceIp + ":10110/" + MAIN_SERVICE_NAME + "/network/setdata?datas={datas}&asset_uuid={asset_uuid}";

                    Map<String, Object> param = new HashMap<>();
                    param.put("datas", networkInfo);
                    param.put("asset_uuid", assetUuid);

                    ResponseEntity<ResponseStrBean> responseEntity = restTemplate.getForEntity(url, ResponseStrBean.class, param);
                    ResponseStrBean responseBean = responseEntity.getBody();

                    // 将节点的资产实时信息通过 websocket 广播到客户端
                    if (!ErrorCodeEnum.ERROR_OK.name().equals(responseBean.getCode())) {
                        stopNetWorkTask(assetUuid);  // 返回值不正确  停掉
                    }
                }
            } catch (Exception e) {
                stopNetWorkTask(assetUuid);
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取资源信息
     * @param types
     * @return
     */
    public Object getAssetInfo(String types) {
        Object retObj = fetchAssetInfo(types);
        return responseHelper.success(retObj);
    }

}
