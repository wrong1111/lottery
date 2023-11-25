package com.qihang.controller.transferOut.app.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.io.Serializable;


@Data
public class TransferDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 接口命令
     * getTransferInfo 获取转单信息（上游提供的返点。彩种，店铺名称，店铺ID,店铺LOGO，店铺联系人）
     * createOrder 创建订单
     * ping 测试接口
     */

    @NotNull(message = "action不能为空")
    String action;

    String data;

    @NotNull(message = "key不能为空")
    String key;

    @NotNull(message = "sign不能为空")
    @Length(message = "sign长度必须是{min}字符", min = 32, max = 32)
    String sign;

    @NotNull(message = "version不能为空")
    String version;

    @NotNull(message = "timestamp不能为空")
    Long timestamp;
}
