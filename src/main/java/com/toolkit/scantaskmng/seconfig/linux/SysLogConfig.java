package com.toolkit.scantaskmng.seconfig.linux;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.toolkit.scantaskmng.global.utils.MyUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;

@Component
public class SysLogConfig {
    // command: rsyslogd
    public JSONObject acquireSysLogConfig() {
        JSONObject logConfig = new JSONObject();

        if (!getSysLogConfig(logConfig))
            return logConfig;

        if (!getLogRotateConfig(logConfig))
            return logConfig;

        return logConfig;
    }

    // config: /etc/rsyslog.conf
    // rule 格式:
    // 日志设备(类型).日志级别   日志处理方式(action)
    //
    // 日志设备(可以理解为日志类型):
    // auth        –pam产生的日志
    // authpriv    –ssh,ftp等登录信息的验证信息
    // cron        –时间任务相关
    // kern        –内核
    // lpr         –打印
    // mail        –邮件
    // mark(syslog)–rsyslog服务内部的信息,时间标识
    // news        –新闻组
    // user        –用户程序产生的相关信息
    // uucp        –unix to unix copy, unix主机之间相关的通讯
    // local 1~7   –自定义的日志设备
    //
    // 日志级别:(从上到下，级别从低到高)
    // debug       –有调式信息的，日志信息最多
    // info        –一般信息的日志，最常用
    // notice      –最具有重要性的普通条件的信息
    // warning     –警告级别
    // err         –错误级别，阻止某个功能或者模块不能正常工作的信息
    // crit        –严重级别，阻止整个系统或者整个软件不能正常工作的信息
    // alert       –需要立刻修改的信息
    // emerg       –内核崩溃等严重信息
    // none        –什么都不记录
    private boolean getSysLogConfig(JSONObject logConfig) {
        try {
            String[] args = new String[] { "cat", "/etc/rsyslog.conf"};
            BufferedReader output = MyUtils.getExecOutput(args);

            String line;
            boolean bRule = false;
            JSONArray rulesArray = new JSONArray();
            while ((line = output.readLine()) != null) {
                if (line.contains("begin forwarding rule") && bRule) {
                    // finish the parsing of the rules, entering the forwarding rule
                    break;
                }
                if (!bRule) {
                    if (line.contains("#### RULES ####")) {
                        // prepare to enter the rules parsing
                        bRule = true;
                    }
                    // not rules line
                    continue;
                }

                // jump over the comment line AND blank line
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }

                // parse the rule line, just save the rule, hand over to server for parsing
                String[] rule = line.split(" +");
                JSONObject jsonRule = new JSONObject();
                jsonRule.put("content", rule[0]);
                jsonRule.put("action", rule[1]);

                rulesArray.add(jsonRule);
            }

            logConfig.put("rules", rulesArray);

            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // config: /etc/logrotate.conf
    private boolean getLogRotateConfig(JSONObject logConfig) {
        try {
            String[] args = new String[]{"cat", "/etc/logrotate.conf"};
            BufferedReader output = MyUtils.getExecOutput(args);

            String line;
            JSONObject rotate = new JSONObject();
            while ((line = output.readLine()) != null) {
                // maxDays unit: day
                if (line.startsWith("daily")) {
                    rotate.put("frequency", "daily");
                    rotate.put("maxDays", 1);
                } else if (line.startsWith("weekly")) {
                    rotate.put("frequency", "weekly");
                    rotate.put("maxDays", 7);
                } else if (line.startsWith("monthly")) {
                    rotate.put("frequency", "monthly");
                    rotate.put("maxDays", 31);
                } else if (line.startsWith("yearly")) {
                    rotate.put("frequency", "yearly");
                    rotate.put("maxDays", 366);
                } else if (line.startsWith("rotate")) {
                    String[] params = line.split(" +");
                    rotate.put("reservedFilesCount", params[1]);
                }
            }

            logConfig.put("rotate", rotate);

            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
