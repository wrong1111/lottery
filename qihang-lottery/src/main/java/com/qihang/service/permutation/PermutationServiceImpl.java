package com.qihang.service.permutation;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qihang.annotation.TenantIgnore;
import com.qihang.common.util.log.LogUtil;
import com.qihang.common.util.order.OrderNumberGenerationUtil;
import com.qihang.common.util.reward.*;
import com.qihang.common.vo.*;
import com.qihang.controller.grandlotto.dto.GrandLottoObjDTO;
import com.qihang.controller.permutation.app.dto.PlaceOrderDTO;
import com.qihang.controller.permutation.app.vo.PermutationRecordVO;
import com.qihang.controller.permutation.app.vo.PermutationVO;
import com.qihang.controller.permutation.app.vo.PlaceOrderVO;
import com.qihang.domain.order.LotteryOrderDO;
import com.qihang.domain.order.PayOrderDO;
import com.qihang.domain.permutation.PermutationAwardDO;
import com.qihang.domain.permutation.PermutationDO;
import com.qihang.domain.user.UserDO;
import com.qihang.enumeration.error.ErrorCodeEnum;
import com.qihang.enumeration.order.lottery.LotteryOrderStateEnum;
import com.qihang.enumeration.order.lottery.LotteryOrderTypeEnum;
import com.qihang.enumeration.order.pay.PayOrderStateEnum;
import com.qihang.enumeration.order.pay.PayOrderTypeEnum;
import com.qihang.enumeration.order.pay.PayTypeEnum;
import com.qihang.mapper.order.LotteryOrderMapper;
import com.qihang.mapper.order.PayOrderMapper;
import com.qihang.mapper.permutation.PermutationAwardMapper;
import com.qihang.mapper.permutation.PermutationMapper;
import com.qihang.mapper.user.UserMapper;
import com.qihang.service.documentary.DocumentaryCommissionHelper;
import io.reactivex.rxjava3.core.Completable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author bright
 * @since 2022-10-10
 */
