package com.qihang.service.transfer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qihang.annotation.TenantIgnore;
import com.qihang.common.util.SpringContextUtils;
import com.qihang.common.util.order.OrderNumberGenerationUtil;
import com.qihang.common.vo.BaseDataVO;
import com.qihang.common.vo.BaseVO;
import com.qihang.constant.TransferEnum;
import com.qihang.controller.order.admin.lottery.vo.SportSchemeDetailsListVO;
import com.qihang.controller.shop.app.vo.ShopVO;
import com.qihang.controller.transferOut.app.dto.ChangeOrderDTO;
import com.qihang.controller.transferOut.app.vo.TransferLotteryVO;
import com.qihang.domain.basketball.BasketballMatchDO;
import com.qihang.domain.beidan.BeiDanMatchDO;
import com.qihang.domain.football.FootballMatchDO;
import com.qihang.domain.order.LotteryOrderDO;
import com.qihang.domain.order.PayOrderDO;
import com.qihang.domain.permutation.PermutationAwardDO;
import com.qihang.domain.permutation.PermutationDO;
import com.qihang.domain.racingball.RacingBallDO;
import com.qihang.domain.shop.ShopDO;
import com.qihang.domain.transfer.LotteryTransferDO;
import com.qihang.domain.transfer.ShopTransferDO;
import com.qihang.domain.user.UserDO;
import com.qihang.domain.winburden.WinBurdenMatchDO;
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
import com.qihang.mapper.permutation.PermutationAwardMapper;
import com.qihang.mapper.permutation.PermutationMapper;
import com.qihang.mapper.racingball.RacingBallMapper;
import com.qihang.mapper.shop.ShopMapper;
import com.qihang.mapper.transfer.LotteryTransferMapper;
import com.qihang.mapper.user.UserMapper;
import com.qihang.mapper.winburden.WinBurdenMatchMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: wyong
 * @Date: 2023/11/25 025 19:38
 * @Description:
 **/
@Slf4j
@Service
public class ITransferOutServiceImpl implements ITransferOutService {


    @Resource
    LotteryTransferMapper lotteryTransferMapper;


    @Resource
    ShopMapper shopMapper;

    @Resource
    UserMapper userMapper;

    @Resource
    IShopTransferService shopTransferService;
    @Resource
    LotteryOrderMapper lotteryOrderMapper;

    @Resource
    FootballMatchMapper footballMatchMapper;

    @Resource
    BasketballMatchMapper basketballMatchMapper;

    @Resource
    BeiDanMatchMapper beiDanMatchMapper;

    @Resource
    WinBurdenMatchMapper winBurdenMatchMapper;

    @Resource
    RacingBallMapper racingBallMapper;

    @Resource
    PermutationMapper permutationMapper;

    @Resource
    PermutationAwardMapper permutationAwardMapper;

    @Resource
    PayOrderMapper payOrderMapper;

    @TenantIgnore
    @Override
    public BaseVO listLottery(String key) {
        BaseDataVO baseVO = BaseDataVO.builder().build();
        ShopTransferDO shopTransferDO = shopTransferService.findShopTransfer(key);
        if (null == shopTransferDO) {
            baseVO.setSuccess(false);
            baseVO.setErrorMsg("未开通，请联系商家");
            return baseVO;
        }

        List<LotteryTransferDO> list = lotteryTransferMapper.selectList(new QueryWrapper<LotteryTransferDO>().lambda().eq(LotteryTransferDO::getStates, 0));
        if (CollectionUtils.isEmpty(list)) {
            return BaseDataVO.builder().data(Collections.EMPTY_LIST).build();
        }
        List<TransferLotteryVO> lotteryVOS = BeanUtil.copyToList(list, TransferLotteryVO.class);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("lotterys", lotteryVOS);

        //  ShopTransferDO shopTransferDO  = TransferServiceImpl.SHOP_TRANSFER_MAP.get(key);

        //当前店的信息
        ShopDO shopDO = shopMapper.selectById(1);
        ShopVO shopVO = new ShopVO();
        BeanUtil.copyProperties(shopDO, shopVO);

        resultMap.put("shop", shopVO);
        resultMap.put("url", shopTransferDO.getTransferInterface());
        return BaseDataVO.builder().data(resultMap).build();
    }


