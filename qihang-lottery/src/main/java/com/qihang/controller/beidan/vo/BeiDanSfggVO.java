package com.qihang.controller.beidan.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
public class BeiDanSfggVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "属于哪天的比赛时间")
    private String startTime;

    @ApiModelProperty(value = "比赛数量")
    private Integer count;

    @ApiModelProperty(value = "比赛队伍信息")
    List<BeidanSfggMatchVO> beiDanMatchList;
}