@Slf4j
@Service
public class PermutationServiceImpl extends ServiceImpl<PermutationMapper, PermutationDO> implements IPermutationService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private LotteryOrderMapper orderMapper;

    @Resource
    private PermutationMapper permutationMapper;

    @Resource
    private PermutationAwardMapper permutationAwardMapper;

    @Resource
    private PayOrderMapper payOrderMapper;


    @Resource
    private LogUtil logUtil;

    @Resource
    DocumentaryCommissionHelper documentaryCommissionHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseVO placeOrder(List<PlaceOrderDTO> placeList, Integer userId, String type, String issueNo) {
        //  int hour = DateUtil.hour(new Date(), true);
//        if (hour >= 21 && hour <= 22) {
//            return new BaseVO(false, ErrorCodeEnum.E082.getKey(), ErrorCodeEnum.E082.getValue());
//        }
        Date date = new Date();
        //查询当前期号
        PermutationAwardDO permutationAward = permutationAwardMapper.selectOne(new QueryWrapper<PermutationAwardDO>().lambda().eq(PermutationAwardDO::getType, type)
                .eq(PermutationAwardDO::getStageNumber, issueNo));
        if (null == permutationAward || permutationAward.getDeadTime().before(date)) {
            return new BaseVO(false, ErrorCodeEnum.E095.getKey(), ErrorCodeEnum.E095.getValue());
        }
        Integer stageNumber = permutationAward.getStageNumber();
        PlaceOrderVO placeOrder = new PlaceOrderVO();
        //计算需要下注的金额
        BigDecimal price = new BigDecimal(0);
        for (PlaceOrderDTO placeOrderDTO : placeList) {
            price = price.add(BigDecimal.valueOf(placeOrderDTO.getNotes() * placeOrderDTO.getTimes() * 2));
        }
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
        // 1.先查询出奖的最后一条数据从而得出这次买的是第几期
        //PermutationAwardDO permutationAward = permutationAwardMapper.selectOne(new QueryWrapper<PermutationAwardDO>().lambda().eq(PermutationAwardDO::getType, type).gt(PermutationAwardDO::getDeadTime, new Date()).orderByAsc(PermutationAwardDO::getId).last("limit 1"));


        logUtil.record(LotteryOrderTypeEnum.valueOFS(type).getValue() + "下单,下单金额【" + price + "】,期号[" + stageNumber + "]");
        //添加钱包消费记录
        PayOrderDO payOrder = new PayOrderDO();
        payOrder.setOrderId(OrderNumberGenerationUtil.getOrderId());
        payOrder.setState(PayOrderStateEnum.PAID.getKey());
        payOrder.setCreateTime(new Date());
        payOrder.setUpdateTime(new Date());
        payOrder.setType(PayOrderTypeEnum.ARRAY.getKey());
        if (type.equals(LotteryOrderTypeEnum.ARRANGE.getKey())) {
            payOrder.setType(PayOrderTypeEnum.ARRANGE.getKey());
        } else if (type.equals(LotteryOrderTypeEnum.SEVEN_STAR.getKey())) {
            payOrder.setType(PayOrderTypeEnum.SEVEN_STAR.getKey());
        } else if (type.equals(LotteryOrderTypeEnum.GRAND_LOTTO.getKey())) {
            payOrder.setType(PayOrderTypeEnum.GRAND_LOTTO.getKey());
            //wyong edit 福彩3D
        } else if (type.equals(LotteryOrderTypeEnum.FC3D.getKey())) {
            payOrder.setType(PayOrderTypeEnum.FC3D.getKey());
        } else if (type.equals(LotteryOrderTypeEnum.FCSSQ.getKey())) {
            payOrder.setType(PayOrderTypeEnum.FCSSQ.getKey());
        } else if (type.equals(LotteryOrderTypeEnum.FCKL8.getKey())) {
            payOrder.setType(PayOrderTypeEnum.FCKL8.getKey());
        } else if (type.equals(LotteryOrderTypeEnum.FCQLC.getKey())) {
            payOrder.setType(PayOrderTypeEnum.FCQLC.getKey());
        }
        payOrder.setUserId(userId);
        payOrder.setPayType(PayTypeEnum.APP.getKey());
        payOrder.setPrice(price);
        payOrderMapper.insert(payOrder);


        //批量添加投注数据
        List<Integer> ids = new ArrayList<>();

        //组合
        List<PermutationVO> list = new ArrayList<>();

        for (PlaceOrderDTO placeOrderDTO : placeList) {
            // 2.添加下注数据
            PermutationDO permutation = new PermutationDO();
            permutation.setUserId(userId);
            permutation.setMode(placeOrderDTO.getMode());
            permutation.setStageNumber(stageNumber);
            permutation.setHundredMyriad(StrUtil.join(",", placeOrderDTO.getHundredMyriad()));
            permutation.setTenMyriad(StrUtil.join(",", placeOrderDTO.getTenMyriad()));
            permutation.setMyriad(StrUtil.join(",", placeOrderDTO.getMyriad()));
            permutation.setKilo(StrUtil.join(",", placeOrderDTO.getKilo()));
            permutation.setIndividual(StrUtil.join(",", placeOrderDTO.getIndividual()));
            permutation.setTen(StrUtil.join(",", placeOrderDTO.getTen()));
            permutation.setHundred(StrUtil.join(",", placeOrderDTO.getHundred()));
            //处理组三和组六
            if (permutation.getMode().equals("1") || permutation.getMode().equals("2")) {
                permutation.setIndividual(JSONUtil.toJsonStr(placeOrderDTO.getIndividual()));
            }
            if (StrUtil.equals(type, LotteryOrderTypeEnum.GRAND_LOTTO.getKey())
                    || StrUtil.equals(type, LotteryOrderTypeEnum.FCSSQ.getKey())
                    || StrUtil.equals(type, LotteryOrderTypeEnum.FCQLC.getKey())
                    || StrUtil.equals(type, LotteryOrderTypeEnum.FCKL8.getKey())
            ) {
                permutation.setIndividual(JSONUtil.toJsonStr(placeOrderDTO.getIndividual()));
                permutation.setTen(JSONUtil.toJsonStr(placeOrderDTO.getTen()));
            }
            permutation.setNotes(placeOrderDTO.getNotes());
            permutation.setTimes(placeOrderDTO.getTimes());
            permutation.setType(type);
            permutation.setCreateTime(new Date());
            permutation.setUpdateTime(new Date());
            permutationMapper.insert(permutation);
            ids.add(permutation.getId());
            List<PermutationVO> makeUp = new ArrayList<>();
            //组合成方案
            if (type.equals(LotteryOrderTypeEnum.ARRAY.getKey()) || type.equals(LotteryOrderTypeEnum.FC3D.getKey())) {
                //排列3 0 直选
                if (placeOrderDTO.getMode().equals("0")) {
                    makeUp = PermutationUtil.makeUp(type, placeOrderDTO.getMode(), stageNumber, placeOrderDTO.getTimes(), placeOrderDTO.getHundred().toArray(), placeOrderDTO.getTen().toArray(), placeOrderDTO.getIndividual().toArray());
                    //2 组六  3 直选和值 1组三复式 4 组选和值
                } else if (placeOrderDTO.getMode().equals("1") || placeOrderDTO.getMode().equals("2") || placeOrderDTO.getMode().equals("3") || placeOrderDTO.getMode().equals("4")) {
                    makeUp = PermutationUtil.makeUp(type, placeOrderDTO.getMode(), stageNumber, placeOrderDTO.getTimes(), placeOrderDTO.getIndividual().toArray());
                } else if (placeOrderDTO.getMode().equals("5")) {
                    //组三单式 2,3|4 (前面组对子，后面单选）
                    makeUp = PermutationUtil.makeUp(type, placeOrderDTO.getMode(), stageNumber, placeOrderDTO.getTimes(), placeOrderDTO.getTen().toArray(), placeOrderDTO.getIndividual().toArray());
                }
            } else if (type.equals(LotteryOrderTypeEnum.ARRANGE.getKey())) {
                //排列5
                makeUp = PermutationUtil.makeUp(type, placeOrderDTO.getMode(), stageNumber, placeOrderDTO.getTimes(), placeOrderDTO.getMyriad().toArray(), placeOrderDTO.getKilo().toArray(), placeOrderDTO.getHundred().toArray(), placeOrderDTO.getTen().toArray(), placeOrderDTO.getIndividual().toArray());
            } else if (type.equals(LotteryOrderTypeEnum.SEVEN_STAR.getKey())) {
                //七星彩
                makeUp = PermutationUtil.makeUp(type, placeOrderDTO.getMode(), stageNumber, placeOrderDTO.getTimes(), placeOrderDTO.getHundredMyriad().toArray(), placeOrderDTO.getTenMyriad().toArray(), placeOrderDTO.getMyriad().toArray(), placeOrderDTO.getKilo().toArray(), placeOrderDTO.getHundred().toArray(), placeOrderDTO.getTen().toArray(), placeOrderDTO.getIndividual().toArray());
            } else if (type.equals(LotteryOrderTypeEnum.GRAND_LOTTO.getKey())) {
                //大乐透
                makeUp = JSONUtil.toList(placeOrderDTO.getSchemeDetails(), PermutationVO.class);
            } else if (type.equals(LotteryOrderTypeEnum.FCSSQ.getKey())) {
                //双色球
                makeUp = JSONUtil.toList(placeOrderDTO.getSchemeDetails(), PermutationVO.class);
            } else if (type.equals(LotteryOrderTypeEnum.FCQLC.getKey())) {
                //七乐彩
                makeUp = JSONUtil.toList(placeOrderDTO.getSchemeDetails(), PermutationVO.class);
            } else if (type.equals(LotteryOrderTypeEnum.FCKL8.getKey())) {
                //快乐8 选一”、“选二”、“选三”、“选四”、“选五”、“选六”、“选七”、“选八”、“选九”和“选十”十种玩法
                //前端直接传过来的，需要 自己拆分成单注
                makeUp = PermutationUtil.makeUp(type, placeOrderDTO.getMode(), stageNumber, placeOrderDTO.getTimes(), placeOrderDTO.getIndividual().toArray());
            }
            list.addAll(makeUp);
        }
        // 3.创建订单
        LotteryOrderDO order = new LotteryOrderDO();
        order.setOrderId(OrderNumberGenerationUtil.getOrderId());
        order.setUserId(userId);
        order.setPrice(price);
        order.setTargetIds(StrUtil.join(",", ids));
        order.setType(LotteryOrderTypeEnum.valueOFS(type).getKey());
        order.setSchemeDetails(JSONUtil.toJsonStr(list));
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        order.setStageNumber(stageNumber);
        orderMapper.insert(order);
        placeOrder.setId(order.getId());
        return placeOrder;
    }

    @Override
    public CommonListVO<PermutationRecordVO> record(String type) {
        CommonListVO<PermutationRecordVO> commonList = new CommonListVO<>();
        List<PermutationRecordVO> list = new ArrayList<>();
        List<PermutationAwardDO> awardList = permutationAwardMapper.selectList(new QueryWrapper<PermutationAwardDO>().lambda().eq(PermutationAwardDO::getType, type).orderByDesc(PermutationAwardDO::getCreateTime));
        for (PermutationAwardDO permutationAwardDO : awardList) {
            PermutationRecordVO permutationRecord = new PermutationRecordVO();
            BeanUtils.copyProperties(permutationAwardDO, permutationRecord);
            permutationRecord.setRewardList(Arrays.asList(permutationAwardDO.getReward().split(",")));
            list.add(permutationRecord);
        }
        commonList.setVoList(list);
        return commonList;
    }


    /*
     排列开奖逻辑。
     数字彩兑奖逻辑
     */
    @Override
    @TenantIgnore
    public BaseVO calculation(PermutationAwardDO permutationAward) {
        //最后出奖的时候，如果还有未出票的订单进行退回金额
        List<LotteryOrderDO> retreatOrderList = orderMapper.selectList(new QueryWrapper<LotteryOrderDO>().lambda().eq(LotteryOrderDO::getState, LotteryOrderStateEnum.TO_BE_ISSUED.getKey()).eq(LotteryOrderDO::getType, permutationAward.getType()));
        for (LotteryOrderDO lotteryOrderDO : retreatOrderList) {
            List<PermutationDO> permutationList = permutationMapper.selectBatchIds(Convert.toList(Integer.class, lotteryOrderDO.getTargetIds()));
            for (PermutationDO permutationDO : permutationList) {
                if (permutationDO.getStageNumber().equals(permutationAward.getStageNumber())) {
                    UserDO userDO = userMapper.selectById(lotteryOrderDO.getUserId());
                    userDO.setGold(userDO.getGold().add(lotteryOrderDO.getPrice()));
                    userMapper.updateById(userDO);
                    //修改订单状态
                    lotteryOrderDO.setState(LotteryOrderStateEnum.REFUND.getKey());
                    orderMapper.updateById(lotteryOrderDO);
                    //添加钱包记录
                    addRecord(lotteryOrderDO);
                }
            }
        }
        List<LotteryOrderDO> orderList = orderMapper.selectList(new QueryWrapper<LotteryOrderDO>().lambda().eq(LotteryOrderDO::getStageNumber, permutationAward.getStageNumber()).eq(LotteryOrderDO::getType, permutationAward.getType()).eq(LotteryOrderDO::getState, LotteryOrderStateEnum.TO_BE_AWARDED.getKey()));
        for (LotteryOrderDO lotteryOrderDO : orderList) {
            log.debug("彩种[{}],订单[{}] 期号[{}],开奖号码:[{}]开始兑奖 >>>>", LotteryOrderTypeEnum.valueOFS(lotteryOrderDO.getType()).getValue(), lotteryOrderDO.getOrderId(), permutationAward.getStageNumber(), permutationAward.getReward());
            //根据id查询投注信息
            List<PermutationDO> permutationList = permutationMapper.selectBatchIds(Convert.toList(Integer.class, lotteryOrderDO.getTargetIds()));
            //判断排列3选的是直选还是组三还是组九
            Double price = 0d;
//            for (PermutationDO permutationDO : permutationList) {
//                if (permutationDO.getStageNumber().equals(permutationAward.getStageNumber())) {
//                    Boolean flag = false;
//                    double bonus = 0d;
//                    //是排列3还是排列5
//                    //wyong edit  福彩3D
//                    if (permutationAward.getType().equals(LotteryOrderTypeEnum.ARRAY.getKey()) || permutationAward.getType().equals(LotteryOrderTypeEnum.FC3D.getKey())) {
//                        //直选
//                        if (ObjectUtil.equal(permutationDO.getMode(), "0")) {
//                            bonus = PermutationUtil.directlyElected(permutationDO.getHundred().split(","), permutationDO.getTen().split(","), permutationDO.getIndividual().split(","), permutationAward.getReward());
//                            flag = bonus > 0 ? true : false;
//                        }
//                        //组三
//                        if (ObjectUtil.equal(permutationDO.getMode(), "1")) {
//                            bonus = PermutationUtil.GroupThree(permutationDO.getIndividual(), permutationAward.getReward());
//                            flag = bonus > 0 ? true : false;
//                        }
//                        //组六
//                        if (ObjectUtil.equal(permutationDO.getMode(), "2")) {
//                            //计算用户买的是否中奖
//                            bonus = PermutationUtil.groupSix(permutationDO.getIndividual(), permutationAward.getReward());
//                            flag = bonus > 0 ? true : false;
//                        }
//                        //直选和值
//                        if (ObjectUtil.equal(permutationDO.getMode(), "3")) {
//                            //计算用户买的是否中奖
//                            bonus = PermutationUtil.directlyElectedGentle(permutationDO.getIndividual().split(","), permutationAward.getReward());
//                            flag = bonus > 0 ? true : false;
//                        }
//                        //组选和值
//                        if (ObjectUtil.equal(permutationDO.getMode(), "4")) {
//                            //计算用户买的是否中奖
//                            bonus = PermutationUtil.groupGentle(permutationDO.getIndividual().split(","), permutationAward.getReward());
//                            flag = bonus > 0 ? true : false;
//                        }
//                        //组三复式
//                        if (ObjectUtil.equal(permutationDO.getMode(), "5")) {
//                            //计算用户买的是否中奖
//                            bonus = PermutationUtil.compound(permutationDO.getTen().split(","), permutationDO.getIndividual().split(","), permutationAward.getReward());
//                            flag = bonus > 0 ? true : false;
//                        }
//                    } else if (permutationAward.getType().equals(LotteryOrderTypeEnum.ARRANGE.getKey())) {
//                        bonus = PermutationUtil.arrangeElected(permutationDO.getMyriad().split(","), permutationDO.getKilo().split(","), permutationDO.getHundred().split(","), permutationDO.getTen().split(","), permutationDO.getIndividual().split(","), permutationAward.getReward());
//                        flag = bonus > 0 ? true : false;
//                    } else if (permutationAward.getType().equals(LotteryOrderTypeEnum.SEVEN_STAR.getKey())) {
//                        //七星彩开奖算法
//                        bonus = PermutationUtil.sevenStarLottery(permutationDO.getHundredMyriad().split(","), permutationDO.getTenMyriad().split(","), permutationDO.getMyriad().split(","), permutationDO.getKilo().split(","), permutationDO.getHundred().split(","), permutationDO.getTen().split(","), permutationDO.getIndividual().split(","), permutationAward.getReward(), permutationAward.getMoneyAward());
//                        flag = bonus > 0 ? true : false;
//                    } else if (permutationAward.getType().equals(LotteryOrderTypeEnum.GRAND_LOTTO.getKey())) {
//                        //大乐透开奖算法
//                        bonus = GrandLottoUtil.award(JSONUtil.toList(permutationDO.getTen(), GrandLottoObjDTO.class), JSONUtil.toList(permutationDO.getIndividual(), GrandLottoObjDTO.class), permutationAward.getReward(), permutationAward.getMoneyAward().split(",")[0], permutationAward.getMoneyAward().split(",")[1]);
//                        flag = bonus > 0 ? true : false;
//                    } else if (permutationAward.getType().equals(LotteryOrderTypeEnum.FCQLC.getKey())) {
//                        //七乐彩开奖算法
//                        BonusVo bonusVo = GrandLottoUtil.awardQLC(JSONUtil.toList(permutationDO.getIndividual(), GrandLottoObjDTO.class), permutationAward.getReward(), permutationAward.getMoneyAward());
//                        flag = bonusVo.getAward();
//                        bonus = bonusVo.getMoney();
//                    } else if (permutationAward.getType().equals(LotteryOrderTypeEnum.FCSSQ.getKey())) {
//                        //双色球
//                        BonusVo bonusVo = GrandLottoUtil.awardSSQ(JSONUtil.toList(permutationDO.getTen(), GrandLottoObjDTO.class), JSONUtil.toList(permutationDO.getIndividual(), GrandLottoObjDTO.class), permutationAward.getReward(), permutationAward.getMoneyAward());
//                        flag = bonusVo.getAward();
//                        bonus = bonusVo.getMoney();
//                    } else if (permutationAward.getType().equals(LotteryOrderTypeEnum.FCKL8.getKey())) {
//                        //快乐8
//                        List<BonusVo> bonusVo = GrandLottoUtil.awardKL8(JSONUtil.toList(permutationDO.getIndividual(), GrandLottoObjDTO.class), permutationAward.getReward(), permutationAward.getMoneyAward(), permutationDO.getMode());
//                        flag = !bonusVo.isEmpty();
//                        bonus = bonusVo.stream().mapToDouble(item -> item.getAwardNotes() * item.getMoney()).sum();
//                    }
//
//                    //TODO 开奖算法
//                    //wyong edit
//                    if (flag) {
//                        price += Double.valueOf(bonus * permutationDO.getTimes());
//                    }
//                }
//            }
            //另外 计算schemedetail
            this.calculationBySchemeDetail(lotteryOrderDO, permutationAward);
            BonuseVO bonuseVO = BonuseVO.builder().build();
            price = lotteryOrderDO.getPrice() != null ? lotteryOrderDO.getWinPrice().doubleValue() : 0;
            log.debug("彩种[{}],订单[{}]  中奖金额[{}] >>>>", LotteryOrderTypeEnum.valueOFS(lotteryOrderDO.getType()).getValue(), lotteryOrderDO.getOrderId(), price);
            if (price > 0) {
                bonuseVO.setShoted(true);
                bonuseVO.setBonus(BigDecimal.valueOf(price));
                log.debug("彩种[{}],订单[{}]  已中奖 >>>>", LotteryOrderTypeEnum.valueOFS(lotteryOrderDO.getType()).getValue(), lotteryOrderDO.getOrderId());
                //中奖就修改订单状态为待派奖
                lotteryOrderDO.setUpdateTime(new Date());
                documentaryCommissionHelper.processCommiss(LotteryOrderTypeEnum.valueOFS(permutationAward.getType()).getValue(), lotteryOrderDO, bonuseVO);
                orderMapper.updateById(lotteryOrderDO);
            } else {
                log.debug("彩种[{}],订单[{}]  未中奖 >>>>", LotteryOrderTypeEnum.valueOFS(lotteryOrderDO.getType()).getValue(), lotteryOrderDO.getOrderId());
                //没有状态改为未中奖
                lotteryOrderDO.setState(LotteryOrderStateEnum.FAIL_TO_WIN.getKey());
                lotteryOrderDO.setUpdateTime(new Date());
                orderMapper.updateById(lotteryOrderDO);
            }

        }
        return new BaseVO();

    }

    /*
     *  数字彩兑奖逻辑
     * */
    @TenantIgnore
    @Override
    public BaseVO calculation(String type) {
        log.debug(" 开奖 彩种[{}],name:[{}] ", type, LotteryOrderTypeEnum.valueOFS(type).getValue());
        List<LotteryOrderDO> orderList = orderMapper.selectList(new QueryWrapper<LotteryOrderDO>().lambda().eq(LotteryOrderDO::getType, type).eq(LotteryOrderDO::getState, LotteryOrderStateEnum.TO_BE_AWARDED.getKey()).orderByAsc(LotteryOrderDO::getStageNumber));
        if (CollectionUtils.isEmpty(orderList)) {
            return new BaseVO();
        }
        for (LotteryOrderDO order : orderList) {
            Integer issueNo = order.getStageNumber();
            PermutationAwardDO permutationAwardDO = permutationAwardMapper.selectOne(new QueryWrapper<PermutationAwardDO>().lambda().eq(PermutationAwardDO::getType, type).eq(PermutationAwardDO::getStageNumber, issueNo).orderByDesc(PermutationAwardDO::getCreateTime));
            if (null == permutationAwardDO) {
                log.error(" 开奖 彩种[{}],name:[{}] 期号[{}] 没有开奖数据", type, LotteryOrderTypeEnum.valueOFS(type).getValue(), issueNo);
                break;
            }
            calculation(permutationAwardDO);
        }
        return new BaseVO();
    }

    @TenantIgnore
    @Override
    public BaseVO calculation(LotteryOrderDO order) {
        Integer issueNo = order.getStageNumber();
        PermutationAwardDO permutationAwardDO = permutationAwardMapper.selectOne(new QueryWrapper<PermutationAwardDO>().lambda().eq(PermutationAwardDO::getType, order.getType()).eq(PermutationAwardDO::getStageNumber, issueNo).orderByDesc(PermutationAwardDO::getCreateTime));
        if (null == permutationAwardDO) {
            log.error(" 开奖 彩种[{}],name:[{}] 期号[{}] 没有开奖数据", order.getType(), LotteryOrderTypeEnum.valueOFS(order.getType()).getValue(), issueNo);
            return BaseVO.builder().success(false).errorMsg("没有开奖数据").build();
        }
        return calculation(permutationAwardDO);
    }


    @TenantIgnore
    @Override
    public BaseVO calculationBySchemeDetail(String type) {
        log.debug(" 开奖 彩种[{}],name:[{}] ", type, LotteryOrderTypeEnum.valueOFS(type).getValue());
        List<LotteryOrderDO> orderList = orderMapper.selectList(new QueryWrapper<LotteryOrderDO>().lambda().eq(LotteryOrderDO::getType, type).eq(LotteryOrderDO::getState, LotteryOrderStateEnum.TO_BE_AWARDED.getKey()).orderByAsc(LotteryOrderDO::getStageNumber));
        if (CollectionUtils.isEmpty(orderList)) {
            return new BaseVO();
        }
        for (LotteryOrderDO order : orderList) {
            Integer issueNo = order.getStageNumber();
            PermutationAwardDO permutationAwardDO = permutationAwardMapper.selectOne(new QueryWrapper<PermutationAwardDO>().lambda().eq(PermutationAwardDO::getType, type).eq(PermutationAwardDO::getStageNumber, issueNo).orderByDesc(PermutationAwardDO::getCreateTime));
            if (null == permutationAwardDO || StringUtils.isBlank(permutationAwardDO.getReward())) {
                log.error(" 开奖 彩种[{}],name:[{}] 期号[{}] 没有开奖数据", type, LotteryOrderTypeEnum.valueOFS(type).getValue(), issueNo);
                break;
            }
            calculationBySchemeDetail(order, permutationAwardDO);
        }
        return new BaseVO();
    }

    @TenantIgnore
    @Override
    public BaseVO calculationBySchemeDetail(LotteryOrderDO order, PermutationAwardDO permutationAwardDO) {
        List<DigitBall> digitBalls = JSONUtil.toList(order.getSchemeDetails(), DigitBall.class);

        //调用线程池
        Future<List<DigitBall>> future = DigitBallAwardUtils.threadPool.submit(new DigitBallAward(digitBalls, permutationAwardDO));
        Map<String, Object> result = new HashMap<>();
        try {
            List<DigitBall> balls = future.get();
            int awardCounts = 0;
            double awardMoney = 0;
            for (DigitBall ball : balls) {
                if (ball.getAward()) {
                    awardMoney = awardMoney + Double.valueOf(ball.getMoney());
                    awardCounts++;
                }
            }
            log.error(" 彩种[{}],订单[{}],中奖金额[{}],中奖注数[{}]", LotteryOrderTypeEnum.valueOFS(order.getType()).getValue(), order.getOrderId(), awardMoney, awardCounts);
            String json = JSON.toJSONString(balls);
            LotteryOrderDO updateOrder = new LotteryOrderDO();
            updateOrder.setSchemeDetails(json);
            updateOrder.setWinCounts(awardCounts);
            if (awardMoney > 0) {
                updateOrder.setWinPrice(BigDecimal.valueOf(awardMoney).multiply(BigDecimal.valueOf(order.getTimes())));
                order.setWinPrice(BigDecimal.valueOf(awardMoney).multiply(BigDecimal.valueOf(order.getTimes())));
                updateOrder.setState("3");
                order.setState("3");
            } else {
                updateOrder.setWinPrice(null);
                updateOrder.setState("2");
                order.setState("2");
                order.setWinPrice(null);
            }
            // orderMapper.update(updateOrder, new QueryWrapper<LotteryOrderDO>().lambda().eq(LotteryOrderDO::getId, order.getId()));
//            result.put("awardCounts", awardCounts);
//            result.put("awardMoney", awardMoney);
//            result.put("issueNo", permutationAwardDO.getStageNumber());
//            result.put("reward", permutationAwardDO.getReward());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return BaseDataVO.builder().success(true).data(result).build();
    }

    private void addRecord(LotteryOrderDO lotteryOrder) {
        PayOrderDO payOrder = new PayOrderDO();
        payOrder.setOrderId(OrderNumberGenerationUtil.getOrderId());
        String type = "";
        if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.ARRAY.getKey())) {
            type = PayOrderTypeEnum.ARRAY_REFUND.getKey();
        } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.ARRANGE.getKey())) {
            type = PayOrderTypeEnum.ARRANGE_REFUND.getKey();
        } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.SEVEN_STAR.getKey())) {
            type = PayOrderTypeEnum.SEVEN_STAR_REFUND.getKey();
            //wyong edit 福彩3D退标
        } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.FC3D.getKey())) {
            type = PayOrderTypeEnum.FC3D_REFUND.getKey();
        } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.FCKL8.getKey())) {
            type = PayOrderTypeEnum.FCKL8_REFUND.getKey();
        } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.FCQLC.getKey())) {
            type = PayOrderTypeEnum.FCQLC_REFUND.getKey();
        } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.FCSSQ.getKey())) {
            type = PayOrderTypeEnum.FCSSQ_REFUND.getKey();
        }
        payOrder.setType(type);
        payOrder.setState(PayOrderStateEnum.PAYMENT.getKey());
        payOrder.setCreateTime(new Date());
        payOrder.setTenantId(lotteryOrder.getTenantId());
        payOrder.setUpdateTime(new Date());
        payOrder.setPayType(PayTypeEnum.APP.getKey());
        payOrder.setUserId(lotteryOrder.getUserId());
        payOrder.setPrice(lotteryOrder.getPrice());
        payOrderMapper.insert(payOrder);
    }
}
