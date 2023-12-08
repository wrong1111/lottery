package com.qihang.domain.order.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class TicketVO implements Serializable {

    String letBall;
    String match;
    String number;
    Integer id;
    String visitingTeam;
    String homeTeam;
    String mode; // 0 胜负过关  1 让球胜平负 2 上下单双 3总进球 4比分 5半全场
    // 竞猜足球 0 让球，1 胜平负，2进球，3半全场 4 比分
    //{"active":true,"index":0,"id":1,"describe":"胜","odds":"1.90"}
    List<TicketContentVO> ticketContentVOList;
}
