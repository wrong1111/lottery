package com.qihang.domain.transfer;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_order_transfer")
public class OrderTransferDO implements Serializable {
    Integer orderId;
    String orderNo;
    Integer shopId;
    Integer transferType;

    Integer sucFlag;
    String errorMsg;
    Integer recevFlag;

    Integer recevCounts;
    Date updateTime;
    Date recevTime;
    Date createTime;
}
