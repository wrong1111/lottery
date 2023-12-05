package com.qihang.service.statistics;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qihang.common.vo.BaseDataVO;
import com.qihang.controller.statistics.vo.ReportVO;
import com.qihang.controller.statistics.vo.StatisticsVO;
import com.qihang.domain.order.LotteryOrderDO;
import com.qihang.domain.order.PayOrderDO;
import com.qihang.domain.user.UserDO;
import com.qihang.domain.withdrawal.WithdrawalDO;
import com.qihang.enumeration.order.lottery.LotteryOrderStateEnum;
import com.qihang.enumeration.order.pay.PayOrderStateEnum;
import com.qihang.enumeration.order.pay.PayOrderTypeEnum;
import com.qihang.enumeration.withdrawal.ExamineEnum;
import com.qihang.mapper.order.LotteryOrderMapper;
import com.qihang.mapper.order.PayOrderMapper;
import com.qihang.mapper.user.UserMapper;
import com.qihang.mapper.withdrawal.WithdrawalMapper;
import org.apache.catalina.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: bright
 * @description:
 * @time: 2022-11-06 13:46
 */
@Service
public class IStatisticsServiceImpl implements IStatisticsService {

    @Resource
    private LotteryOrderMapper lotteryOrderMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private WithdrawalMapper withdrawalMapper;

    @Resource
    private PayOrderMapper payOrderMapper;


