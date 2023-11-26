package com.qihang.service.racingball;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qihang.common.util.order.OrderNumberGenerationUtil;
import com.qihang.common.vo.BaseVO;
import com.qihang.controller.basketball.dto.BasketballMatchDTO;
import com.qihang.controller.beidan.dto.BeiDanMatchDTO;
import com.qihang.controller.football.dto.FootballMatchDTO;
import com.qihang.controller.racingball.app.dto.BallCalculationDTO;
import com.qihang.controller.racingball.app.vo.RacingBallOrderVO;
import com.qihang.controller.winburden.dto.WinBurdenMatchDTO;
import com.qihang.domain.basketball.BasketballMatchDO;
import com.qihang.domain.beidan.BeiDanMatchDO;
import com.qihang.domain.football.FootballMatchDO;
import com.qihang.domain.order.LotteryOrderDO;
import com.qihang.domain.order.PayOrderDO;
import com.qihang.domain.racingball.RacingBallDO;
import com.qihang.domain.user.UserDO;
import com.qihang.enumeration.error.ErrorCodeEnum;
import com.qihang.enumeration.order.lottery.LotteryOrderTypeEnum;
import com.qihang.enumeration.order.pay.PayOrderStateEnum;
import com.qihang.enumeration.order.pay.PayOrderTypeEnum;
import com.qihang.enumeration.order.pay.PayTypeEnum;
import com.qihang.mapper.basketball.BasketballMatchMapper;
import com.qihang.mapper.beidan.BeiDanMatchMapper;
import com.qihang.mapper.football.FootballMatchMapper;
import com.qihang.mapper.order.LotteryOrderMapper;
import com.qihang.mapper.order.PayOrderMapper;
import com.qihang.mapper.racingball.RacingBallMapper;
import com.qihang.mapper.user.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author bright
 * @since 2022-10-24
 */
