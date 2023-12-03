package com.qihang.controller.order.admin.lottery.vo;

import lombok.Data;

import java.io.Serializable;


@Data
public class SportSchemeDetailsVO implements Serializable {


    String number;
    String homeTeam;
    String visitingTeam;
    String content;
    // {"number":"周二004","homeTeam":"[2]斯托克港","visitingTeam":"[1]博尔顿","content":"让平(4.2)"
    String letball;
}
