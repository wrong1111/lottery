package com.qihang.controller.transferIn.admin.dto;

import com.qihang.common.dto.PageDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class AdminPlatDTO extends PageDTO {

    @ApiModelProperty(value = "ID")
    Integer id;
    @ApiModelProperty(value = "对外店名")
    String shopName;

    @ApiModelProperty(value = "对外联系方式")
    String shopConcatPhone;

    @ApiModelProperty(value = "对外联系人")
    String shopConcatName;

    @ApiModelProperty(value = "收单 下游的余额")
    BigDecimal money;

    @ApiModelProperty(value = "收单 下游的应付余额")
    BigDecimal golds;

    @ApiModelProperty(value = "0 收单 1 转单")
    Integer transferType;

    @ApiModelProperty(value = "收单 接口key")
    String transferKey;

    @ApiModelProperty(value = "收单 接口秘钥")
    String transferSecurty;

    @ApiModelProperty(value = "收单 接口地址")
    String transferInterface;

    @ApiModelProperty(value = "转单  上游接口状态")
    String interfaceState;

    Date createTime;

    @ApiModelProperty(value = "转单  上游接口状态最后更新时间 ")
    Date updateTime;

    @ApiModelProperty(value = "一键获取地址")
    String gateinfo;

    @ApiModelProperty(value = "店家对应账户ID")
    Integer uid;
}