    @Override
    public StatisticsVO calculation() {
        List<Integer> agentList = userMapper.selectList(new QueryWrapper<UserDO>().lambda().eq(UserDO::getIsAgent, "1")).stream().map(UserDO::getId).collect(Collectors.toList());
        StatisticsVO statistics = new StatisticsVO();
        //待出票数量
        Long ticketingCount = lotteryOrderMapper.selectCount(new QueryWrapper<LotteryOrderDO>().lambda().eq(LotteryOrderDO::getState, LotteryOrderStateEnum.TO_BE_ISSUED.getKey()));
        Long awardCount = lotteryOrderMapper.selectCount(new QueryWrapper<LotteryOrderDO>().lambda().eq(LotteryOrderDO::getState, LotteryOrderStateEnum.WAITING_AWARD.getKey()));
        Long withdrawalCount = withdrawalMapper.selectCount(new QueryWrapper<WithdrawalDO>().lambda().eq(WithdrawalDO::getState, ExamineEnum.UNAPPROVED.getKey()));
        Long userCount = userMapper.selectCount(null);
        Date start = DateUtil.parse(DateUtil.today() + " 00:00:00");
        Date end = DateUtil.parse(DateUtil.today() + " 23:59:59");
        LambdaQueryWrapper<UserDO> qw = new QueryWrapper<UserDO>().lambda()
                .ge(UserDO::getCreateTime, start)
                .lt(UserDO::getCreateTime, end);
        List<UserDO> userList = userMapper.selectList(qw);
        Long registerCount = Long.valueOf(userList.size());
        Long realCount = userList.stream().filter(user -> user.getIsReal().equals("1")).count();
        List<Map<String, Object>> maps = userMapper.selectMaps(new QueryWrapper<UserDO>().select("sum(gold) as totalGold,sum(price) as totalPrice"));
        BigDecimal totalPrice = new BigDecimal(maps.get(0).get("totalGold").toString()).add(new BigDecimal(maps.get(0).get("totalPrice").toString()));
        QueryWrapper<PayOrderDO> payOrderQueryWrapper = new QueryWrapper<>();
        payOrderQueryWrapper.select("sum(price) as totalPrice");
        payOrderQueryWrapper.lambda().ge(PayOrderDO::getCreateTime, start);
        payOrderQueryWrapper.lambda().le(PayOrderDO::getCreateTime, end);
        payOrderQueryWrapper.lambda().eq(PayOrderDO::getState, PayOrderStateEnum.PAID.getKey());
        payOrderQueryWrapper.lambda().eq(PayOrderDO::getType, PayOrderTypeEnum.RECHARGE.getKey());
        payOrderQueryWrapper.lambda().notIn(PayOrderDO::getUserId, agentList);
        List<Map<String, Object>> mapList = payOrderMapper.selectMaps(payOrderQueryWrapper);
        BigDecimal rechargePrice = new BigDecimal(ObjectUtil.isNull(mapList.get(0)) ? "0" : mapList.get(0).get("totalPrice").toString());
        QueryWrapper<WithdrawalDO> withdrawalQueryWrapper = new QueryWrapper<>();
        withdrawalQueryWrapper.select("sum(amount) as totalPrice");
        withdrawalQueryWrapper.lambda().ge(WithdrawalDO::getUpdateTime, start);
        withdrawalQueryWrapper.lambda().le(WithdrawalDO::getUpdateTime, end);
        withdrawalQueryWrapper.lambda().eq(WithdrawalDO::getState, ExamineEnum.PAID.getKey());
        withdrawalQueryWrapper.lambda().notIn(WithdrawalDO::getUserId, agentList);
        List<Map<String, Object>> withdrawalMapList = withdrawalMapper.selectMaps(withdrawalQueryWrapper);
        BigDecimal withdrawalPrice = new BigDecimal(ObjectUtil.isNull(withdrawalMapList.get(0)) ? "0" : withdrawalMapList.get(0).get("totalPrice").toString());
        List<Map<String, Object>> lotteryOrderMapList = lotteryOrderMapper.selectMaps(new QueryWrapper<LotteryOrderDO>().select("sum(win_price) as totalPrice").lambda()
                .eq(LotteryOrderDO::getState, LotteryOrderStateEnum.WAITING_AWARD.getKey())
                .notIn(LotteryOrderDO::getUserId, agentList));
        BigDecimal awardPrice = new BigDecimal(ObjectUtil.isNull(lotteryOrderMapList.get(0)) ? "0" : lotteryOrderMapList.get(0).get("totalPrice").toString());
        List<Map<String, Object>> orderMapList = lotteryOrderMapper.selectMaps(new QueryWrapper<LotteryOrderDO>().select("sum(price) as totalPrice").lambda()
                .ge(LotteryOrderDO::getCreateTime, start)
                .le(LotteryOrderDO::getCreateTime, end)
                .notIn(LotteryOrderDO::getUserId, agentList)
                .ne(LotteryOrderDO::getState, LotteryOrderStateEnum.REFUSE.getKey())
                .ne(LotteryOrderDO::getState, LotteryOrderStateEnum.REFUND.getKey()));
        BigDecimal bettingPrice = new BigDecimal(ObjectUtil.isNull(orderMapList.get(0)) ? "0" : orderMapList.get(0).get("totalPrice").toString());

        List<Map<String, Object>> loMapList = lotteryOrderMapper.selectMaps(new QueryWrapper<LotteryOrderDO>().select("sum(win_price) as totalPrice").lambda()
                .eq(LotteryOrderDO::getState, LotteryOrderStateEnum.ALREADY_AWARD.getKey())
                .notIn(LotteryOrderDO::getUserId, agentList));
        BigDecimal alreadyAwardPrice = new BigDecimal(ObjectUtil.isNull(loMapList.get(0)) ? "0" : loMapList.get(0).get("totalPrice").toString());


        //查询昨天的下注金额和派奖金额
        start = DateUtil.offsetDay(start, -1);
        end = DateUtil.offsetDay(end, -1);
        List<Map<String, Object>> yesterdayBettingPriceMap = lotteryOrderMapper.selectMaps(new QueryWrapper<LotteryOrderDO>()
                .select("sum(price) as totalPrice")
                .lambda()
                .ge(LotteryOrderDO::getCreateTime, start)
                .le(LotteryOrderDO::getCreateTime, end)
                .ne(LotteryOrderDO::getState, LotteryOrderStateEnum.REFUSE.getKey())
                .ne(LotteryOrderDO::getState, LotteryOrderStateEnum.REFUND.getKey())
                .notIn(LotteryOrderDO::getUserId, agentList));
        BigDecimal yesterdayBettingPrice = new BigDecimal(ObjectUtil.isNull(yesterdayBettingPriceMap.get(0)) ? "0" : yesterdayBettingPriceMap.get(0).get("totalPrice").toString());

        List<Map<String, Object>> yesterdayAwardPriceMap = lotteryOrderMapper.selectMaps(new QueryWrapper<LotteryOrderDO>().select("sum(win_price) as totalPrice")
                .lambda()
                .ge(LotteryOrderDO::getCreateTime, start)
                .le(LotteryOrderDO::getCreateTime, end)
                .notIn(LotteryOrderDO::getUserId, agentList)
                .and(lq -> lq.eq(LotteryOrderDO::getState, LotteryOrderStateEnum.WAITING_AWARD.getKey())
                        .or()
                        .eq(LotteryOrderDO::getState, LotteryOrderStateEnum.ALREADY_AWARD.getKey())));
        BigDecimal yesterdayAwardPrice = new BigDecimal(ObjectUtil.isNull(yesterdayAwardPriceMap.get(0)) ? "0" : yesterdayAwardPriceMap.get(0).get("totalPrice").toString());

        //总提现金额
        List<Map<String, Object>> yesterdayBettingPriceMaps = withdrawalMapper.selectMaps(new QueryWrapper<WithdrawalDO>()
                .select("sum(amount) as totalPrice")
                .lambda()
                .eq(WithdrawalDO::getState, "1")
                .notIn(WithdrawalDO::getUserId, agentList));

        BigDecimal s1 = new BigDecimal(ObjectUtil.isNull(yesterdayBettingPriceMaps.get(0)) ? "0" : yesterdayBettingPriceMaps.get(0).get("totalPrice").toString());

        //查找总共充值金额
        List<Map<String, Object>> totalMaps = payOrderMapper.selectMaps(new QueryWrapper<PayOrderDO>().select("sum(price) as totalPrice")
                .lambda().eq(PayOrderDO::getType, PayOrderTypeEnum.RECHARGE.getKey())
                .eq(PayOrderDO::getState, PayOrderStateEnum.PAID.getKey())
                .notIn(PayOrderDO::getUserId, agentList));
        BigDecimal s3 = new BigDecimal(ObjectUtil.isNull(totalMaps.get(0)) ? "0" : totalMaps.get(0).get("totalPrice").toString());

        //总投注
        List<Map<String, Object>> totalBettingPriceMaps = lotteryOrderMapper.selectMaps(new QueryWrapper<LotteryOrderDO>()
                .select("sum(price) as totalPrice")
                .lambda()
                .ne(LotteryOrderDO::getState, "5").ne(LotteryOrderDO::getState, "6")
                .notIn(LotteryOrderDO::getUserId, agentList));
        BigDecimal s2 = new BigDecimal(ObjectUtil.isNull(totalBettingPriceMaps.get(0)) ? "0" : totalBettingPriceMaps.get(0).get("totalPrice").toString());


        //总充值
        statistics.setTotalRechargePrice(s3);

        //总投注
        statistics.setTotalBetting(s2);

        //总盈利
        statistics.setTotalProfitPrice(s3.subtract(s1));

        //昨日派奖金额
        statistics.setYesterdayAwardPrice(yesterdayAwardPrice);
        //昨日盈利金额
        statistics.setYesterdayProfitPrice(yesterdayBettingPrice.subtract(yesterdayAwardPrice));
        //昨日投注金额
        statistics.setYesterdayBettingPrice(yesterdayBettingPrice);
        statistics.setTicketingCount(ticketingCount);

        statistics.setAwardCount(awardCount);
        statistics.setWithdrawalCount(withdrawalCount);
        statistics.setUserCount(userCount);
        statistics.setRegisterCount(registerCount);
        statistics.setRealCount(realCount);
        statistics.setTotalPrice(totalPrice);
        statistics.setRechargePrice(rechargePrice);
        statistics.setWithdrawalPrice(withdrawalPrice);
        statistics.setAwardPrice(awardPrice);
        statistics.setBettingPrice(bettingPrice);
        statistics.setAlreadyAwardPrice(alreadyAwardPrice);
        return statistics;
    }

