package com.qihang.domain.transfer;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_lottery_transfer_log")
@Builder
public class LotteryTransferLogDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    Integer id;

    Integer shopId;
    String orderId;
    /**
     * 0 默认接收  1 转单出去
     */
    String type;
    String content;
    String receveMsg;
    Date createTime;

}
