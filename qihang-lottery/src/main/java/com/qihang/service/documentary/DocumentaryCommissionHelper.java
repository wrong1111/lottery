package com.qihang.service.documentary;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qihang.common.util.order.OrderNumberGenerationUtil;
import com.qihang.domain.documentary.DocumentaryDO;
import com.qihang.domain.documentary.DocumentaryUserDO;
import com.qihang.domain.order.LotteryOrderDO;
import com.qihang.domain.order.PayOrderDO;
import com.qihang.domain.user.UserDO;
import com.qihang.enumeration.order.lottery.LotteryOrderStateEnum;
import com.qihang.enumeration.order.pay.PayOrderStateEnum;
import com.qihang.enumeration.order.pay.PayOrderTypeEnum;
import com.qihang.enumeration.order.pay.PayTypeEnum;
import com.qihang.mapper.documentary.DocumentaryMapper;
import com.qihang.mapper.documentary.DocumentaryUserMapper;
import com.qihang.mapper.order.PayOrderMapper;
import com.qihang.mapper.user.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;


@Slf4j
@Component
public class DocumentaryCommissionHelper {

    @Resource
    DocumentaryMapper documentaryMapper;

    @Resource
    UserMapper userMapper;

    @Resource
    PayOrderMapper payOrderMapper;

    @Resource
    DocumentaryUserMapper documentaryUserMapper;


    public void processCommiss(String name, LotteryOrderDO order, Double price) {

        //查询订单是不是发单订单
        DocumentaryDO documentary = documentaryMapper.selectOne(new QueryWrapper<DocumentaryDO>().lambda().eq(DocumentaryDO::getLotteryOrderId, order.getId()));
        //查询是否是跟单
        DocumentaryUserDO documentaryUser = documentaryUserMapper.selectOne(new QueryWrapper<DocumentaryUserDO>().lambda().eq(DocumentaryUserDO::getLotteryOrderId, order.getId()));

        if (ObjectUtil.isNotNull(documentary)) {
            log.debug("=======>[{}][已中奖]  订单 :[{}]  中奖【{}】是跟单发起人 [{}]  ", name, order.getOrderId(), price, documentary.getUserId());
            //是发单订单
            order.setState(LotteryOrderStateEnum.WAITING_AWARD.getKey());
            BigDecimal winPrice = NumberUtil.round(price, 2);
            order.setWinPrice(winPrice);
        } else if (ObjectUtil.isNotNull(documentaryUser)) {
            log.debug("=======>[{}][已中奖]  订单 :[{}]  中奖【{}】是跟单订单 跟单人[{}]  ", name, order.getOrderId(), price, documentaryUser.getUserId());
            //是跟单订单
            //查询跟单是那个用户的订单
            documentary = documentaryMapper.selectOne(new QueryWrapper<DocumentaryDO>().lambda().eq(DocumentaryDO::getId, documentaryUser.getDocumentaryId()));
            BigDecimal winPrice = NumberUtil.round(price, 2);
            //需要扣除比赛后的金额给发单用户，根据发单的设置的佣金比例来计算
            BigDecimal proportionPrice = winPrice.multiply(new BigDecimal((float) documentary.getCommission() / 100)).setScale(2, RoundingMode.HALF_UP);
            order.setState(LotteryOrderStateEnum.WAITING_AWARD.getKey());
            order.setWinPrice(winPrice.subtract(proportionPrice));
            log.debug("=======>[{}][已中奖]  订单 :[{}]  中奖【{}】是跟单订单 跟单人[{}],佣金[{}],分佣[{}]  ", name, order.getOrderId(), price, documentary.getUserId(), documentary.getCommission(), proportionPrice.toPlainString());
            //给发单用户加金额
            UserDO userDO = userMapper.selectById(documentary.getUserId());
            userDO.setGold(userDO.getGold().add(proportionPrice));
            userMapper.updateById(userDO);
            //添加流水记录
            PayOrderDO payOrder = new PayOrderDO();
            payOrder.setOrderId(OrderNumberGenerationUtil.getOrderId());
            payOrder.setType(PayOrderTypeEnum.ISSUING_REWARD.getKey());
            payOrder.setState(PayOrderStateEnum.PAYMENT.getKey());
            payOrder.setCreateTime(new Date());
            payOrder.setTenantId(order.getTenantId());
            payOrder.setUpdateTime(new Date());
            payOrder.setPayType(PayTypeEnum.APP.getKey());
            payOrder.setUserId(documentary.getUserId());
            payOrder.setPrice(proportionPrice);
            payOrderMapper.insert(payOrder);
        } else {
            log.debug("=======>[{}][中奖] 订单[{}] 已中奖[{}]  ", name, order.getOrderId(), price);
            //已经中奖
            order.setState(LotteryOrderStateEnum.WAITING_AWARD.getKey());
            order.setWinPrice(NumberUtil.round(price, 2));
        }
    }

}
