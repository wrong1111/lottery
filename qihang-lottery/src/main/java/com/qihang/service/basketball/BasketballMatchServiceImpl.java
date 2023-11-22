package com.qihang.service.basketball;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qihang.annotation.TenantIgnore;
import com.qihang.common.util.order.OrderNumberGenerationUtil;
import com.qihang.common.util.reward.BasketballUtil;
import com.qihang.common.util.reward.FootballUtil;
import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.controller.basketball.dto.BasketballMatchDTO;
import com.qihang.controller.basketball.vo.BasketballMatchVO;
import com.qihang.controller.basketball.vo.BasketballVO;
import com.qihang.controller.order.admin.lottery.vo.SportSchemeDetailsListVO;
import com.qihang.controller.racingball.app.dto.BallCalculationDTO;
import com.qihang.controller.racingball.app.vo.BallCalculationVO;
import com.qihang.domain.basketball.BasketballMatchDO;
import com.qihang.domain.beidan.BeiDanMatchDO;
import com.qihang.domain.documentary.DocumentaryDO;
import com.qihang.domain.documentary.DocumentaryUserDO;
import com.qihang.domain.order.LotteryOrderDO;
import com.qihang.domain.order.PayOrderDO;
import com.qihang.domain.racingball.RacingBallDO;
import com.qihang.domain.user.UserDO;
import com.qihang.enumeration.ball.BettingStateEnum;
import com.qihang.enumeration.order.lottery.LotteryOrderStateEnum;
import com.qihang.enumeration.order.lottery.LotteryOrderTypeEnum;
import com.qihang.enumeration.order.pay.PayOrderStateEnum;
import com.qihang.enumeration.order.pay.PayOrderTypeEnum;
import com.qihang.enumeration.order.pay.PayTypeEnum;
import com.qihang.mapper.basketball.BasketballMatchMapper;
import com.qihang.mapper.documentary.DocumentaryMapper;
import com.qihang.mapper.documentary.DocumentaryUserMapper;
import com.qihang.mapper.order.LotteryOrderMapper;
import com.qihang.mapper.order.PayOrderMapper;
import com.qihang.mapper.racingball.RacingBallMapper;
import com.qihang.mapper.user.UserMapper;
import com.qihang.service.documentary.DocumentaryCommissionHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bright
 * @since 2022-10-05
 */
@Service
@Slf4j
public class BasketballMatchServiceImpl extends ServiceImpl<BasketballMatchMapper, BasketballMatchDO> implements IBasketballMatchService {

    @Resource
    private BasketballMatchMapper basketballMatchMapper;

    @Resource
    private LotteryOrderMapper lotteryOrderMapper;

    @Resource
    private RacingBallMapper racingBallMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private PayOrderMapper payOrderMapper;

    @Resource
    private DocumentaryMapper documentaryMapper;
    @Resource
    private DocumentaryUserMapper documentaryUserMapper;

    @Resource
    DocumentaryCommissionHelper documentaryCommissionHelper;

