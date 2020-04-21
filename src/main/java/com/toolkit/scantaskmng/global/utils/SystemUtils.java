package com.toolkit.scantaskmng.global.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class SystemUtils {
    public static Properties sysProps = System.getProperties();

    public static InetAddress getLocalHostLANAddress() {
        try {
            InetAddress candidateAddress = null;
            // 遍历所有的网络接口
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // 在所有的接口下再遍历IP
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            // site-local类型的地址未被发现，先记录候选地址
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress;
            }
            // 如果没有发现 non-loopback地址.只能用最次选的方案
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            return jdkSuppliedAddress;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> getLocalIPList() {
        List<String> listIp = new ArrayList<String>();
        try {
            InetAddress candidateAddress = null;
            // 遍历所有的网络接口
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // 在所有的接口下再遍历IP
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了
                            listIp.add(inetAddr.getHostAddress());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listIp;
    }

    static public String getOsName() {
        return sysProps.getProperty("os.name");
    }

    static public String getOsArch() {
        return sysProps.getProperty("os.arch");
    }

    static public String getOsVersion() {
        return sysProps.getProperty("os.version");
    }

    static public boolean isWindows() {
        String osName = getOsName();
        return osName.indexOf("Windows") >= 0;
    }

    static public String getProp(String propName) {
        return sysProps.getProperty(propName);
    }

    static public JSONObject getProps() {
        String[] keys = {
                "os.name", "sun.boot.library.path", "user.dir", "user.country", "java.runtime.version",
                "os.arch", "line.separator", "os.version", "user.home", "user.timezone", "user.name",
                "user.language", "file.separator",
                "java.specification.version", "java.home", "sun.arch.data.model", "awt.toolkit",
                "sun.jnu.encoding", "java.vm.version", "java.library.path", "java.class.version",
                "java.runtime.name", "java.vm.vendor", "file.encoding", "java.version", "java.vendor",
                "java.vm.name", "sun.os.patch.level", "PID", "catalina.base", "sun.cpu.endian",
                "java.awt.graphicsenv", "java.endorsed.dirs", "java.io.tmpdir", "sun.desktop"
        };
        JSONObject props = new JSONObject();

        for (String key: keys) {
            props.put(key, sysProps.getProperty(key));
        }
        return props;
    }

    static public String getEnvEncoding() {
        return sysProps.getProperty("sun.jnu.encoding");
    }

    static public JSONArray getOpenPorts() {
        JSONArray openPorts = new JSONArray();
        if (isWindows()) {
            // Windows: netstat -an
            try {
                String[] args = new String[] { "netstat", "-an" };
                BufferedReader output = MyUtils.getExecOutput(args);
                String line;
                while ((line = output.readLine()) != null) {
                    // 去除首尾空格
                    line = line.trim();
                    // 跳过无用数据行
                    if (!line.startsWith("TCP") && !line.startsWith("UDP"))
                        continue;

                    // Examples:
                    // 协议   本地地址               外部地址               状态
                    // TCP    [::]:135               [::]:0                 LISTENING
                    // TCP    192.168.137.55:54510   58.87.82.241:443       ESTABLISHED
                    JSONObject portObj = new JSONObject();
                    String[] params = line.split(" +");
                    // 协议
                    portObj.put("protocol", params[0]);
                    // 本地地址和端口，未知地址按 0.0.0.0 表示
                    String[] localAddr = params[1].split(":");
                    if (localAddr.length > 2) {
                        portObj.put("localAddr", "0.0.0.0");
                    } else {
                        portObj.put("localAddr", localAddr[0]);
                    }
                    portObj.put("localPort", localAddr[localAddr.length - 1]);
                    // 外部地址和端口，未知地址按 0.0.0.0 表示
                    String[] foreignAddr = params[2].split(":");
                    if (foreignAddr.length > 2) {
                        portObj.put("foreignAddr", "0.0.0.0");
                    } else {
                        portObj.put("foreignAddr", foreignAddr[0]);
                    }
                    portObj.put("foreignPort", foreignAddr[foreignAddr.length - 1]);
                    // 状态
                    if (line.startsWith("TCP")) {
                        portObj.put("status", params[3]);
                    } else {
                        portObj.put("status", "None");
                    }
                    // 序号
                    portObj.put("index", openPorts.size() + 1);

                    // 添加本条记录
                    openPorts.add(portObj);

                }

                output.close();
            } catch (IOException e) {
                e.printStackTrace();
                return openPorts;
            }
        } else {
            // Linux: netstat -tunpl
            try {
                String[] args = new String[] { "netstat", "-tunpl" };
                BufferedReader output = MyUtils.getExecOutput(args);
                String line;
                while ((line = output.readLine()) != null) {
                    // 去除首尾空格
                    line = line.trim();
                    // 跳过无用数据行
                    if (!line.startsWith("tcp") && !line.startsWith("udp"))
                        continue;

                    // Examples:
                    // 1     7      14     21                      45                      69          81
                    // Proto Recv-Q Send-Q Local Address           Foreign Address         State       PID/Program name
                    // tcp        0      0 127.0.0.1:41980         0.0.0.0:*               LISTEN      81562/java
                    // tcp6       0      0 :::22                   :::*                    LISTEN      1348/sshd
                    // udp        0      0 0.0.0.0:41239           0.0.0.0:*                           938/avahi-daemon: r
                    String uLine = line.toUpperCase();
                    JSONObject portObj = new JSONObject();
                    // 协议
                    portObj.put("protocol", uLine.substring(0, 5).trim());
                    // 本地地址和端口，未知地址按 0.0.0.0 表示
                    String[] localAddr = uLine.substring(20, 43).trim().split(":");
                    if (localAddr.length > 2) {
                        portObj.put("localAddr", "0.0.0.0");
                    } else {
                        portObj.put("localAddr", localAddr[0]);
                    }
                    portObj.put("localPort", localAddr[localAddr.length - 1]);
                    // 外部地址和端口，未知地址按 0.0.0.0 表示
                    String[] foreignAddr = uLine.substring(44, 67).trim().split(":");
                    if (foreignAddr.length > 2) {
                        portObj.put("foreignAddr", "0.0.0.0");
                    } else {
                        portObj.put("foreignAddr", foreignAddr[0]);
                    }
                    portObj.put("foreignPort", foreignAddr[foreignAddr.length - 1]);
                    // 状态
                    if (line.startsWith("tcp")) {
                        portObj.put("status", uLine.substring(68, 79).trim());
                    } else {
                        portObj.put("status", "None");
                    }
                    // 序号
                    portObj.put("index", openPorts.size() + 1);

                    // 添加本条记录
                    openPorts.add(portObj);
                }

                output.close();
            } catch (IOException e) {
                e.printStackTrace();
                return openPorts;
            }
        }

        return openPorts;
    }

}
