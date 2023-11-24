package com.qihang.domain.transfer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_lottery_transfer")
public class LotteryTransferDO {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    Integer id;
    Integer shopId;
    Integer lotteryType;
    Integer states;
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * '0 设为接单' 1 转单;
     */
    Integer transferFlag;

    /**
     * 接单截止前多少分钟
     */
    Integer transferBeforeTime;

    /**
     * '1 手动转单' 0 默认，自动转单;
     */
    Integer transferOutAuto;

    BigDecimal commiss;
}
