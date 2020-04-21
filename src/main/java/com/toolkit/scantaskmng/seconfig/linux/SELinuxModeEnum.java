package com.toolkit.scantaskmng.seconfig.linux;

public enum SELinuxModeEnum {
    // Default: unknown
    UNKNOWN(0, "unknown"),

    // SELinux security policy is enforced.
    ENFORCING(1, "enforcing"),

    // SELinux prints warnings instead of enforcing.
    PERMISSIVE(2, "permissive"),

    // No SELinux policy is loaded.
    DISABLED(3, "disabled"),
    ;

    private int mode;
    private String modeName;

    private SELinuxModeEnum(int mode, String modeName) {
        this.mode = mode;
        this.modeName = modeName;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getModeName() {
        return modeName;
    }

    public void setModeName(String modeName) {
        this.modeName = modeName;
    }
}
