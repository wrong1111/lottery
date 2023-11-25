package com.qihang.domain.transfer;

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
@TableName("t_shop_transfer")
public class ShopTransferDO implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    Integer id;

    String shopName;
    String shopConcatPhone;
    String shopConcatName;
    BigDecimal money;
    BigDecimal golds;
    Integer transferType;
    String transferKey;
    String transferSecurty;
    String transferInterface;
    String interfaceState;
    Date createTime;
    Date updateTime;
    Long uid;

}
