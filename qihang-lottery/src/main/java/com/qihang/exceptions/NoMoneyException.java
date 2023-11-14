package com.qihang.exceptions;


import com.qihang.enumeration.error.ErrorCodeEnum;
import lombok.Data;

@Data
public class NoMoneyException extends Exception {

    String code = ErrorCodeEnum.E0763.getKey();
    String msg = ErrorCodeEnum.E0763.getValue();

    public NoMoneyException() {
        super();
    }

    public NoMoneyException(String code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
