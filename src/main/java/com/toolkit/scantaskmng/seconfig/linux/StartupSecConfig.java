package com.toolkit.scantaskmng.seconfig.linux;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.toolkit.scantaskmng.global.utils.MyUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
public class StartupSecConfig {
    private boolean seLinuxEnabled;
    private SELinuxModeEnum seLinuxMode;
    private SELinuxPolicyEnum seLinuxPolicy;

    public JSONObject acquireSeLinuxInfo() {
        try {
            String[] args = new String[]{ "sestatus" };
            BufferedReader output = MyUtils.getExecOutput(args);

            // Examples:
            //    SELinux status:                 enabled
            //    SELinuxfs mount:                /sys/fs/selinux
            //    SELinux root directory:         /etc/selinux
            //    Loaded policy name:             targeted
            //    Current mode:                   enforcing
            //    Mode from config file:          enforcing
            //    Policy MLS status:              enabled
            //    Policy deny_unknown status:     allowed
            //    Max kernel policy version:      31

            String line;
            while ((line = output.readLine()) != null) {
                String[] keyValue = line.split(":");
                if (keyValue[0].equals("Current mode")) {
                    // Current mode is the active mode of SELinux
                    if (keyValue[1].contains(SELinuxModeEnum.ENFORCING.getModeName()))
                        seLinuxMode = SELinuxModeEnum.ENFORCING;
                    else if (keyValue[1].contains(SELinuxModeEnum.PERMISSIVE.getModeName()))
                        seLinuxMode = SELinuxModeEnum.PERMISSIVE;
                    else if (keyValue[1].contains(SELinuxModeEnum.DISABLED.getModeName()))
                        seLinuxMode = SELinuxModeEnum.DISABLED;
                    else
                        seLinuxMode = SELinuxModeEnum.UNKNOWN;
                } else if (keyValue[0].equals("Loaded policy name")) {
                    // Judge the active policy
                    if (keyValue[1].contains(SELinuxPolicyEnum.TARGETED.getPolicyName()))
                        seLinuxPolicy = SELinuxPolicyEnum.TARGETED;
                    else if (keyValue[1].contains(SELinuxPolicyEnum.MINIMUM.getPolicyName()))
                        seLinuxPolicy = SELinuxPolicyEnum.MINIMUM;
                    else if (keyValue[1].contains(SELinuxPolicyEnum.MLS.getPolicyName()))
                        seLinuxPolicy = SELinuxPolicyEnum.MLS;
                    else
                        seLinuxPolicy = SELinuxPolicyEnum.UNKNOWN;
                } else if (keyValue[0].equals("SELinux status")) {
                    seLinuxEnabled = keyValue[1].contains("enabled");
                }
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        JSONObject seLinuxInfo = new JSONObject();
        seLinuxInfo.put("status", seLinuxEnabled);
        seLinuxInfo.put("mode", seLinuxMode.getMode());
        seLinuxInfo.put("modeName", seLinuxMode.getModeName());
        seLinuxInfo.put("policy", seLinuxPolicy.getPolicy());
        seLinuxInfo.put("policyName", seLinuxPolicy.getPolicyName());

//        JSONObject info = new JSONObject();
//        info.put("SELinux", seLinuxInfo);

        return seLinuxInfo;
    }

    public JSONArray acquireSelfRunServices() {
        JSONArray jsonServices = new JSONArray();
        try {
            String[] args = new String[]{ "chkconfig", "--list" };
            BufferedReader output = MyUtils.getExecOutput(args);

            String line;
            while ((line = output.readLine()) != null) {
                // Check the valid line containing the services
                if (line.isEmpty() || line.startsWith(" ") || !line.contains("0:"))
                    continue;

                // transfer multi-blank to single blank
                line = line.replaceAll(" +", " ");

                // split the info into service name and run level
                String[] service = line.split(" ");
                JSONObject jsonService = new JSONObject();
                jsonService.put("service", service[0]);
                jsonService.put("runLevel", service[1]);

                jsonServices.add(jsonService);
            }

            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return jsonServices;
    }


}
