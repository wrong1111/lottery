package com.qihang.controller.beidan.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author bright
 * @since 2022-10-05
 */
@Data
@Accessors(chain = true)
public class BeiDanMatchDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "编号")
    private String number;

    @ApiModelProperty(value = "赛事")
    private String match;

    @ApiModelProperty(value = "让球")
    private String letBall;

    @ApiModelProperty(value = "让球赔率")
    private List<Map<String, Object>> letOddsList = new ArrayList<>();

    @ApiModelProperty(value = "单双赔率")
    private List<Map<String, Object>> oddEvenOdds = new ArrayList<>();

    @ApiModelProperty(value = "总进球赔率")
    private List<Map<String, Object>> goalOddsList = new ArrayList<>();

    @ApiModelProperty(value = "半全场赔率")
    private List<Map<String, Object>> halfWholeOddsList = new ArrayList<>();

    @ApiModelProperty(value = "比分赔率")
    private List<Map<String, Object>> scoreOddsList = new ArrayList<>();


    @ApiModelProperty(value = "胜负过关赔率")
    private List<Map<String, Object>> sfggOdds = new ArrayList<>();

    @ApiModelProperty(value = "主队")
    private String homeTeam;

    @ApiModelProperty(value = "客队")
    private String visitingTeam;
}
