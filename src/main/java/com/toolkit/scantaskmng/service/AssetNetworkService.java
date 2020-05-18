package com.toolkit.scantaskmng.service;


import com.alibaba.fastjson.JSONObject;
import com.toolkit.scantaskmng.global.bean.ResponseBean;
import com.toolkit.scantaskmng.global.enumeration.ErrorCodeEnum;
import com.toolkit.scantaskmng.global.response.ResponseHelper;
import com.toolkit.scantaskmng.global.response.ResponseStrBean;
import com.toolkit.scantaskmng.global.utils.MyUtils;
import com.toolkit.scantaskmng.global.utils.StringUtils;
import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.PacketReceiver;
import jpcap.packet.Packet;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
public class AssetNetworkService {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private ScheduledExecutorService threadPoolTaskScheduler = Executors.newScheduledThreadPool(1);

    public static List<TimerTask> getPacketTaskList = new ArrayList<>();  // 流量任务

    @Autowired
    ResponseHelper responseHelper;

    public static RestTemplate restTemplate = new RestTemplate();

    @Value("${main.service.ip}")
    public String mainServiceIp;

    @Value("${main.service.name}")
    private String MAIN_SERVICE_NAME;  //主服务名 embed-terminal

    public ResponseBean getDelayInfo(String type, String ip) {  // typo 1:延时; 2:吞吐量; 3:带宽;
        JSONObject jsonInfo = new JSONObject();

        try {
//            tcp_bw：B与A节点建立tcp连接能够跑的带宽（B服务器带宽为10M）。
//            tcp_lat:  B与A节点的延时。
            String typeCode = "tcp_lat";
            String command = String.format("qperf %s tcp_lat", ip);  // 延时
            if ("2".equals(type)) {
                typeCode = "tcp_bw_throughput";
                command = String.format("qperf %s tcp_bw", ip);  // 吞吐量
            } else if ("3".equals(type)) {
                typeCode = "tcp_bw";
                command = String.format("qperf %s tcp_bw", ip);  //带宽
            }

            String[] args = new String[]{"sh", "-c", command};
            BufferedReader output = MyUtils.getExecOutput(args);

            output.readLine();
            String line;
            while ((line = output.readLine()) != null) {
                line = line.replaceAll(" ", "");
                if ("2".equals(type) && line.indexOf("bw") > -1 ) {
                    line = line.substring(line.indexOf("=") + 1);

                    String sKey  = "";
                    String zCode = "";

                    String reg = "[A-Za-z]+";
                    sKey = line.replaceAll(reg, "").replaceAll("/", "");
                    logger.info("吞吐量==========================" + sKey);

                    double sKey1 = Double.parseDouble(sKey) * 0.2777;

                    zCode = line.replaceAll(sKey, "");
                    logger.info("吞吐量==========================" + zCode);

                    output.close();
                    jsonInfo.put(typeCode, sKey1 + zCode);
//                    return sKey1 + zCode;
                } else if ((line.indexOf("bw") > -1) || (line.indexOf("latency") > -1)){
                    logger.info("==========================" + line.substring(line.indexOf("=") + 1));
                    output.close();
                    jsonInfo.put(typeCode, line.substring(line.indexOf("=") + 1));
                }
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseHelper.success(jsonInfo);
    }

    @Data
    private class TimerTask {
        private ScheduledFuture<?> future;
        private String assetUuid;
    }

    /**
     * 启动抓取数据包
     * @param assetUuid
     * @return
     */
    public Object startGetPacket(String assetUuid, String secondTimeStr) {

        // 每secondTime秒执行一次
        long secondTime = 1;
        if (StringUtils.isValid(secondTimeStr)) {
            secondTime = Long.parseLong(secondTimeStr);
            secondTime = secondTime > 1 ? secondTime : 1;
        }

        rmList(getPacketTaskList, assetUuid);  // 每次启动前先停止任务

        NetworkRunnable runnable = new NetworkRunnable();
        runnable.setAssetUuid(assetUuid);

        //0延时，每secondTime秒执行下future的任务
        ScheduledFuture<?> future = threadPoolTaskScheduler.scheduleAtFixedRate(runnable, 0, secondTime, TimeUnit.SECONDS);

        if (future == null)
            return false;

        TimerTask timerTask = new TimerTask();
        timerTask.setAssetUuid(assetUuid);
        timerTask.setFuture(future);
        getPacketTaskList.add(timerTask);

        return responseHelper.success();
    }

    /**
     * 停止抓取数据包
     * @param assetUuid
     * @return
     */
    public Object stopGetPacket(String assetUuid) {
        if (!StringUtils.isValid(assetUuid))
            return false;
        return rmList(getPacketTaskList, assetUuid);
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
    class MyPacketReceiver implements PacketReceiver {

        private String assetUuid;

        /**
         * 接收包
         * @param packet
         */
        public void receivePacket(Packet packet) {

        }

        public boolean setData (String assetUuid, Object obj) {

            try {
                String url = "http://" + mainServiceIp + ":10110/" + MAIN_SERVICE_NAME + "/network/packet/setdata?datas={datas}&asset_uuid={asset_uuid}";

                Map<String, Object> param = new HashMap<>();
                param.put("datas", obj);
                param.put("asset_uuid", assetUuid);

                ResponseEntity<ResponseStrBean> responseEntity = restTemplate.getForEntity(url, ResponseStrBean.class, param);
                ResponseStrBean responseBean = responseEntity.getBody();

                // 将节点的资产实时信息通过 websocket 广播到客户端
                if (!ErrorCodeEnum.ERROR_OK.name().equals(responseBean.getCode())) {
                    stopGetPacket(assetUuid);  // 返回值不正确  停掉
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                stopGetPacket(assetUuid);
            }
            return true;
        }
    }


    @Data
    private class NetworkRunnable implements Runnable {
        private String assetUuid;

        @Override
        public void run() {
            JpcapCaptor jpcap = null;
            try{
                //获取本机上的网络接口对象数组
                final  NetworkInterface[] devices = JpcapCaptor.getDeviceList();
                if (devices.length > 0) {
                    NetworkInterface nc=devices[1];
                    // 捕获数据包(需要打开的网卡实例,一次捕获数据包的最大byte数,是否采用混乱模式,捕获的数据包的超时设置)
                    jpcap = JpcapCaptor.openDevice(nc, 1, false, 20);
                    MyPacketReceiver packetReceiver = new MyPacketReceiver();

                    packetReceiver.setAssetUuid(assetUuid);

                    //一次接包个数(个数到时产生回调),回调者
                    jpcap.loopPacket(1, packetReceiver);

                    jpcap.breakLoop();
                    jpcap.close();
                } else {
                    logger.info("devices.length=" + devices.length);
                }

            }catch(Exception e){
                e.printStackTrace();
                if (jpcap != null) {
                    jpcap.breakLoop();
                    jpcap.close();
                }
            }
        }
    }

}