    @TenantIgnore
    @Override
    public BaseVO getAccountMoney(String key) {
        ShopTransferDO shopTransferDO = TransferServiceImpl.SHOP_TRANSFER_MAP.get(key);
        UserDO userDO = userMapper.selectOne(new QueryWrapper<UserDO>().lambda().eq(UserDO::getUid, shopTransferDO.getUid()));
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("gold", userDO.getGold());
        resultMap.put("price", userDO.getPrice());
        resultMap.put("total", userDO.getPrice().add(userDO.getGold()));
        return BaseDataVO.builder().data(resultMap).build();
    }


    @Override
    public BaseVO createOrder(String data, String key) {
        BaseVO baseVO = new BaseVO();
        ShopTransferDO shopTransferDO = TransferServiceImpl.SHOP_TRANSFER_MAP.get(key);
        UserDO userDO = userMapper.selectOne(new QueryWrapper<UserDO>().lambda().eq(UserDO::getUid, shopTransferDO.getUid()));
        if (null == userDO) {
            baseVO.setSuccess(false);
            baseVO.setErrorMsg("账户异常，请联系管理员");
            return baseVO;
        }
        ChangeOrderDTO changeOrderDTO = JSON.parseObject(data, ChangeOrderDTO.class);
        ITransferOutService transferOutService = SpringContextUtils.getBean(ITransferOutService.class);
        if (isSports(changeOrderDTO.getLotteryId())) {
            return transferOutService.createSportOrder(changeOrderDTO.getOrderDO(), changeOrderDTO.getRacingBallDOList(), key);
        } else {
            return transferOutService.createDigitOrder(changeOrderDTO.getOrderDO(), changeOrderDTO.getPermutationDOList(), key);
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @TenantIgnore
    @Override
    public BaseVO createSportOrder(LotteryOrderDO lotteryOrderDO, List<RacingBallDO> racingBallDOList, String key) {
        Integer lotteryId = Integer.valueOf(lotteryOrderDO.getType());
        //判断此账户开通接单 与否
        ShopTransferDO shopTransferDO = TransferServiceImpl.SHOP_TRANSFER_MAP.get(key);
        if (null == shopTransferDO) {
            return BaseVO.builder().success(false).errorCode("-1").errorMsg("未开通，请联系商家").build();
        }
        if (!"0".equals(shopTransferDO.getInterfaceState())) {
            return BaseVO.builder().success(false).errorCode("-1").errorMsg("暂停收单，请联系商家").build();
        }
        UserDO userDO = userMapper.selectById(shopTransferDO.getUid());
        if (null == userDO) {
            return BaseVO.builder().success(false).errorCode("-1").errorMsg("账号异常，请联系商家").build();
        }
        //修改订单发起人为 账户对应的会员账户uid
        lotteryOrderDO.setUserId(userDO.getId());
        lotteryOrderDO.setTenantId(1);
        lotteryOrderDO.setCreateTime(new Date());
        lotteryOrderDO.setState("0");
        lotteryOrderDO.setWinPrice(BigDecimal.ZERO);
        lotteryOrderDO.setWinCounts(0);
        lotteryOrderDO.setTicketingTime(null);
        lotteryOrderDO.setBill(null);
        lotteryOrderDO.setId(null);
        lotteryOrderDO.setTransferShopId(shopTransferDO.getId());
        lotteryOrderDO.setTransferType(TransferEnum.TransferIn.code);//收单
        //判断此彩种是否收单
        LotteryTransferDO lotteryTransferDOS = lotteryTransferMapper.selectOne(new QueryWrapper<LotteryTransferDO>().lambda().eq(LotteryTransferDO::getLotteryType, lotteryId).eq(LotteryTransferDO::getTransferFlag, TransferEnum.TransferIn.code));
        if (null == lotteryTransferDOS || lotteryTransferDOS.getStates() != 0) {
            return BaseVO.builder().success(false).errorCode("-1").errorMsg("彩种暂停收单，请联系商家").build();
        }
        //判断此账户对应的余额是否足够支付
        BigDecimal money = BigDecimal.ZERO;
        List<SportSchemeDetailsListVO> listVOList = JSONUtil.toList(lotteryOrderDO.getSchemeDetails(), SportSchemeDetailsListVO.class);
        money = listVOList.stream().map(sportSchemeDetailsListVO -> BigDecimal.valueOf(Integer.valueOf(sportSchemeDetailsListVO.getNotes()))).reduce(BigDecimal::add).get().multiply(new BigDecimal(2));
        if (money.compareTo(lotteryOrderDO.getPrice()) != 0) {
            return BaseVO.builder().success(false).errorCode("-1").errorMsg("投注金额【" + lotteryOrderDO.getPrice().toPlainString() + "】与实际不符【" + money.toPlainString() + "】").build();
        }
        //判断此单是否已经下过单
        String orderNo = key + lotteryOrderDO.getOrderId();
        lotteryOrderDO.setOrderId(orderNo);
        if (lotteryOrderMapper.selectCount(new QueryWrapper<LotteryOrderDO>().lambda().eq(LotteryOrderDO::getOrderId, orderNo)) > 0) {
            return BaseVO.builder().success(false).errorCode("1").errorMsg("此单已经下过单，请勿重复下单").build();
        }
        Date now = new Date();
        //判断此单是否已经超过最后下单时间
        int beforeTimes = lotteryTransferDOS.getTransferBeforeTime();//提前
        //调整订单 内容的对阵ID。
        //足球与篮球，根据gameno来判断哪场赛事，北单按期 号+场次
        if (LotteryOrderTypeEnum.FOOTBALL.getKey().equals(lotteryOrderDO.getType())) {
            List<String> gameNoList = racingBallDOList.stream().map(racingBallDO -> racingBallDO.getGameNo()).collect(Collectors.toList());
            List<FootballMatchDO> footballMatchDOS = footballMatchMapper.selectList(new QueryWrapper<FootballMatchDO>().lambda().in(FootballMatchDO::getGameNo, gameNoList));
            Map<String, FootballMatchDO> footballMatchDOMap = footballMatchDOS.stream().collect(Collectors.toMap(FootballMatchDO::getGameNo, footballMatchDO -> footballMatchDO, (b, c) -> b));
            for (RacingBallDO racingBallDO : racingBallDOList) {
                FootballMatchDO footballMatchDO = footballMatchDOMap.get(racingBallDO.getGameNo());
                if (null != footballMatchDO) {
                    racingBallDO.setTargetId(footballMatchDO.getId());
                } else {
                    return BaseVO.builder().success(false).errorCode("-1").errorMsg("未找到匹配赛事" + racingBallDO.getGameNo()).build();
                }
                if (DateUtils.addSeconds(now, beforeTimes).getTime() > footballMatchDO.getDeadline().getTime()) {
                    return BaseVO.builder().success(false).errorCode("-1").errorMsg("赛事" + racingBallDO.getGameNo() + " 已到截止时间，不再收单").build();
                }
            }
        } else if (LotteryOrderTypeEnum.BASKETBALL.getKey().equals(lotteryOrderDO.getType())) {
            List<String> gameNoList = racingBallDOList.stream().map(racingBallDO -> racingBallDO.getGameNo()).collect(Collectors.toList());
            List<BasketballMatchDO> basketballMatchDOS = basketballMatchMapper.selectList(new QueryWrapper<BasketballMatchDO>().lambda().in(BasketballMatchDO::getGameNo, gameNoList));
            Map<String, BasketballMatchDO> basketballMatchDOMap = basketballMatchDOS.stream().collect(Collectors.toMap(BasketballMatchDO::getGameNo, a -> a, (b, c) -> b));
            for (RacingBallDO racingBallDO : racingBallDOList) {
                BasketballMatchDO basketballMatchDO = basketballMatchDOMap.get(racingBallDO.getGameNo());
                if (null != basketballMatchDO) {
                    racingBallDO.setTargetId(basketballMatchDO.getId());
                } else {
                    return BaseVO.builder().success(false).errorCode("-1").errorMsg("未找到匹配赛事" + racingBallDO.getGameNo()).build();
                }
                if (DateUtils.addSeconds(now, beforeTimes).getTime() > basketballMatchDO.getDeadline().getTime()) {
                    return BaseVO.builder().success(false).errorCode("-1").errorMsg("赛事" + racingBallDO.getGameNo() + " 已到截止时间，不再收单").build();
                }
            }
        } else if (LotteryOrderTypeEnum.SINGLE.getKey().equals(lotteryOrderDO.getType())) {
            List<String> gameNoList = racingBallDOList.stream().map(racingBallDO -> racingBallDO.getGameNo()).collect(Collectors.toList());
            List<BeiDanMatchDO> beiDanMatchDOS = beiDanMatchMapper.selectList(new QueryWrapper<BeiDanMatchDO>().lambda().in(BeiDanMatchDO::getGameNo, gameNoList));
            Map<String, BeiDanMatchDO> beiDanMatchDOMap = beiDanMatchDOS.stream().collect(Collectors.toMap(BeiDanMatchDO::getGameNo, a -> a, (b, c) -> b));
            for (RacingBallDO racingBallDO : racingBallDOList) {
                BeiDanMatchDO beiDanMatchDO = beiDanMatchDOMap.get(racingBallDO.getGameNo());
                if (null != beiDanMatchDO) {
                    racingBallDO.setTargetId(beiDanMatchDO.getId());
                } else {
                    return BaseVO.builder().success(false).errorCode("-1").errorMsg("未找到匹配赛事" + racingBallDO.getGameNo()).build();
                }
                if (DateUtils.addSeconds(now, beforeTimes).getTime() > beiDanMatchDO.getDeadline().getTime()) {
                    return BaseVO.builder().success(false).errorCode("-1").errorMsg("赛事" + racingBallDO.getGameNo() + " 已到截止时间，不再收单").build();
                }
            }
        } else if (LotteryOrderTypeEnum.VICTORY_DEFEAT.getKey().equals(lotteryOrderDO.getType()) || LotteryOrderTypeEnum.REN_JIU.getKey().equals(lotteryOrderDO.getType())) {
            List<String> gameNoList = racingBallDOList.stream().map(racingBallDO -> racingBallDO.getGameNo()).collect(Collectors.toList());
            List<WinBurdenMatchDO> winBurdenMatchDOS = winBurdenMatchMapper.selectList(new QueryWrapper<WinBurdenMatchDO>().lambda().in(WinBurdenMatchDO::getGameNo, gameNoList));
            Map<String, WinBurdenMatchDO> winBurdenMatchDOMap = winBurdenMatchDOS.stream().collect(Collectors.toMap(WinBurdenMatchDO::getGameNo, a -> a, (b, c) -> b));
            for (RacingBallDO racingBallDO : racingBallDOList) {
                WinBurdenMatchDO winBurdenMatchDO = winBurdenMatchDOMap.get(racingBallDO.getGameNo());
                if (null != winBurdenMatchDO) {
                    racingBallDO.setTargetId(winBurdenMatchDO.getId());
                } else {
                    return BaseVO.builder().success(false).errorCode("-1").errorMsg("未找到匹配赛事" + racingBallDO.getGameNo()).build();
                }
                if (DateUtils.addSeconds(now, beforeTimes).getTime() > winBurdenMatchDO.getDeadline().getTime()) {
                    return BaseVO.builder().success(false).errorCode("-1").errorMsg("赛事" + winBurdenMatchDO.getIssueNo() + "-" + winBurdenMatchDO.getNumber() + " 已到截止时间，不再收单").build();
                }
            }
        }
        lotteryOrderDO.setTargetIds(JSONUtil.toJsonStr(racingBallDOList.stream().map(racingBallDO -> racingBallDO.getTargetId()).collect(Collectors.toList())));
        //入库，写记录
        lotteryOrderMapper.insert(lotteryOrderDO);
        for (RacingBallDO racingBallDO : racingBallDOList) {
            racingBallDO.setId(null);
            racingBallMapper.insert(racingBallDO);
        }

        //扣款，写记录
        if (money.compareTo(userDO.getGold()) == 1) {
            if (money.compareTo(userDO.getGold().add(userDO.getPrice())) == 1) {
                return new BaseVO(false, ErrorCodeEnum.E0763.getKey(), ErrorCodeEnum.E0763.getValue());
            }
        }
        if (money.compareTo(userDO.getGold()) == 1) {
            //直接扣除彩金
            BigDecimal remainingPrice = money.subtract(userDO.getGold());
            //彩金直接设置为0
            userDO.setGold(new BigDecimal(0));
            //剩下的扣除奖金余额
            userDO.setPrice(userDO.getPrice().subtract(remainingPrice));
            userMapper.updateById(userDO);
        } else {
            //直接扣除彩金
            userDO.setGold(userDO.getGold().subtract(money));
            userMapper.updateById(userDO);
        }
        //添加钱包消费记录
        PayOrderDO payOrder = new PayOrderDO();
        payOrder.setType("" + lotteryId);
        payOrder.setOrderId(OrderNumberGenerationUtil.getOrderId());
        payOrder.setState(PayOrderStateEnum.PAID.getKey());
        payOrder.setCreateTime(new Date());
        payOrder.setUpdateTime(new Date());
        payOrder.setPayType(PayTypeEnum.APP.getKey());
        payOrder.setUserId(userDO.getId());
        payOrder.setPrice(money);
        payOrderMapper.insert(payOrder);
        Map<String, Object> map = new HashMap<>();
        map.put("gold", userDO.getGold().toPlainString());
        map.put("price", userDO.getPrice().toPlainString());
        map.put("total", userDO.getGold().add(userDO.getPrice()).toPlainString());
        return BaseDataVO.builder().data(map).build();
    }

    @Transactional(rollbackFor = Exception.class)
    @TenantIgnore
    @Override
    public BaseVO createDigitOrder(LotteryOrderDO lotteryOrderDO, List<PermutationDO> permutationDOS, String key) {
        String issueNo = lotteryOrderDO.getStageNumber() + "";

        Integer lotteryId = Integer.valueOf(lotteryOrderDO.getType());
        //判断此账户开通接单 与否
        ShopTransferDO shopTransferDO = TransferServiceImpl.SHOP_TRANSFER_MAP.get(key);
        if (null == shopTransferDO) {
            return BaseVO.builder().success(false).errorCode("-1").errorMsg("未开通，请联系商家").build();
        }
        if (!"0".equals(shopTransferDO.getInterfaceState())) {
            return BaseVO.builder().success(false).errorCode("-1").errorMsg("暂停收单，请联系商家").build();
        }
        UserDO userDO = userMapper.selectById(shopTransferDO.getUid());
        if (null == userDO) {
            return BaseVO.builder().success(false).errorCode("-1").errorMsg("账号异常，请联系商家").build();
        }
        //修改订单发起人为 账户对应的会员账户uid
        lotteryOrderDO.setUserId(userDO.getId());
        lotteryOrderDO.setTenantId(1);
        lotteryOrderDO.setCreateTime(new Date());
        lotteryOrderDO.setState("0");
        lotteryOrderDO.setWinPrice(BigDecimal.ZERO);
        lotteryOrderDO.setWinCounts(0);
        lotteryOrderDO.setTicketingTime(null);
        lotteryOrderDO.setBill(null);
        lotteryOrderDO.setId(null);
        lotteryOrderDO.setTransferShopId(shopTransferDO.getId());
        lotteryOrderDO.setTransferType(TransferEnum.TransferIn.code);//收单
        //判断此彩种是否收单
        LotteryTransferDO lotteryTransferDOS = lotteryTransferMapper.selectOne(new QueryWrapper<LotteryTransferDO>().lambda().eq(LotteryTransferDO::getLotteryType, lotteryId).eq(LotteryTransferDO::getTransferFlag, TransferEnum.TransferIn.code));
        if (null == lotteryTransferDOS || lotteryTransferDOS.getStates() != 0) {
            return BaseVO.builder().success(false).errorCode("-1").errorMsg("彩种暂停收单，请联系商家").build();
        }

        //判断此单是否已经下过单
        String orderNo = key + lotteryOrderDO.getOrderId();
        lotteryOrderDO.setOrderId(orderNo);
        if (lotteryOrderMapper.selectCount(new QueryWrapper<LotteryOrderDO>().lambda().eq(LotteryOrderDO::getOrderId, orderNo)) > 0) {
            return BaseVO.builder().success(false).errorCode("1").errorMsg("此单已经下过单，请勿重复下单").build();
        }

        //判断此账户对应的余额是否足够支付
        BigDecimal money = BigDecimal.ZERO;

        PermutationAwardDO permutationAwardDO = permutationAwardMapper.selectOne(new QueryWrapper<PermutationAwardDO>().lambda()
                .eq(PermutationAwardDO::getType, lotteryOrderDO.getType()).eq(PermutationAwardDO::getStageNumber, issueNo));
        if (null == permutationAwardDO) {
            return BaseVO.builder().success(false).errorCode("-1").errorMsg("彩种暂停，请联系商家").build();
        }
        Date now = new Date();
        int beforeTimes = lotteryTransferDOS.getTransferBeforeTime();
        if (DateUtils.addSeconds(now, beforeTimes).getTime() > permutationAwardDO.getDeadTime().getTime()) {
            return BaseVO.builder().success(false).errorCode("-1").errorMsg("彩种" + LotteryOrderTypeEnum.valueOFS(lotteryId + "").getValue() + " 已到截止时间，不再收单").build();
        }
        //入库，写记录
        lotteryOrderMapper.insert(lotteryOrderDO);
        for (PermutationDO permutationDO : permutationDOS) {
            permutationDO.setUserId(userDO.getId());
            permutationDO.setId(null);
            permutationDO.setCreateTime(new Date());
            permutationDO.setReward(null);
            permutationMapper.insert(permutationDO);
        }

        //扣款，写记录
        if (money.compareTo(userDO.getGold()) == 1) {
            if (money.compareTo(userDO.getGold().add(userDO.getPrice())) == 1) {
                return new BaseVO(false, ErrorCodeEnum.E0763.getKey(), ErrorCodeEnum.E0763.getValue());
            }
        }
        if (money.compareTo(userDO.getGold()) == 1) {
            //直接扣除彩金
            BigDecimal remainingPrice = money.subtract(userDO.getGold());
            //彩金直接设置为0
            userDO.setGold(new BigDecimal(0));
            //剩下的扣除奖金余额
            userDO.setPrice(userDO.getPrice().subtract(remainingPrice));
            userMapper.updateById(userDO);
        } else {
            //直接扣除彩金
            userDO.setGold(userDO.getGold().subtract(money));
            userMapper.updateById(userDO);
        }
        //添加钱包消费记录
        PayOrderDO payOrder = new PayOrderDO();
        payOrder.setType("" + lotteryId);
        payOrder.setOrderId(OrderNumberGenerationUtil.getOrderId());
        payOrder.setState(PayOrderStateEnum.PAID.getKey());
        payOrder.setCreateTime(new Date());
        payOrder.setUpdateTime(new Date());
        payOrder.setPayType(PayTypeEnum.APP.getKey());
        payOrder.setUserId(userDO.getId());
        payOrder.setPrice(money);
        payOrderMapper.insert(payOrder);
        Map<String, Object> map = new HashMap<>();
        map.put("gold", userDO.getGold().toPlainString());
        map.put("price", userDO.getPrice().toPlainString());
        map.put("total", userDO.getGold().add(userDO.getPrice()).toPlainString());
        return BaseDataVO.builder().data(map).build();
    }


    public static boolean isSports(Integer lotteryId) {
        return (lotteryId == 0 || lotteryId == 2 || lotteryId == 1 ||
                lotteryId == 6 || lotteryId == 7) ? true : false;

    }
}
