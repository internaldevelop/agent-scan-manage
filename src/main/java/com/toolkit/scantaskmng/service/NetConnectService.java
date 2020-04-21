package com.toolkit.scantaskmng.service;

import com.alibaba.fastjson.JSONObject;
import com.toolkit.scantaskmng.global.bean.ResponseBean;
import com.toolkit.scantaskmng.global.enumeration.ErrorCodeEnum;
import com.toolkit.scantaskmng.global.response.ResponseHelper;
import com.toolkit.scantaskmng.global.utils.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class NetConnectService {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ResponseHelper responseHelper;

    /**
     * 网络路径连通性测试
     * @param ip
     * @return
     */
    public ResponseBean ping(String ip) {
        boolean connect = false;
        Runtime runtime = Runtime.getRuntime();
        JSONObject props = SystemUtils.getProps();
        String sysName = props.getString("os.name");
        try {
//            String[] args = new String[]{"sh", "-c", "ping " + ip};
//            BufferedReader output = MyUtils.getExecOutput(args);

            Process process;
            if (sysName.indexOf("indows") > -1) {
                process = runtime.exec("ping " + ip + " -n 2");
            } else {
                process = runtime.exec("ping " + ip + " -c 2");
            }

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            StringBuffer sb = new StringBuffer();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            System.out.println("返回值为:"+sb);
            is.close();
            isr.close();
            br.close();

            if (null != sb && !sb.toString().equals("")) {
                String sbStr = sb.toString();
                if ((sbStr.indexOf("TTL") > -1) || (sbStr.indexOf("ttl") > -1 && sbStr.indexOf("time") > -1)) {
                    // 网络畅通
                    connect = true;
                } else {
                    // 网络不畅通
                    connect = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject jsonData = new JSONObject();
        jsonData.put("ip", ip);

        if(connect){
            jsonData.put("isconnect", "1");
        }
        else {
            jsonData.put("isconnect", "0");
        }
        return responseHelper.success(jsonData);

    }

    public ResponseBean urlResp(String url) {
        JSONObject jsonData = new JSONObject();
        boolean trueFlag = false;
        String []cmds = {"curl", "-i", "-w",
                "curl_status:%{http_code}==curl_total_time:%{time_total}s", url};
        ProcessBuilder pb=new ProcessBuilder(cmds);
        pb.redirectErrorStream(true);
        try {
            Process p = pb.start();
            BufferedReader br=null;
            String line=null;
            br=new BufferedReader(new InputStreamReader(p.getInputStream(), "gbk"));
            while((line=br.readLine())!=null){
                System.out.println("\n" + line);
                if (line.indexOf("curl_status:200") > -1 || (line.indexOf("timestamp") > -1 && line.indexOf("status") > -1)) {
                    String[] ctTimes = line.substring(line.indexOf("curl_total_time")).split(":");
                    System.out.println(ctTimes[0] + "===" + ctTimes[1]);
                    jsonData.put("total_time", ctTimes[1]);
                    trueFlag = true;
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseHelper.success(jsonData);
//        if (trueFlag) {
//
//        } else {
//            return responseHelper.error(ErrorCodeEnum.ERROR_FAIL_CONNECT, jsonData);
//        }

    }
}
