package com.toolkit.scantaskmng.seconfig.linux;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.bcel.internal.generic.RET;
import com.toolkit.scantaskmng.global.bean.ResponseBean;
import com.toolkit.scantaskmng.global.enumeration.ErrorCodeEnum;
import com.toolkit.scantaskmng.global.response.ResponseHelper;
import com.toolkit.scantaskmng.global.utils.MyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

@Component
public class AccountSecConfig {
    @Autowired private ResponseHelper responseHelper;

    public JSONArray acquireAccountProps() {
        JSONArray jsonAccounts = new JSONArray();
        // passwd
        if (!readPasswdRecords(jsonAccounts))
            return null;

        // shadow
        if (!readShadowRecords(jsonAccounts))
            return null;

        return jsonAccounts;
    }

    public JSONArray acquireGroupProps() {
        JSONArray jsonGroups = new JSONArray();
        // group
        if (!readGroupRecords(jsonGroups))
            return null;

        return jsonGroups;
    }

    private boolean readPasswdRecords(JSONArray jsonAccounts) {
        try {
            // Output the passwd file contents
            String[] args = new String[] { "cat", "/etc/passwd" };

            // read all of the accounts passwd parameters
            BufferedReader output = MyUtils.getExecOutput(args);

            // Examples:
            // tcpdump:x:72:72::/:/sbin/nologin
            // ybz:x:1000:1000:ybz:/home/ybz:/bin/bash
            // wyt:x:1001:1001::/home/wyt:/bin/bash
            // mysql:x:27:27:MySQL Server:/var/lib/mysql:/bin/false
            // Format:
            // [account]:[password]:[UID]:[GID]:[comment]:[home]:[shell]
            String line;
            while ((line = output.readLine()) != null) {
                // add a tail, avoid cutting the tail elements
                line += ":end";
                String[] params = line.split(":");
                JSONObject accObject = getAccount(jsonAccounts, params[0]);
                accObject.put("password", params[1]);
                accObject.put("UID", params[2]);
                accObject.put("GID", params[3]);
                accObject.put("comment", params[4]);
                accObject.put("home", params[5]);
                accObject.put("shell", params[6]);
            }

            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean readShadowRecords(JSONArray jsonAccounts) {
        try {
            // Output the shadow file contents
            String[] args = new String[] { "cat", "/etc/shadow" };

            // Read all of the account shadow parameters
            BufferedReader output = MyUtils.getExecOutput(args);

            // Examples:
            // sshd:!!:17813::::::
            // oprofile:!!:17813::::::
            // ybz:$6$dN7GpMsoKTP/GJhX$gO58HL/8.lpzgWODRHAEa3FhiMlJCt5n9Nt3vxZupQ.SFd3iYJPQS8B.BjmZME00st02ztWGA.UF3wL38zefJ.::0:99999:7:::
            // wyt:$6$idbI/.DD$oZe1aVThkhk/HEoYMJkTBpNWQW4Rix/4G/7/NpxJuNkjoWknNl.eEuKGCDWj9JNnxkmknwUh/AKaRD.N9o1CY/:17837:0:99999:7:::
            // mysql:!!:17837::::::
            // Format:
            // [account]:[encrypted_pwd]:[last_modified_time]:[pwd_unchange_days]:[pwd_live_days]:[pwd_tip_days]:
            // [pwd_expire_days]:[acc_expire_time]:[reserved]
            String line;
            while ((line = output.readLine()) != null) {
                // add a tail, avoid cutting the tail elements
                line += ":end";
                String[] params = line.split(":");
                JSONObject accObject = getAccount(jsonAccounts, params[0]);
                accObject.put("encrypted_pwd", params[1]);
                accObject.put("last_modified_time", params[2]);
                accObject.put("pwd_unchange_days", params[3]);
                accObject.put("pwd_live_days", params[4]);
                accObject.put("acc_expire_time", params[7]);
            }

            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean readGroupRecords(JSONArray jsonGroups) {
        try {
            // Output the group file contents
            String[] args = new String[] { "cat", "/etc/group" };

            // Read all of the groups records
            BufferedReader output = MyUtils.getExecOutput(args);

            // Examples:
            // wyt:x:1001:
            // mysql:x:27:
            // docker:x:981:wyt
            // Format:
            // [group]:[group_pwd]:[GID]:[account1[,account2]]
            String line;
            while ((line = output.readLine()) != null) {
                // add a tail, avoid cutting the tail elements
                line += ":end";
                String[] params = line.split(":");
                JSONObject group = new JSONObject();
                group.put("group", params[0]);
                group.put("group_pwd", params[1]);
                group.put("GID", params[2]);
                group.put("accounts", params[3]);

                jsonGroups.add(group);
            }

            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private JSONObject getAccount(JSONArray jsonAccounts, String account) {
        JSONObject jsonAcc = null;
        for (Iterator iter = jsonAccounts.iterator(); iter.hasNext(); ) {
            // Search account in the list
            jsonAcc = (JSONObject) iter.next();
            if (jsonAcc.getString("account").equals(account))
                return jsonAcc;
        }

        // if not existed in the list, create a new object to store the account
        jsonAcc = new JSONObject();
        jsonAcc.put("account", account);

        // add this new account in the list
        jsonAccounts.add(jsonAcc);

        return jsonAcc;
    }
}
