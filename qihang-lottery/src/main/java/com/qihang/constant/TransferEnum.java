package com.qihang.constant;

public enum TransferEnum {
    TransferIn(0, "收单"),

    TransferOut(1, "转单");

    public Integer code;
    public String val;

    TransferEnum(Integer code, String val) {
        this.code = code;
        this.val = val;
    }
}
