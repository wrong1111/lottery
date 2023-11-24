package com.qihang.controller.transferIn.admin.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class AddLotteryTransferInDTO implements Serializable {

    @ApiModelProperty("收单ID")
    @NotNull(message = "收单ID不能空")
    Integer shopId;

    @ApiModelProperty("彩种ID")
    @NotNull(message = "彩种ID不能空")
    Integer lotteryType;

    @ApiModelProperty("收单 提前时间")
    @NotNull(message = "不能空")
    @Min(value = 0, message = "最小不能负数")
    Integer beforeTime;

    @ApiModelProperty("状态 0 正常 1 暂停")
    @NotNull(message = "state不能空")
    Integer state;

    @ApiModelProperty("返点")
    @NotNull(message = "返点不能空")
    BigDecimal commiss;
}
