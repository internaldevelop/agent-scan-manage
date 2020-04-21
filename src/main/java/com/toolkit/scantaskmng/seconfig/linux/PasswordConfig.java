package com.toolkit.scantaskmng.seconfig.linux;

import com.alibaba.fastjson.JSONObject;
import com.toolkit.scantaskmng.global.utils.MyUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
public class PasswordConfig {
    public JSONObject acquirePasswordProps() {
        JSONObject pwdProps = new JSONObject();
        if (!getPasswordBaseProps(pwdProps))
            return null;

        if (!getPasswordComplexity(pwdProps))
            return null;

        return pwdProps;
    }

    private boolean getPasswordBaseProps(JSONObject pwdProps) {
        try {
            String[] args = new String[] { "sh", "-c", "cat /etc/login.defs | grep ^PASS_" };
            BufferedReader output = MyUtils.getExecOutput(args);

            // Examples:
            // PASS_MAX_DAYS	99999
            // PASS_MIN_DAYS	0
            // PASS_MIN_LEN	5
            // PASS_WARN_AGE	7
            String line;
            while ((line = output.readLine()) != null) {
                String[] params = line.split("\t");
                pwdProps.put(params[0], params[1]);
            }

            output.close();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean getPasswordComplexity(JSONObject pwdProps) {
        try {
            String[] args = new String[] { "sh", "-c", "cat /etc/pam.d/system-auth | grep password | grep requisite" };
            BufferedReader output = MyUtils.getExecOutput(args);

            // Examples:
            // password  requisite pam_cracklib.so retry=5  difok=3 minlen=10 ucredit=-1 lcredit=-3 dcredit=-3
            // dictpath=/usr/share/cracklib/pw_dict
            // 尝试次数：5  最少不同字符：3 最小密码长度：10  最少大写字母：1 最少小写字母：3 最少数字：3 密码字典：/usr/share/cracklib/pw_dict
            String line = output.readLine();
            if (line == null || line.isEmpty())
                return false;

            String[] params = line.split(" ");
            for (String param: params) {
                if (!param.contains("="))
                    continue;
                // add a tail, avoid cutting the tail elements
                param += "=end";
                String[] props = param.split("=");
                pwdProps.put("PASS_" + props[0], props[1]);
            }

            output.close();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
