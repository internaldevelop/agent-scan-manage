package com.toolkit.scantaskmng.service;

import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import com.toolkit.scantaskmng.global.algorithm.AESEncrypt;
import com.toolkit.scantaskmng.global.algorithm.RSAEncrypt;
import com.toolkit.scantaskmng.global.enumeration.ErrorCodeEnum;
import com.toolkit.scantaskmng.global.response.ResponseHelper;
import com.toolkit.scantaskmng.global.response.ResponseStrBean;
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
import sun.misc.BASE64Decoder;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
public class AssetCollectScheduler {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    AssetInfoDataService assetInfoDataService;

    private ScheduledExecutorService threadPoolTaskScheduler = Executors.newScheduledThreadPool(1);

    public static List<TimerTask> timerTaskList = new ArrayList<>();

    RestTemplate restTemplate = new RestTemplate();

    @Value("${main.service.ip}")
    public String mainServiceIp;

    @Autowired
    ResponseHelper responseHelper;


    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Data
    private class TimerTask {
        private ScheduledFuture<?> future;
        private String assetUuid;
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

        return true;
    }

    // 停止任务
    public boolean stopTask(String assetUuid) {
        if (!StringUtils.isValid(assetUuid))
            return false;

        return rmList(timerTaskList, assetUuid);

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

            Object responseObj = assetInfoDataService.fetchAssetInfo(this.infoTypes);

//            传给bend_server
            JSONObject jsonMsg = (JSONObject) JSONObject.toJSON(responseObj);

            String url = "http://" + mainServiceIp + ":10110/fw-bend-server/resources/setdata?datas={datas}&asset_uuid={asset_uuid}";
            Map<String, Object> param = new HashMap<>();

            param.put("datas", jsonMsg.get("payload"));
            param.put("asset_uuid", assetUuid);
            try {
                ResponseEntity<ResponseStrBean> responseEntity = restTemplate.getForEntity(url, ResponseStrBean.class, param);
                ResponseStrBean responseBean = (ResponseStrBean)responseEntity.getBody();

                // 将节点的资产实时信息通过 websocket 广播到客户端
                if (!ErrorCodeEnum.ERROR_OK.name().equals(responseBean.getCode())) {

                    stopTask(assetUuid);  // 返回值不正确  停掉
                }
            } catch (Exception e) {
                stopTask(assetUuid);
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
        Object retObj = assetInfoDataService.fetchAssetInfo(types);
        return responseHelper.success(retObj);
    }



}