    @Override
    public CommonListVO<BasketballVO> basketballMatchList() {
        CommonListVO<BasketballVO> commonList = new CommonListVO<>();
        List<BasketballVO> basketballList = new ArrayList<>();
        //大于当前时间 wyong 23-11-12
        List<BasketballMatchDO> basketballMatchList = basketballMatchMapper.selectList(new QueryWrapper<BasketballMatchDO>().lambda().eq(BasketballMatchDO::getState, BettingStateEnum.YES.getKey()).gt(BasketballMatchDO::getDeadline, new Date()));
        Map<String, List<BasketballMatchDO>> map = basketballMatchList.stream().collect(Collectors.groupingBy(BasketballMatchDO::getStartTime));
        //对map的key进行排序
        map = map.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        Integer id = 0;
        for (Map.Entry<String, List<BasketballMatchDO>> entry : map.entrySet()) {
            BasketballVO basketball = new BasketballVO();
            basketball.setStartTime(entry.getKey());
            basketball.setCount(entry.getValue().size());
            List<BasketballMatchVO> basketballMatchVOList = new ArrayList<>();
            //排序
            List<BasketballMatchDO> list = entry.getValue();
            list.sort(Comparator.comparing(BasketballMatchDO::getNumber));
            for (BasketballMatchDO basketballMatch : list) {
                BasketballMatchVO basketballMatchVO = new BasketballMatchVO();
                BeanUtils.copyProperties(basketballMatch, basketballMatchVO);
                //默认选择项为0，方便前端好控制选择了几项
                basketballMatchVO.setChoiceCount(0);
                List<Map<String, Object>> mapList = new ArrayList<>();
                Map<String, Object> mapObj = null;

                /*============================胜负赔率组成list<map>结构==============================*/
                if (StrUtil.isNotBlank(basketballMatch.getWinNegativeOdds())) {
                    String[] winNegativeArr = basketballMatch.getWinNegativeOdds().split(",");
                    for (int i = 0; i < winNegativeArr.length; i++) {
                        mapObj = new HashMap<>(3);
                        mapObj.put("id", ++id);
                        mapObj.put("odds", winNegativeArr[i]);
                        mapObj.put("active", false);
                        if (i == 0) {
                            mapObj.put("describe", "主负");
                        } else if (i == 1) {
                            mapObj.put("describe", "主胜");
                        }
                        mapList.add(mapObj);
                    }
                }
                //添加对对象中
                basketballMatchVO.setWinNegativeOddsList(mapList);
                /*============================让分胜负赔率组成list<map>===============================*/

                //清空原来的数据
                mapList = new ArrayList<>();
                if (StrUtil.isNotBlank(basketballMatch.getCedePointsOdds())) {
                    String[] cedePointsArr = basketballMatch.getCedePointsOdds().split(",");
                    for (int i = 0; i < cedePointsArr.length; i++) {
                        mapObj = new HashMap<>(3);
                        mapObj.put("id", ++id);
                        mapObj.put("odds", cedePointsArr[i]);
                        mapObj.put("active", false);
                        if (i == 0) {
                            mapObj.put("describe", "主负");
                        } else if (i == 1) {
                            mapObj.put("describe", "主胜");
                        }
                        mapList.add(mapObj);
                    }
                }
                //添加对对象中
                basketballMatchVO.setCedePointsOddsList(mapList);

                /*============================让大小分赔率组成list<map>===============================*/

                //清空原来的数据
                mapList = new ArrayList<>();
                if (StrUtil.isNotBlank(basketballMatch.getSizeOdds())) {
                    String[] sizeArr = basketballMatch.getSizeOdds().split(",");
                    for (int i = 0; i < sizeArr.length; i++) {
                        mapObj = new HashMap<>(3);
                        mapObj.put("id", ++id);
                        mapObj.put("odds", sizeArr[i]);
                        mapObj.put("score", sizeArr[1]);
                        mapObj.put("active", false);
                        if (i == 0) {
                            mapObj.put("describe", "大");
                        } else if (i == 2) {
                            mapObj.put("describe", "小");
                        }
                        mapList.add(mapObj);
                    }
                }
                //添加对对象中
                basketballMatchVO.setSizeOddsList(mapList);


                /*============================胜负差赔率组成list<map>===============================*/

                //清空原来的数据
                mapList = new ArrayList<>();
                if (StrUtil.isNotBlank(basketballMatch.getDifferenceOdds())) {
                    String[] differenceArr = basketballMatch.getDifferenceOdds().split(",");
                    String[] differenceItems = {"1-5", "6-10", "11-15", "16-20", "21-25", "26+", "1-5", "6-10", "11-15", "16-20", "21-25", "26+"};
                    for (int i = 0; i < differenceArr.length; i++) {
                        mapObj = new HashMap<>(3);
                        mapObj.put("id", ++id);
                        if (i <= 5) {
                            mapObj.put("describe", "主负" + differenceItems[i]);
                        } else {
                            mapObj.put("describe", "主胜" + differenceItems[i]);
                        }
                        mapObj.put("odds", differenceArr[i]);
                        mapObj.put("active", false);
                        mapList.add(mapObj);
                    }
                }
                //添加对对象中
                basketballMatchVO.setDifferenceOddsList(mapList);
                basketballMatchVOList.add(basketballMatchVO);
            }
            basketball.setBasketballMatchList(basketballMatchVOList);
            basketballList.add(basketball);
        }
        commonList.setVoList(basketballList);
        return commonList;
    }

    @Override
    public BallCalculationVO calculation(BallCalculationDTO ballCalculation) {
        return BasketballUtil.calculation(ballCalculation.getBasketballMatchList(), ballCalculation.getMultiple(), ballCalculation.getPssTypeList());
    }

