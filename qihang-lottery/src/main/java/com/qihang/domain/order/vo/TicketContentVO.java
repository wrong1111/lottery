package com.qihang.domain.order.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class TicketContentVO implements Serializable {
    Boolean active;
    Integer index;
    Integer id;
    String describe;
    String odds;
    Boolean shoted;
    String letball;
    /**
     * 竞足  0 让球胜平负 1 胜平负 2 进球数 3半全场 4 比分
     * 竞蓝  0 胜负 1 让分胜负 2 胜分差 3 大小分
     * 北单 0 让球胜平负，1 总进球 2比分 3上下单双 4 半全场  5 胜负过关
     */
    String mode;
}
