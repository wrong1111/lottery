package com.qihang.controller.statistics.vo;

import com.qihang.common.vo.BaseVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;


@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportVO extends BaseVO {
    private static final long serialVersionUID = 1L;

    Integer orderCounts = 0;
    BigDecimal orderMoney = BigDecimal.ZERO;
    Integer receiveCounts = 0;
    BigDecimal receiveMoney = BigDecimal.ZERO;
    Integer changeCounts = 0;
    BigDecimal changeMoney = BigDecimal.ZERO;
    Integer rechargeCounts = 0;
    BigDecimal rechargeMoney = BigDecimal.ZERO;
    Integer users = 0;
    BigDecimal allMoney = BigDecimal.ZERO;
    Integer drawCounts;
    BigDecimal drawMoney = BigDecimal.ZERO;
    BigDecimal revokePrice = BigDecimal.ZERO;
}
