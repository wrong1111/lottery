package com.qihang.common.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;


@Builder
@Data
public class BonuseVO implements Serializable {
    @Builder.Default
    Boolean shoted = false;//是否中奖
    @Builder.Default
    BigDecimal money = BigDecimal.ZERO;//本金
    @Builder.Default
    BigDecimal bonus = BigDecimal.ZERO;//奖金
}
