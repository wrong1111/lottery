package com.qihang.controller.transferIn.admin.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;


@Data
public class AdminShopTransferInDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    Integer id;

    @ApiModelProperty(value = "对外联系方式")
    @NotNull(message = "对外联系方式不能为空")
    String shopConcatPhone;

    @ApiModelProperty(value = "对外联系人")
    @NotNull(message = "对外联系人不能为空")
    String shopConcatName;

    @ApiModelProperty(value = "收单 接口秘钥")
    String transferSecurty;

    @ApiModelProperty(value = "店名")
    @NotNull(message = "店名不能为空")
    String shopName;

    @ApiModelProperty(value = "是否开通")
    @NotNull(message = "开通状态 不能为空")
    String interfaceState;

    @ApiModelProperty(value = "接口地址")
    String transferInterface;

    @ApiModelProperty(value = "接口 账号")
    String transferKey;
}