    @Override
    @TenantIgnore
    public BaseVO award() {
        log.debug("=======>[竞猜篮球]=======");
        //查询未出票的订单
        List<LotteryOrderDO> orderNotList = lotteryOrderMapper.selectList(new QueryWrapper<LotteryOrderDO>().lambda().eq(LotteryOrderDO::getState, LotteryOrderStateEnum.TO_BE_ISSUED.getKey()).eq(LotteryOrderDO::getType, LotteryOrderTypeEnum.BASKETBALL.getKey()));
        log.debug("=======>[竞猜篮球] [未出票] 数量:[{}]", orderNotList.size());
        for (LotteryOrderDO lotteryOrderDO : orderNotList) {
            //查询下注的列表
            List<RacingBallDO> racingBallList = racingBallMapper.selectBatchIds(Convert.toList(Integer.class, lotteryOrderDO.getTargetIds()));
            Boolean flag = true;
            for (RacingBallDO racingBallDO : racingBallList) {
                BasketballMatchDO basketballMatch = basketballMatchMapper.selectById(racingBallDO.getTargetId());
                //如果比赛还没有出结果直接跳出
                if (StrUtil.isBlank(basketballMatch.getAward())) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                log.error("=======>[竞猜篮球] [未出票] 订单[{}] 已开奖未出票，退票 ", lotteryOrderDO.getOrderId());
                //如果订单为出票并且结果也出了情况下进行退票操作
                UserDO userDO = userMapper.selectById(lotteryOrderDO.getUserId());
                userDO.setGold(userDO.getGold().add(lotteryOrderDO.getPrice()));
                userMapper.updateById(userDO);
                //修改订单状态
                lotteryOrderDO.setState(LotteryOrderStateEnum.REFUND.getKey());
                lotteryOrderMapper.updateById(lotteryOrderDO);
                //添加钱包记录
                addRecord(lotteryOrderDO, userDO.getTenantId());
            }
        }
        //查询篮球已经下注的订单列表
        List<LotteryOrderDO> orderList = lotteryOrderMapper.selectList(new QueryWrapper<LotteryOrderDO>().lambda().eq(LotteryOrderDO::getState, LotteryOrderStateEnum.TO_BE_AWARDED.getKey()).eq(LotteryOrderDO::getType, LotteryOrderTypeEnum.BASKETBALL.getKey()));
        log.debug("=======>[竞猜篮球] [待开奖] 数量:[{}]", orderList.size());
        Map<Integer, BasketballMatchDO> matchMap = new HashMap<>();
        for (LotteryOrderDO order : orderList) {
            //查询下注的列表
            List<RacingBallDO> racingBallList = racingBallMapper.selectBatchIds(Convert.toList(Integer.class, order.getTargetIds()));
            //用戶下注列表
            List<BasketballMatchDTO> basketballMatchList = new ArrayList<>();
            //每场比赛出奖比赛列表
            List<String> list = new ArrayList<>();
            Boolean flag = true;

            Map<String, BasketballMatchDO> resultMatch = new HashMap<>();
            for (RacingBallDO racingBallDO : racingBallList) {
                //下注結果組成list
                basketballMatchList.add(JSONUtil.toBean(racingBallDO.getContent(), BasketballMatchDTO.class));
                //查询下注对应的比赛赛果
                BasketballMatchDO basketballMatch = null;
                if (matchMap.get(racingBallDO.getTargetId()) != null) {
                    basketballMatch = matchMap.get(racingBallDO.getTargetId());
                } else {
                    basketballMatch = basketballMatchMapper.selectById(racingBallDO.getTargetId());
                    matchMap.put(racingBallDO.getTargetId(), basketballMatch);
                }
                //如果比赛还没有出结果直接跳出
                if (StrUtil.isBlank(basketballMatch.getAward())) {
                    log.info("=======>[竞猜篮球] [待开奖]  订单 [{}] 赛事 [{}]  未开奖 ", order.getOrderId(), basketballMatch.getNumber());
                    flag = false;
                    break;
                }
                list.add(basketballMatch.getAward() + "," + basketballMatch.getHalfFullCourt());
                resultMatch.put(basketballMatch.getNumber(), basketballMatch);
            }
            if (flag) {
                //对schemeDetails兑奖
                if (StringUtils.isBlank(order.getSchemeDetails())) {
                    log.error("============订单 [{}] 没有具体schemeDetail 不参与兑派奖==========", order.getOrderId());
                    continue;
                }
                List<SportSchemeDetailsListVO> listVOList = JSONUtil.toList(order.getSchemeDetails(), SportSchemeDetailsListVO.class);
                BasketballUtil.awardSchemeDetails(listVOList, resultMatch);
                //计算用户有没有中奖，中奖了把每一注的金额进行累加在返回
                double price = listVOList.stream().filter(item -> item.isAward()).mapToDouble(item -> Double.valueOf(item.getMoney())).sum();
                //反向保存一下数据
                order.setSchemeDetails(JSON.toJSONString(listVOList));
                //等于0相当于没有中奖
                log.info("=======>[竞猜篮球] [待开奖]  订单 [{}] 中奖金额 【{}】 ", order.getOrderId(), price);
                if (price == 0) {
                    log.info("=======>[竞猜篮球] [未中奖]  订单 [{}]  ", order.getOrderId());
                    order.setState(LotteryOrderStateEnum.FAIL_TO_WIN.getKey());
                } else {
                    documentaryCommissionHelper.processCommiss("竞猜篮球", order, price);
                }
                order.setUpdateTime(new Date());
                lotteryOrderMapper.updateById(order);
            }
        }
        return new BaseVO();
    }

    private void addRecord(LotteryOrderDO lotteryOrder, Integer tenantId) {
        PayOrderDO payOrder = new PayOrderDO();
        payOrder.setOrderId(OrderNumberGenerationUtil.getOrderId());
        payOrder.setType(PayOrderTypeEnum.BASKETBALL_REFUND.getKey());
        payOrder.setState(PayOrderStateEnum.PAYMENT.getKey());
        payOrder.setCreateTime(new Date());
        payOrder.setTenantId(tenantId);
        payOrder.setUpdateTime(new Date());
        payOrder.setPayType(PayTypeEnum.APP.getKey());
        payOrder.setUserId(lotteryOrder.getUserId());
        payOrder.setPrice(lotteryOrder.getPrice());
        payOrderMapper.insert(payOrder);
    }
}
