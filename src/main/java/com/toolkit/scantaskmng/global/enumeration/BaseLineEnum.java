package com.toolkit.scantaskmng.global.enumeration;

public enum BaseLineEnum {
    UNKNOWN(0),
    WINDOWS(1),
    LINUX(2),
    ;
    private int type;

    BaseLineEnum(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
