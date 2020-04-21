package com.toolkit.scantaskmng.global.enumeration;

public enum TaskRunStatusEnum {
    IDLE(1),            // 空闲
    RUNNING(2),         // 任务执行中
    FINISHED(3),        // 任务完成
    INTERRUPTED(4),     // 任务中断
    ;

    private int status;

    TaskRunStatusEnum(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
