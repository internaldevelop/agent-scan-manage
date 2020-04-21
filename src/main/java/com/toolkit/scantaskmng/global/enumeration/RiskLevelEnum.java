package com.toolkit.scantaskmng.global.enumeration;

public enum RiskLevelEnum {
    NO_RISK(0),     // 无风险
    RISK_L1(1),     // 风险等级 L1
    RISK_L2(2),     // 风险等级 L2
    RISK_L3(3),     // 风险等级 L3
    RISK_L4(4),     // 风险等级 L4
    ;

    private int level;

    RiskLevelEnum(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
