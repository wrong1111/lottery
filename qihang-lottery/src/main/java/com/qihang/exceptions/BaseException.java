package com.qihang.exceptions;


import lombok.Data;

@Data
public class BaseException extends Exception {

    String code;
    String msg;

    public BaseException() {
        super();
    }

    public BaseException(String code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
