package com.qihang.controller.transferIn.admin.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class AdminShopTransferInVO implements Serializable {
    @ApiModelProperty("id")
    Integer id;

    @ApiModelProperty("shopId")
    Integer shopId;

    @ApiModelProperty("店名")
    String shopName;

    @ApiModelProperty("彩种ID")
    Integer lotteryType;

    @ApiModelProperty("彩种名称")
    String lotteryName;

    @ApiModelProperty("彩种LOGO")
    String icon;
    @ApiModelProperty(value = "彩种状态", notes = "0 收单开通，1停止收单")
    Integer states;
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    /**
     * 接单截止前多少分钟
     */
    @ApiModelProperty(value = "收单  截止前多少秒停止")
    Integer transferBeforeTime;


    @ApiModelProperty(value = "收单  返点")
    BigDecimal commiss;

    /**
     * '1 手动转单' 0 默认，自动转单;
     */
    @ApiModelProperty(value = "转单 自动0 1 手动")
    Integer transferOutAuto;

}
