package com.qihang.controller.transferIn.admin.dto;

import com.qihang.common.dto.PageDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class LotteryOutDTO extends PageDTO {
    @ApiModelProperty("彩种id")
    Integer lotteryId;

    @ApiModelProperty("商家id")
    Integer shopId;

    @ApiModelProperty("状态")
    Integer state;
}
