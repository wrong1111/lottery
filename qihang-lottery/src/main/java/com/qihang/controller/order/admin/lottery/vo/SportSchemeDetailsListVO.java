package com.qihang.controller.order.admin.lottery.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class SportSchemeDetailsListVO implements Serializable {

    String type;
    //倍数
    String notes;
    String forecastBonus;
    String isShow;
    List<SportSchemeDetailsVO> ballCombinationList;
    String money;
    boolean award;

}
