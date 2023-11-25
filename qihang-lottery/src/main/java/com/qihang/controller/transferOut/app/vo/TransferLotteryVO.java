package com.qihang.controller.transferOut.app.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author: wyong
 * @Date: 2023/11/25 025 19:40
 * @Description:
 **/
@Data
public class TransferLotteryVO implements Serializable {
    Integer lotteryType;
    Integer transferBeforeTime;
    BigDecimal commiss;
}
