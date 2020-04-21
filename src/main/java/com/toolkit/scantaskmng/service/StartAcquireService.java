package com.toolkit.scantaskmng.service;

import com.alibaba.fastjson.JSONObject;
import com.toolkit.scantaskmng.bean.po.AssetPerfDataPo;
import com.toolkit.scantaskmng.dao.mybatis.AssetNetworkMapper;
import com.toolkit.scantaskmng.global.bean.ResponseBean;
import com.toolkit.scantaskmng.global.utils.MyUtils;
import com.toolkit.scantaskmng.global.utils.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

@Component
public class StartAcquireService implements ApplicationRunner {

    private static Logger logger = LoggerFactory.getLogger(StartAcquireService.class);
    @Autowired
    AssetInfoService assetInfoService;
    @Autowired
    AssetNetworkMapper assetNetworkMapper;

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // 创建一个 Runnable ，设置：任务和项目的 UUID
        MyRunnable runnable = new MyRunnable();

        // 制定任务计划
        ScheduledFuture<?> future = threadPoolTaskScheduler.schedule(runnable, new CronTrigger("0/30 * * * * *"));

    }

        private class MyRunnable implements Runnable {
        @Override
        public void run() {
            AssetPerfDataPo apdPo = new AssetPerfDataPo();
            List<String> localIPList = SystemUtils.getLocalIPList();


            String assetUUids = assetNetworkMapper.getAssetUUid(localIPList);
            Date now = new Date();
            if (!StringUtils.isEmpty(assetUUids)) {

                ResponseBean ret =assetInfoService.fetchAssetInfo("CPU Usage,Mem,FST");
                JSONObject payload = (JSONObject) ret.getPayload();

                JSONObject objFst = payload.getJSONObject("FST");
                if (objFst != null) {
                    String fstUsedPercent = objFst.getString("usedPercentTotal");
                    apdPo.setDisk_used_percent(fstUsedPercent);
                    System.out.println(fstUsedPercent + "====================================FST");
                } else {
                    apdPo.setDisk_used_percent("0");
                }


                String cpuUsedPercent = payload.getString("CPU usage");
                apdPo.setCpu_used_percent(cpuUsedPercent);
                System.out.println(cpuUsedPercent + "====================================CPU");
                JSONObject objMemory = payload.getJSONObject("Memory");
                if (objMemory != null) {
                    String memoryUsedPercent = objMemory.getString("usedPercent");
                    apdPo.setMemory_used_percent(memoryUsedPercent);
                    System.out.println(memoryUsedPercent + "====================================Memory");
                } else {
                    apdPo.setMemory_used_percent("0");
                }
                apdPo.setUuid(UUID.randomUUID().toString());
                for(String assetUUid : assetUUids.split(",")) {
                    apdPo.setAsset_uuid(assetUUid);
                    Timestamp currentTime = MyUtils.getCurrentSystemTimestamp();
                    apdPo.setCreate_time(currentTime);
                    assetNetworkMapper.addAssetPerfData(apdPo);
                }
            }
        }

    }
}
