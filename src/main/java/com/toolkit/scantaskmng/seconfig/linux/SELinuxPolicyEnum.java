package com.toolkit.scantaskmng.seconfig.linux;

public enum SELinuxPolicyEnum {
    UNKNOWN(0, "unknown"),

    // Targeted processes are protected
    TARGETED(1, "targeted"),

    // Modification of targeted policy. Only selected processes are protected.
    MINIMUM(2, "minimum"),

    // Multi Level Security protection.
    MLS(3, "mls"),
    ;

    private int policy;
    private String policyName;

    SELinuxPolicyEnum(int policy, String policyName) {
        this.policy = policy;
        this.policyName = policyName;
    }

    public int getPolicy() {
        return policy;
    }

    public void setPolicy(int policy) {
        this.policy = policy;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }
}
