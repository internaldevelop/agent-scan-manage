package com.toolkit.scantaskmng.seconfig.linux;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.toolkit.scantaskmng.global.utils.MyUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;

@Component
public class FirewallConfig {
    // 查看所有打开的端口
    // firewall-cmd --zone=public --list-ports
    public JSONObject acquireFirewallProps() {
        JSONObject firewallProps = new JSONObject();
        // systemctl status  OR  firewall-cmd --state
        boolean active = MyUtils.checkServiceActive("firewalld");
        firewallProps.put("active", active);
//        if (!getFirewallState(firewallProps))
//            return firewallProps;

        if (active) {
            if (!getOpenPorts(firewallProps))
                return firewallProps;
        }

        return firewallProps;
    }

    // firewall-cmd --zone=public --list-ports
    private boolean getOpenPorts(JSONObject fwProps) {
        try {
            String[] args = new String[] { "firewall-cmd", "--zone=public", "--list-ports" };
            BufferedReader output = MyUtils.getExecOutput(args);

            String line = output.readLine();
            if (line == null || line.isEmpty())
                return false;

            JSONArray jsonPorts = new JSONArray();
            String[] ports = line.split(" +");
            for (String port: ports) {
                String[] params = port.split("/");
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("port", params[0]);
                jsonParam.put("protocol", params[1]);

                jsonPorts.add(jsonParam);
            }

            fwProps.put("ports", jsonPorts);

            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
