package com.toolkit.scantaskmng.global.enumeration;

public enum SysLogTypeEnum {
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
    AUTH(1, "auth"),
    AUTH_PRIV(2, "authpriv"),
    CRON(3, "cron"),
    KERN(4, "kern"),
    LPR(5, "lpr"),
    MAIL(6, "mail"),
    MARK(7, "mark"),
    NEWS(8, "news"),
    USER(9,"user"),
    UUCP(10, "uucp"),
    LOCAL1(11, "local1"),
    LOCAL2(12, "local2"),
    LOCAL3(13, "local3"),
    LOCAL4(14, "local4"),
    LOCAL5(15, "local5"),
    LOCAL6(16, "local6"),
    LOCAL7(17, "local7"),
    ;

    private int type;
    private String name;

    SysLogTypeEnum(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
