package com.qihang.controller.statistics.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class QueryDTO implements Serializable {

    @ApiModelProperty(value = "开始日期")
    String start;

    @ApiModelProperty(value = "开始日期")
    String end;
}
