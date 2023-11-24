package com.qihang.controller.transferIn.admin.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;


@Data
public class AdminShopTransferInDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @NotNull(message = "ID不能为空")
    Integer id;

    @ApiModelProperty(value = "对外联系方式")
    @NotNull(message = "对外联系方式不能为空")
    String shopConcatPhone;

    @ApiModelProperty(value = "对外联系人")
    @NotNull(message = "对外联系人不能为空")
    String shopConcatName;

    @ApiModelProperty(value = "收单 接口秘钥")
    @NotNull(message = "收单 接口秘钥不能为空")
    @Size(min = 32, max = 32)
    String transferSecurty;

    @ApiModelProperty(value = "收单 接口地址")
    @NotNull(message = "收单 接口地址不能为空")
    String transferInterface;
}
