package com.toolkit.scantaskmng.seconfig.linux;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.toolkit.scantaskmng.global.utils.MyUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;

@Component
public class SysServiceConfig {

    // 1. 修改 ssh 的连接端口，建议改成非标准的高端端口【1024到65535】
    // Port  22022
    // 2. 禁止 root 用户直接登录 ssh，用普通账号 ssh 连接，然后在切换到 root 账号登录。
    // 在/etc/ssh/sshd_config配置文件中设置以下参数
    // PermitRootLogin  no
    // 3. 限制 ssh 连接的 IP 地址，只允许用户指定的 IP 地址可以 ssh 连接服务器。
    // 修改 /etc/hosts.allow 和 /etc/hosts.deny 这两个配置文件。
    // vim /etc/hosts.deny     #设置禁止所有ip连接服务器的ssh。
    // sshd:all:deny
    // vim  /etc/hosts.allow    #设置允许指定ip连接服务器的ssh。
    // sshd:210.xx.xx.xx:allow
    public JSONObject acquireSSHProps() {
        JSONObject sshProps = new JSONObject();
        if (!getSSHPort(sshProps))
            return null;

        if (!getSSHLoginPermit(sshProps))
            return null;

        if (!getSSHConnectRestrict(sshProps))
            return null;

        return sshProps;
    }

    private boolean getSSHPort(JSONObject sshProps) {
        try {
            String[] args = new String[] { "sh", "-c", "netstat -tnpl4 | grep sshd" };
            BufferedReader output = MyUtils.getExecOutput(args);

            // Examples:
            // tcp        0      0 0.0.0.0:22              0.0.0.0:*               LISTEN      1313/sshd
            String line = output.readLine();
            if (line == null)
                return false;

            String[] params = line.split(" +");
            String[] ports = params[3].split(":");
            sshProps.put("port", ports[1]);

            output.close();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean getSSHLoginPermit(JSONObject sshProps) {
        try {
            String[] args = new String[] { "sh", "-c", "cat /etc/ssh/sshd_config | grep ^PermitRootLogin" };
            BufferedReader output = MyUtils.getExecOutput(args);

            // Examples:
            // #PermitRootLogin yes
            String line = output.readLine();
            if (line == null || line.isEmpty()) {
                sshProps.put("PermitRootLogin", true);
            } else {
                String[] params = line.split(" +");
                sshProps.put("PermitRootLogin", params[1].equalsIgnoreCase("no") ? false : true);
            }

            output.close();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean getSSHConnectRestrict(JSONObject sshProps) {
        try {
            // Read the host deny setting of the ssh service
            String[] args = new String[] { "sh", "-c", "cat /etc/hosts.deny | grep sshd:all:deny" };
            BufferedReader output = MyUtils.getExecOutput(args);

            String line = output.readLine();
            if (line == null || line.isEmpty())
                sshProps.put("denyAll", false);
            else
                sshProps.put("denyAll", true);
            output.close();

            // Read the host deny setting of the ssh service
            args = new String[] { "sh", "-c", "cat /etc/hosts.allow | grep ^sshd" };
            output = MyUtils.getExecOutput(args);

            JSONArray allowList = new JSONArray();
            while ((line = output.readLine()) != null) {
                String[] params = line.split(":");
                JSONObject jsonIP = new JSONObject();
                jsonIP.put("IP", params[1]);
                allowList.add(jsonIP);
            }
            sshProps.put("allowIPs", allowList);

            output.close();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
