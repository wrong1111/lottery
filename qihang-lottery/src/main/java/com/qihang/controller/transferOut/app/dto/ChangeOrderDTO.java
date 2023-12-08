package com.qihang.controller.transferOut.app.dto;

import com.qihang.domain.order.LotteryOrderDO;
import com.qihang.domain.order.LotteryTicketDO;
import com.qihang.domain.permutation.PermutationDO;
import com.qihang.domain.racingball.RacingBallDO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;


@Data
public class ChangeOrderDTO implements Serializable {

    Integer lotteryId;
    BigDecimal orderMoney;

    //订单信息
    LotteryOrderDO orderDO;

    // 赛事对阵选项
    List<RacingBallDO> racingBallDOList;

    //数据彩数据选项
    List<PermutationDO> permutationDOList;

    List<LotteryTicketDO> ticketDOList;
}
