package com.toolkit.scantaskmng.service;


import com.alibaba.fastjson.JSONObject;
import com.toolkit.scantaskmng.global.bean.ResponseBean;
import com.toolkit.scantaskmng.global.enumeration.ErrorCodeEnum;
import com.toolkit.scantaskmng.global.response.ResponseHelper;
import com.toolkit.scantaskmng.global.response.ResponseStrBean;
import com.toolkit.scantaskmng.global.utils.MyUtils;
import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.PacketReceiver;
import jpcap.packet.*;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
public class AssetNetworkService {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ResponseHelper responseHelper;

    public static RestTemplate restTemplate = new RestTemplate();

    @Value("${main.service.ip}")
    public String mainServiceIp;

    @Value("${main.service.name}")
    private String MAIN_SERVICE_NAME;  //主服务名 embed-terminal

    public ResponseBean getDelayInfo(String type, String ip) {  // type 1:延时; 2:吞吐量; 3:带宽;
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

    /**
     * 启动抓取数据包
     * @param assetUuid
     * @return
     */
    public Object startGetPacket(String assetUuid) {
        logger.info("java.library.path:" + System.getProperty("java.library.path"));
        this.getPacket(assetUuid);
        return responseHelper.success();
    }

    public boolean getPacket (String assetUuid) {
        JpcapCaptor jpcap = null;
        try {
            NetworkInterface[] devices = JpcapCaptor.getDeviceList();
            for (NetworkInterface device : devices) {
                if (device != null) {
                    // 捕获数据包(需要打开的网卡实例,一次捕获数据包的最大byte数,是否采用混乱模式,捕获的数据包的超时设置)
                    jpcap = JpcapCaptor.openDevice(device, 1, false, 20);

                    PacketThread packetThread = new PacketThread();
                    packetThread.setJpcap(jpcap);
                    packetThread.setAssetUuid(assetUuid);

                    packetThread.start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Data
    class MyPacketReceiver implements PacketReceiver {

        private String assetUuid;
        private Random rand = new Random();

        /**
         * 接收包
         * @param packet
         */
        public void receivePacket(Packet packet) {

            int randNum = rand.nextInt(100);  // 0-99随机数 TODO
            logger.info("MyPacketReceiver.receivePacket---------------" + randNum);
//            if (randNum > 98) {}
            JSONObject objRet = new JSONObject();

            String sourceIp = "";  // 源IP
            String sourcePort = "";  // 源端口
            String destIp = "";  // 目的IP
            String destPort = "";  // 目的端口
            String transportProtocol = "";  // 传输协议
            String appProtocol = "";  // 应用协议

            boolean isKnow = true;

            //Tcp包,在java Socket中只能得到负载数据
            if (packet instanceof TCPPacket) {
                TCPPacket p = (TCPPacket) packet;

                sourceIp = "" + p.src_ip;  // 源IP
                sourcePort = "" + p.src_port;  // 源端口
                destIp = "" + p.dst_ip;  // 目的IP
                destPort = "" + p.dst_port;  // 目的端口
                transportProtocol = "TCP";  // 传输协议
                appProtocol = "";  // 应用协议

            } else if (packet instanceof UDPPacket) {  // UDP包,开着QQ,你就会看到:它是tcp+udp
                UDPPacket p = (UDPPacket) packet;

                sourceIp = "" + p.src_ip;  // 源IP
                sourcePort = "" + p.src_port;  // 源端口
                destIp = "" + p.dst_ip;  // 目的IP
                destPort = "" + p.dst_port;  // 目的端口
                transportProtocol = "UDP";  // 传输协议
                appProtocol = "";  // 应用协议

            } else if (packet instanceof ICMPPacket) {  //如果你要在程序中构造一个ping报文,就要构建ICMPPacket包
                ICMPPacket p = (ICMPPacket) packet;

                sourceIp = "" + p.src_ip;  // 源IP
                destIp = "" + p.dst_ip;  // 目的IP
                transportProtocol = "ICMP";  // 传输协议
                appProtocol = "";  // 应用协议

            } else if (packet instanceof ARPPacket) {  //是否地址转换协议请求包
                ARPPacket p = (ARPPacket) packet;

                sourceIp = "" + p.getSenderProtocolAddress();  // 源IP
                destIp = "" + p.getTargetProtocolAddress();  // 目的IP
                transportProtocol = "ARP";  // 传输协议
                appProtocol = "";  // 应用协议

            } else if (packet instanceof IPPacket) {
                IPPacket p = (IPPacket) packet;

                sourceIp = "" + p.src_ip;  // 源IP
                destIp = "" + p.dst_ip;  // 目的IP
                transportProtocol = "IP";  // 传输协议
                appProtocol = "";  // 应用协议

            } else {
                isKnow = false;
            }

            if (isKnow) {
                objRet.put("source_ip", sourceIp.replace("/", ""));  // 源IP
                objRet.put("source_port", sourcePort);  // 源端口
                objRet.put("dest_ip", destIp.replace("/", ""));  // 目的IP
                objRet.put("dest_port", destPort);  // 目的端口
                objRet.put("transport_protocol", transportProtocol);  // 传输协议
                objRet.put("app_protocol", appProtocol);  // 应用协议
                objRet.put("src_data", packet);

                setData(assetUuid, objRet);
            }
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
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    @Data
    class PacketThread extends Thread {
        private String assetUuid;
        private JpcapCaptor jpcap;

        @Override
        public void run() {

            MyPacketReceiver packetReceiver = new MyPacketReceiver();
            packetReceiver.setAssetUuid(assetUuid);

            //使用接包处理器循环抓包
            jpcap.loopPacket(-1, packetReceiver);
            jpcap.breakLoop();
            jpcap.close();
        }
    }

}