    @Override
    public Map<String, Object> daGet(String start, String end) {
        Map<String, Object> allMap = new HashMap<>();
        //订单 相关的数据项
        String[] sqlColum = new String[]{"sum(1) orderCounts", "sum(price) orderMoney",
                "sum(case when revoke_price is not null then revoke_price else 0 end ) revokePrice",
                "sum(case when transfer_type = 0 then 1 else 0 end ) receiveCounts",
                "sum(case when transfer_type = 0 then price else 0 end ) receiveMoney",
                "sum(case when transfer_type = 1 then 1 else 0 end ) changeCounts",
                "sum(case when transfer_type = 1 then price else 0 end ) changeMoney",
        };
        LambdaQueryWrapper<LotteryOrderDO> qw = new QueryWrapper<LotteryOrderDO>()
                .select(sqlColum).lambda();
        qw.between(LotteryOrderDO::getCreateTime,
                DateUtil.parse(start, DateUtil.newSimpleFormat("yyyy-MM-dd")),
                DateUtils.addDays(DateUtil.parse(end, DateUtil.newSimpleFormat("yyyy-MM-dd")), 1));
        List<Map<String, Object>> resultList = lotteryOrderMapper.selectMaps(qw);
        if (!CollectionUtil.isEmpty(resultList)) {
            Map<String, Object> paymentMap = resultList.get(0);
            if (null != paymentMap) {
                allMap.putAll(paymentMap);
            }
        }

        //充值
        String[] sqlColumnPay = new String[]{"sum(case when type =0 and state = 1 then 1 else 0 end  ) rechargeCounts ",
                "sum(case when type =0 and state = 1 then price else 0 end ) rechargeMoney "};
        LambdaQueryWrapper<PayOrderDO> payQw = new QueryWrapper<PayOrderDO>()
                .select(sqlColumnPay).lambda();
        payQw.between(PayOrderDO::getCreateTime,
                DateUtil.parse(start, DateUtil.newSimpleFormat("yyyy-MM-dd")),
                DateUtils.addDays(DateUtil.parse(end, DateUtil.newSimpleFormat("yyyy-MM-dd")), 1));
        List<Map<String, Object>> payOrderList = payOrderMapper.selectMaps(payQw);
        if (CollectionUtil.isNotEmpty(payOrderList)) {
            Map<String, Object> payOrderMap = payOrderList.get(0);
            if (null != payOrderMap) {
                allMap.putAll(payOrderMap);
            }
        }

        //提现
        String[] sqlColumWithraw = new String[]{
                " sum(case when state in (0,1) then 1 else 0 end) drawCounts ", "sum(case when state in (0,1) then amount else 0 end) drawMoney "
        };
        LambdaQueryWrapper<WithdrawalDO> withrawQw = new QueryWrapper<WithdrawalDO>()
                .select(sqlColumWithraw).lambda();
        withrawQw.between(WithdrawalDO::getCreateTime,
                DateUtil.parse(start, DateUtil.newSimpleFormat("yyyy-MM-dd")),
                DateUtils.addDays(DateUtil.parse(end, DateUtil.newSimpleFormat("yyyy-MM-dd")), 1));
        List<Map<String, Object>> withrawList = withdrawalMapper.selectMaps(withrawQw);
        if (CollectionUtil.isNotEmpty(withrawList)) {
            Map<String, Object> withrawMap = withrawList.get(0);
            if (null != withrawMap) {
                allMap.putAll(withrawMap);
            }
        }

        //会员
        String[] sqlColumnUser = new String[]{"count(1) users", "sum(gold+price) allMoney "};
        LambdaQueryWrapper<UserDO> userQw = new QueryWrapper<UserDO>()
                .select(sqlColumnUser).lambda();
//        userQw.between(UserDO::getCreateTime,
//                DateUtil.parse(start, DateUtil.newSimpleFormat("yyyy-MM-dd")),
//                DateUtils.addDays(DateUtil.parse(end, DateUtil.newSimpleFormat("yyyy-MM-dd")), 1));
        List<Map<String, Object>> userMap = userMapper.selectMaps(userQw);
        if (CollectionUtil.isNotEmpty(userMap)) {
            Map<String, Object> user = userMap.get(0);
            if (null != user) {
                allMap.putAll(user);
            }
        }
        return allMap;
    }

}
