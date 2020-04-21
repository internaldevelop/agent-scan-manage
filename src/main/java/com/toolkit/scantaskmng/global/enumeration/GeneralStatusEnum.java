package com.toolkit.scantaskmng.global.enumeration;

public enum GeneralStatusEnum {
    LOGICAL_DELETE(-99),
    INVALID(0),
    VALID(1),
    ;

    private int status;

    GeneralStatusEnum(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
