package com.toolkit.scantaskmng.service;

import com.alibaba.fastjson.JSONObject;
import com.toolkit.scantaskmng.global.bean.ResponseBean;
import com.toolkit.scantaskmng.global.response.ResponseHelper;
import com.toolkit.scantaskmng.global.utils.MyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;

@Component
public class AssetNetworkService {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ResponseHelper responseHelper;

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


}
