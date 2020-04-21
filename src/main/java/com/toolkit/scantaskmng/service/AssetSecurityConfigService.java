package com.toolkit.scantaskmng.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.toolkit.scantaskmng.global.bean.ResponseBean;
import com.toolkit.scantaskmng.global.enumeration.ErrorCodeEnum;
import com.toolkit.scantaskmng.global.response.ResponseHelper;
import com.toolkit.scantaskmng.global.utils.MyUtils;
import com.toolkit.scantaskmng.global.utils.SystemUtils;
import com.toolkit.scantaskmng.seconfig.linux.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@Component
public class AssetSecurityConfigService {
    @Autowired
    private ResponseHelper responseHelper;

    @Autowired
    private StartupSecConfig startupSecConfig;
    @Autowired
    private AccountSecConfig accountSecConfig;
    @Autowired
    private PasswordConfig passwordConfig;
    @Autowired
    private SysServiceConfig serviceConfig;
    @Autowired
    private FirewallConfig firewallConfig;
    @Autowired
    private IptablesConfig iptablesConfig;
    @Autowired
    private LoginSecureConfig loginSecureConfig;
    @Autowired
    private SysLogConfig sysLogConfig;

    public ResponseBean fetchSecurityConfig(String types) {
        // 不支持Windows系统安全配置采集
        if (SystemUtils.isWindows())
            return responseHelper.error(ErrorCodeEnum.ERROR_WIN_NO_SEC_CONFIG);

        boolean all = false;
        List<String> typeList = null;
        if (types.isEmpty()) {
            // if types has not assigned, means all the security config are needed.
            all = true;
        } else {
            typeList = Arrays.asList(types.split(","));
        }

        JSONObject jsonResp = new JSONObject();

        // Startup security config
        if (all || typeList.contains("startup")) {
            jsonResp.put("SELinux", startupSecConfig.acquireSeLinuxInfo());
            jsonResp.put("selfRunServices", startupSecConfig.acquireSelfRunServices());
        }

        // Account security config
        if (all || typeList.contains("account")) {
            jsonResp.put("accounts", accountSecConfig.acquireAccountProps());
            jsonResp.put("groups", accountSecConfig.acquireGroupProps());
        }

        // Password security config
        if (all || typeList.contains("password")) {
            jsonResp.put("passwordProps", passwordConfig.acquirePasswordProps());
        }

        // System service config
        if (all || typeList.contains("service")) {
            jsonResp.put("sshConfig", serviceConfig.acquireSSHProps());
            jsonResp.put("firewall", firewallConfig.acquireFirewallProps());
            jsonResp.put("iptables", iptablesConfig.acquireIptablesProps());
        }

        // Login security config
        if (all || typeList.contains("login")) {
            jsonResp.put("login", loginSecureConfig.acquireLoginProps());
        }

        // iptables config
        if (all || typeList.contains("iptables")) {
            jsonResp.put("iptablesConfig", iptablesConfig.acquireIptablesRules());
        }

        // system log config
        if (all || typeList.contains("syslog")) {
            jsonResp.put("syslog", sysLogConfig.acquireSysLogConfig());
        }

        return responseHelper.success(jsonResp);
    }

    public ResponseBean setUserAccountPwd(String account, String password) {
        // 不支持Windows系统安全配置采集
        if (SystemUtils.isWindows())
            return responseHelper.error(ErrorCodeEnum.ERROR_WIN_NO_SEC_CONFIG);

        // 如果账户不存在，先创建账户
        if (!isAccountExist(account)) {
            if (!createAccount(account)) {
                responseHelper.error(ErrorCodeEnum.ERROR_FAIL_LINUX_CREATE_ACCOUNT);
            }
        }

        // 修改账户的密码
        if (!setAccountPwd(account, password)) {
            responseHelper.error(ErrorCodeEnum.ERROR_FAIL_LINUX_UPDATE_PASSWD);
        }

        return responseHelper.success();
    }

    private boolean isAccountExist(String account) {
        try {
            // 查找指定账户
            String command = String.format("cat /etc/passwd | grep %s", account);
            String[] args = new String[]{"sh", "-c", command};
            BufferedReader output = MyUtils.getExecOutput(args);

            // 从输出结果中确认账户是否存在
            String line;
            while ((line = output.readLine()) != null) {
                if (line.startsWith(account)) {
                    output.close();
                    return true;
                }
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private boolean createAccount(String account) {
        try {
            // 创建账户
            String[] args = new String[]{"useradd", account};
            BufferedReader output = MyUtils.getExecOutput(args);

            // 输出结果为空表示账户创建成功，否则说明已有账户，创建失败
            String line;
            while ((line = output.readLine()) != null) {
                output.close();
                return false;
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean setAccountPwd(String account, String password) {
        try {
            // 修改账户密码
            String[] args = new String[]{"passwd", account};
            Process proc = Runtime.getRuntime().exec(args);

            // 等待输入新密码
            Scanner sc = new Scanner(System.in);
            OutputStream out = proc.getOutputStream();
            out.write((password+"\n").getBytes());
            out.flush();

            // 等待再次输入新密码
            Scanner sc2 = new Scanner(System.in);
            out.write((password+"\n").getBytes());
            out.flush();
            out.close();

            // 检查密码修改的结果
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream(), SystemUtils.getEnvEncoding()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("successfully")) {
                    reader.close();
                    return true;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
}
