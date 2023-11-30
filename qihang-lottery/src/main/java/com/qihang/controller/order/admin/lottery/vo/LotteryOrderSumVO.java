package com.qihang.controller.order.admin.lottery.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class LotteryOrderSumVO implements Serializable {
    Integer counts;
    BigDecimal price;


    Integer waitPrintCounts;
    BigDecimal waintPrintPrice;

    Integer waitAwardCounts;
    BigDecimal waitAwardPrice;

    Integer waitBounsCounts;
    BigDecimal waitBounsPrice;


    Integer awardCounts;
    BigDecimal awardPrice;
    BigDecimal awardBetPrice;

    Integer notAwardCounts;
    BigDecimal notAwardPrice;

    Integer backCounts;
    BigDecimal backPrice;
}
