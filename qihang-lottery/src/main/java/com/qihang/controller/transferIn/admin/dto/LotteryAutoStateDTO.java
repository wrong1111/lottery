package com.qihang.controller.transferIn.admin.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class LotteryAutoStateDTO implements Serializable {
    @ApiModelProperty("id")
    @NotNull(message = "ID不能为空")
    Integer id;

    @ApiModelProperty("转单状态  1 手动 0 自动")
    @NotNull(message = "转单状态 不能为空")
    Integer states;
}
