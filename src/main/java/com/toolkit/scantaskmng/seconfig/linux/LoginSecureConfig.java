package com.toolkit.scantaskmng.seconfig.linux;

import com.alibaba.fastjson.JSONObject;
import com.toolkit.scantaskmng.global.utils.MyUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;

@Component
public class LoginSecureConfig {
    public JSONObject acquireLoginProps() {
        JSONObject loginProps = new JSONObject();
        // First get the login props from /etc/pam.d/login
        getAuthProps(loginProps, "login");

        // Maybe /etc/pam.d/login has no setting, then get them from system-auth
        getAuthProps(loginProps, "system-auth");

        return loginProps;
    }

    // /etc/pam.d/login OR /etc/pam.d/system-auth
    // auth     required       pam_tally2.so deny=6 even_deny_root unlock_time=1200 root_unlock_time=1200
    private boolean getAuthProps(JSONObject loginProps, String module) {
        String command = String.format("cat /etc/pam.d/%s | grep ^auth | grep pam_tally2.so", module);
        try {
            String[] args = new String[] { "sh", "-c", command };
            BufferedReader output = MyUtils.getExecOutput(args);

            String line = output.readLine();
            if (line == null || line.isEmpty())
                return false;

            String[] params = line.split(" +");
            for (String param : params) {
                if (param.equalsIgnoreCase("even_deny_root")) {
                    loginProps.put(param, true);
                    continue;
                } else if (param.contains("=")) {
                    String[] props = param.split("=");
                    loginProps.put(props[0], props[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
