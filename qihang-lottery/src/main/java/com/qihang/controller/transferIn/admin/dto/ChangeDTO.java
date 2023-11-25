package com.qihang.controller.transferIn.admin.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Author: wyong
 * @Date: 2023/11/25 025 21:00
 * @Description:
 **/
@Data
public class ChangeDTO implements Serializable {


    @ApiModelProperty("地址")
    @NotNull(message = "url不能为空") String url;

}
