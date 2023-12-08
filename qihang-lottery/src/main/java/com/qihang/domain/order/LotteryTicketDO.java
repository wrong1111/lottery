package com.qihang.domain.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_lottery_ticket")
public class LotteryTicketDO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    Integer id;
    String orderId;// '订单ID' ,
    String ticketNo;//'票号编号',
    BigDecimal forecast;// '单票预计最大奖金',
    Integer times;//  '倍数',
    BigDecimal price;//  '票金额(包含倍数)',
    Integer bets;//  '此票注数',
    BigDecimal winPrice;//  '中奖金额',
    Integer state;//  '票状态 与订单状态同步 0 默认 待出票 1 待开奖 2 未中奖 3 待派奖 4 已中奖 5 拒绝 6.退票',
    Integer ticketState;//  '0 待出票 1 已出票 2 退票 3 部分退票',
    String ticketContent;//  '票内容 场次，主队，让球，客队 选项 赔率',
    Date ticketingTime;//  '出票时间',
    Date revokeTime;//  '退票时间',
    BigDecimal revokePrice;//  '退票金额',
    String bill;//  '票据',
    Date createTime;

    String betType;//串过关玩法
    Integer type;//彩种
    String matchs;
}
