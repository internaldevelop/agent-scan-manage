package com.toolkit.scantaskmng.global.enumeration;

public enum SysLogLevelEnum {
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
    DEBUG(1, "debug"),
    INFO(2, "info"),
    NOTICE(3, "notice"),
    WARNING(4, "warning"),
    ERROR(5, "err"),
    CRITICAL(6, "crit"),
    ALERT(7, "alert"),
    EMERGENCY(8, "emerg"),
    NONE(100, "none"),
    ;

    private int level;
    private String name;

    SysLogLevelEnum(int level, String name) {
        this.level = level;
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
