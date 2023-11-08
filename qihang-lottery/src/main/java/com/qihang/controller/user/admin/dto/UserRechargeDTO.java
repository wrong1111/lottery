package com.qihang.controller.user.admin.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author: bright
 * @description:
 * @time: 2022-10-12 15:38
 */
@Data
public class UserRechargeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "0 加钱 1 减钱", required = true)
    @NotBlank(message = "类型不能为空")
    private String type;

    @ApiModelProperty(value = "金额", required = true)
    @NotNull(message = "金额不能为空")
    private BigDecimal price;
}
