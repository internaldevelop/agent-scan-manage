package com.toolkit.scantaskmng.seconfig.linux;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.toolkit.scantaskmng.global.utils.MyUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;

@Component
public class IptablesConfig {
    // iptables rules file:
    // Centos: /etc/sysconfig/iptables
    // UbuntuL: /etc/iptables.rules
    public JSONObject acquireIptablesProps() {
        JSONObject iptablesProps = new JSONObject();
        boolean active = MyUtils.checkServiceActive("iptables");
        iptablesProps.put("active", active);

        if (active) {
            if (!getOpenPorts(iptablesProps))
                return iptablesProps;
        }

        return iptablesProps;
    }

    public JSONObject acquireIptablesRules() {
        JSONObject iptablesRules = new JSONObject();
        boolean active = MyUtils.checkServiceActive("iptables");
        iptablesRules.put("active", active);

        if (active) {
            if (!getChainRules(iptablesRules, "INPUT"))
                return iptablesRules;
            if (!getChainRules(iptablesRules, "FORWARD"))
                return iptablesRules;
            if (!getChainRules(iptablesRules, "OUTPUT"))
                return iptablesRules;
        }

        return iptablesRules;

    }

    private boolean getChainRules(JSONObject iptablesProps, String chain) {
        try {
            String[] args = new String[] { "iptables", "-nL", chain };
            BufferedReader output = MyUtils.getExecOutput(args);

            JSONArray rulesArray = new JSONArray();
            String line;
            while ((line = output.readLine()) != null) {
                JSONObject jsonRule = new JSONObject();
                String[] params = line.split(" +");
                if (params == null || params.length < 5)
                    continue;
                // parse REJECT target
                if ( params[0].equals("REJECT") || params[0].equals("ACCEPT") ) {
                    jsonRule.put("target", params[0]);
                    jsonRule.put("protocol", params[1]);
                    jsonRule.put("source", params[3]);
                    jsonRule.put("destination", params[4]);
                    if (params.length > 6) {
                        if (params[5].equals("reject-with")) {
                            // reject and return packet type
                            jsonRule.put(params[5], params[6]);
                        } else if (params.length >= 9 && params[8].contains("dpt")) {
                            // port or ports scope
                            String[] portParams = params[8].split(":");
                            if (portParams[0].equals("dpt")) {
                                jsonRule.put("port", portParams[1]);
                            } else if (portParams[0].equals("dpts")) {
                                jsonRule.put("portStart", portParams[1]);
                                jsonRule.put("portEnd", portParams[2]);
                            }
                        }
                    }

                    rulesArray.add(jsonRule);
                }

            }

            iptablesProps.put("Chain " + chain, rulesArray);

            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    // 查看INPUT chain开放的端口
    // iptalbles -nL
    private boolean getOpenPorts(JSONObject iptablesProps) {
        try {
            String[] args = new String[] { "iptables", "-nL" };
            BufferedReader output = MyUtils.getExecOutput(args);

            JSONArray portsArray = new JSONArray();
            String line;
            while ((line = output.readLine()) != null) {
                if (!line.startsWith("ACCEPT"))
                    continue;
                String[] params = line.split(" +");
                if (params.length >= 9 && params[6].equals("NEW") && params[8].contains(":")) {
                    JSONObject jsonPort = new JSONObject();
                    jsonPort.put("target", params[0]);
                    jsonPort.put("protocol", params[1]);
                    String[] port = params[8].split(":");
                    jsonPort.put("port", port[1]);

                    portsArray.add(jsonPort);
                }
            }

            iptablesProps.put("ports", portsArray);

            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
