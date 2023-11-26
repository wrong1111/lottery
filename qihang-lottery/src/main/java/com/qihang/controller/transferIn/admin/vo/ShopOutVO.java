package com.qihang.controller.transferIn.admin.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ShopOutVO implements Serializable {

    @ApiModelProperty("id")
    Integer id;
    @ApiModelProperty("店名")
    String shopName;
}