@Service
public class RacingBallServiceImpl extends ServiceImpl<RacingBallMapper, RacingBallDO> implements IRacingBallService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RacingBallMapper racingBallMapper;

    @Resource
    private LotteryOrderMapper orderMapper;

    @Resource
    private PayOrderMapper payOrderMapper;

    @Resource
    private FootballMatchMapper footballMatchMapper;

    @Resource
    private BasketballMatchMapper basketballMatchMapper;

    @Resource
    private BeiDanMatchMapper beiDanMatchMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseVO createOrder(BallCalculationDTO ballCalculation, Integer userId) {
        String issueNo = ballCalculation.getIssueNo();
        Date date = new Date();
        Date minDeadline = null;
        Map<Integer, Date> deadlineMap = new HashMap<>();
        if (StrUtil.equals(ballCalculation.getType(), LotteryOrderTypeEnum.FOOTBALL.getKey())) {
            List<FootballMatchDTO> footballMatchList = ballCalculation.getFootballMatchList();
            for (FootballMatchDTO footballMatchDTO : footballMatchList) {
                FootballMatchDO footballMatchDO = footballMatchMapper.selectById(footballMatchDTO.getId());
                if (deadlineMap.get(footballMatchDO.getId()) == null) {
                    deadlineMap.put(footballMatchDO.getId(), footballMatchDO.getDeadline());
                }
                if (date.after(footballMatchDO.getDeadline())) {
                    return new BaseVO(false, ErrorCodeEnum.E084.getKey(), ErrorCodeEnum.E084.getValue());
                }
                if (null == minDeadline || footballMatchDO.getDeadline().before(minDeadline)) {
                    minDeadline = footballMatchDO.getDeadline();
                }
            }
        } else if (StrUtil.equals(ballCalculation.getType(), LotteryOrderTypeEnum.BASKETBALL.getKey())) {
            List<BasketballMatchDTO> basketballMatchList = ballCalculation.getBasketballMatchList();
            for (BasketballMatchDTO basketballMatchDTO : basketballMatchList) {
                BasketballMatchDO basketballMatchDO = basketballMatchMapper.selectById(basketballMatchDTO.getId());
                if (deadlineMap.get(basketballMatchDO.getId()) == null) {
                    deadlineMap.put(basketballMatchDO.getId(), basketballMatchDO.getDeadline());
                }
                if (date.after(basketballMatchDO.getDeadline())) {
                    return new BaseVO(false, ErrorCodeEnum.E084.getKey(), ErrorCodeEnum.E084.getValue());
                }
                if (null == minDeadline || basketballMatchDO.getDeadline().before(minDeadline)) {
                    minDeadline = basketballMatchDO.getDeadline();
                }
            }
        } else if (StrUtil.equals(ballCalculation.getType(), LotteryOrderTypeEnum.SINGLE.getKey())) {
            List<BeiDanMatchDTO> beiDanMatchList = ballCalculation.getBeiDanMatchList();
            for (BeiDanMatchDTO beiDanMatchDTO : beiDanMatchList) {
                BeiDanMatchDO beiDanMatchDO = beiDanMatchMapper.selectById(beiDanMatchDTO.getId());
                if (date.after(beiDanMatchDO.getDeadline())) {
                    return new BaseVO(false, ErrorCodeEnum.E084.getKey(), ErrorCodeEnum.E084.getValue());
                }
            }
        }

        RacingBallOrderVO racingBallOrder = new RacingBallOrderVO();
        PayOrderDO payOrder = new PayOrderDO();
        //总金额
        BigDecimal price = BigDecimal.valueOf(ballCalculation.getNotes() * 2 * ballCalculation.getMultiple());
        //计算用户的账号是否充足
        UserDO user = userMapper.selectById(userId);
        if (price.compareTo(user.getGold()) == 1) {
            if (price.compareTo(user.getGold().add(user.getPrice())) == 1) {
                return new BaseVO(false, ErrorCodeEnum.E0763.getKey(), ErrorCodeEnum.E0763.getValue());
            }
        }
        if (price.compareTo(user.getGold()) == 1) {
            //直接扣除彩金
            BigDecimal remainingPrice = price.subtract(user.getGold());
            //彩金直接设置为0
            user.setGold(new BigDecimal(0));
            //剩下的扣除奖金余额
            user.setPrice(user.getPrice().subtract(remainingPrice));
            userMapper.updateById(user);
        } else {
            //直接扣除彩金
            user.setGold(user.getGold().subtract(price));
            userMapper.updateById(user);
        }
        //批量添加投注数据
        List<Integer> ids = new ArrayList<>();
        //判断是足球还是篮球还是北单
        if (StrUtil.equals(ballCalculation.getType(), LotteryOrderTypeEnum.FOOTBALL.getKey())) {
            for (FootballMatchDTO footballMatch : ballCalculation.getFootballMatchList()) {
                RacingBallDO racingBall = new RacingBallDO();
                racingBall.setUserId(userId);
                racingBall.setTargetId(footballMatch.getId());
                racingBall.setCreateTime(new Date());
                racingBall.setUpdateTime(new Date());
                racingBall.setNotes(ballCalculation.getNotes());
                racingBall.setTimes(ballCalculation.getMultiple());
                racingBall.setType(StrUtil.join(",", ballCalculation.getPssTypeList()));
                racingBall.setContent(JSONUtil.toJsonStr(footballMatch));
                racingBall.setGameNo(getMatchGameNo(footballMatch.getNumber(), deadlineMap.get(footballMatch.getId())));
                racingBallMapper.insert(racingBall);
                ids.add(racingBall.getId());
            }
            payOrder.setType(PayOrderTypeEnum.FOOTBALL.getKey());
            //篮球
        } else if (StrUtil.equals(ballCalculation.getType(), LotteryOrderTypeEnum.BASKETBALL.getKey())) {
            for (BasketballMatchDTO basketballMatch : ballCalculation.getBasketballMatchList()) {
                RacingBallDO racingBall = new RacingBallDO();
                racingBall.setUserId(userId);
                racingBall.setTargetId(basketballMatch.getId());
                racingBall.setCreateTime(new Date());
                racingBall.setUpdateTime(new Date());
                racingBall.setNotes(ballCalculation.getNotes());
                racingBall.setTimes(ballCalculation.getMultiple());
                racingBall.setType(StrUtil.join(",", ballCalculation.getPssTypeList()));
                racingBall.setContent(JSONUtil.toJsonStr(basketballMatch));
                racingBall.setGameNo(getMatchGameNo(basketballMatch.getNumber(), deadlineMap.get(basketballMatch.getId())));
                racingBallMapper.insert(racingBall);
                ids.add(racingBall.getId());
            }
            payOrder.setType(PayOrderTypeEnum.BASKETBALL.getKey());
            //北单
        } else if (StrUtil.equals(ballCalculation.getType(), LotteryOrderTypeEnum.SINGLE.getKey())) {
            for (BeiDanMatchDTO beiDanMatch : ballCalculation.getBeiDanMatchList()) {
                RacingBallDO racingBall = new RacingBallDO();
                racingBall.setUserId(userId);
                racingBall.setTargetId(beiDanMatch.getId());
                racingBall.setCreateTime(new Date());
                racingBall.setUpdateTime(new Date());
                racingBall.setNotes(ballCalculation.getNotes());
                racingBall.setTimes(ballCalculation.getMultiple());
                racingBall.setType(StrUtil.join(",", ballCalculation.getPssTypeList()));
                racingBall.setContent(JSONUtil.toJsonStr(beiDanMatch));
                racingBall.setGameNo(issueNo + fillZero(beiDanMatch.getNumber(), 4));
                racingBallMapper.insert(racingBall);
                ids.add(racingBall.getId());
            }
            payOrder.setType(PayOrderTypeEnum.SINGLE.getKey());
        } else if (StrUtil.equals(ballCalculation.getType(), LotteryOrderTypeEnum.VICTORY_DEFEAT.getKey())) {
            for (WinBurdenMatchDTO winBurdenMatch : ballCalculation.getWinBurdenMatchList()) {
                RacingBallDO racingBall = new RacingBallDO();
                racingBall.setUserId(userId);
                racingBall.setTargetId(winBurdenMatch.getId());
                racingBall.setCreateTime(new Date());
                racingBall.setUpdateTime(new Date());
                racingBall.setNotes(ballCalculation.getNotes());
                racingBall.setTimes(ballCalculation.getMultiple());
                racingBall.setType(StrUtil.join(",", ballCalculation.getPssTypeList()));
                racingBall.setContent(JSONUtil.toJsonStr(winBurdenMatch));
                racingBall.setGameNo(issueNo + fillZero(winBurdenMatch.getNumber(), 3));
                racingBallMapper.insert(racingBall);
                ids.add(racingBall.getId());
            }
            payOrder.setType(PayOrderTypeEnum.VICTORY_DEFEAT.getKey());
        } else if (StrUtil.equals(ballCalculation.getType(), LotteryOrderTypeEnum.REN_JIU.getKey())) {
            for (WinBurdenMatchDTO winBurdenMatch : ballCalculation.getWinBurdenMatchList()) {
                RacingBallDO racingBall = new RacingBallDO();
                racingBall.setUserId(userId);
                racingBall.setTargetId(winBurdenMatch.getId());
                racingBall.setCreateTime(new Date());
                racingBall.setUpdateTime(new Date());
                racingBall.setNotes(ballCalculation.getNotes());
                racingBall.setTimes(ballCalculation.getMultiple());
                racingBall.setType(StrUtil.join(",", ballCalculation.getPssTypeList()));
                racingBall.setContent(JSONUtil.toJsonStr(winBurdenMatch));
                racingBall.setGameNo(issueNo + fillZero(winBurdenMatch.getNumber(), 3));
                racingBallMapper.insert(racingBall);
                ids.add(racingBall.getId());
            }
            payOrder.setType(PayOrderTypeEnum.REN_JIU.getKey());
        }
        //添加钱包消费记录
        payOrder.setOrderId(OrderNumberGenerationUtil.getOrderId());
        payOrder.setState(PayOrderStateEnum.PAID.getKey());
        payOrder.setCreateTime(new Date());
        payOrder.setUpdateTime(new Date());
        payOrder.setPayType(PayTypeEnum.APP.getKey());
        payOrder.setUserId(userId);
        payOrder.setPrice(price);
        payOrderMapper.insert(payOrder);

        //创建订单
        LotteryOrderDO order = new LotteryOrderDO();
        order.setOrderId(OrderNumberGenerationUtil.getOrderId());
        order.setUserId(userId);
        order.setPrice(price);
        order.setSchemeDetails(ballCalculation.getSchemeDetails());
        order.setTargetIds(StrUtil.join(",", ids));
        order.setType(ballCalculation.getType());
        order.setForecast(ballCalculation.getForecast());
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        order.setTimes(ballCalculation.getMultiple());
        orderMapper.insert(order);
        racingBallOrder.setId(order.getId());
        return racingBallOrder;
    }

    public static String fillZero(String str, int len) {
        if (str.length() >= len) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len - str.length(); i++) {
            sb.append("0");
        }
        sb.append(str);
        return sb.toString();
    }

    public static String[] WEEK_LIST = new String[]{"周日", "周一", "周二", "周三", "周四", "周五", "周六"};

    public static String getDeadline(Date deadline) {
        String date = DateUtil.format(deadline, "yyyyMMdd");
        Date matchDate = DateUtil.parse(date + " 100001", "yyyyMMddHHmmss");
        String gameNo = date;
        if (deadline.before(matchDate)) {
            gameNo = DateUtil.format(DateUtils.addDays(deadline, -1), "yyyyMMdd");
        }
        return gameNo;
    }

    public static String getMatchGameNo(String matchNo, Date deadline) {
        String week = matchNo.substring(0, 2);
        String no = matchNo.substring(2);
        String gameNo = getDeadline(deadline);
        switch (week) {
            case "周一":
                gameNo += "1" + no;
                break;
            case "周二":
                gameNo += "2" + no;
                break;
            case "周三":
                gameNo += "3" + no;
                break;
            case "周四":
                gameNo += "4" + no;
                break;
            case "周五":
                gameNo += "5" + no;
                break;
            case "周六":
                gameNo += "6" + no;
                break;
            default:
                gameNo += "7" + no;
                break;
        }
        return gameNo;
    }
}
