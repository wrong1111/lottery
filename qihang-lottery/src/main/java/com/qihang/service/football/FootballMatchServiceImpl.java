package com.qihang.service.football;

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
import com.qihang.common.util.reward.FootballUtil;
import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.controller.football.dto.FootballMatchDTO;
import com.qihang.controller.football.vo.FootballMatchVO;
import com.qihang.controller.football.vo.FootballVO;
import com.qihang.controller.order.admin.lottery.vo.SportSchemeDetailsListVO;
import com.qihang.controller.racingball.app.dto.BallCalculationDTO;
import com.qihang.controller.racingball.app.vo.BallCalculationVO;
import com.qihang.domain.documentary.DocumentaryDO;
import com.qihang.domain.documentary.DocumentaryUserDO;
import com.qihang.domain.football.FootballMatchDO;
import com.qihang.domain.order.LotteryOrderDO;
import com.qihang.domain.order.LotteryTicketDO;
import com.qihang.domain.order.PayOrderDO;
import com.qihang.domain.racingball.RacingBallDO;
import com.qihang.domain.user.UserDO;
import com.qihang.enumeration.ball.BettingStateEnum;
import com.qihang.enumeration.order.lottery.LotteryOrderStateEnum;
import com.qihang.enumeration.order.lottery.LotteryOrderTypeEnum;
import com.qihang.enumeration.order.pay.PayOrderStateEnum;
import com.qihang.enumeration.order.pay.PayOrderTypeEnum;
import com.qihang.enumeration.order.pay.PayTypeEnum;
import com.qihang.mapper.documentary.DocumentaryMapper;
import com.qihang.mapper.documentary.DocumentaryUserMapper;
import com.qihang.mapper.football.FootballMatchMapper;
import com.qihang.mapper.order.LotteryOrderMapper;
import com.qihang.mapper.order.LotteryTicketMapper;
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
 * 足彩 胜负平表 服务实现类
 *
 * @author bright
 * @since 2022-10-05
 */
@Service
@Slf4j
public class FootballMatchServiceImpl extends ServiceImpl<FootballMatchMapper, FootballMatchDO> implements IFootballMatchService {

    @Resource
    private FootballMatchMapper footballMatchMapper;

    @Resource
    private LotteryOrderMapper lotteryOrderMapper;

    @Resource
    private RacingBallMapper racingBallMapper;

    @Resource
    private PayOrderMapper payOrderMapper;

    @Resource
    private UserMapper userMapper;
    @Resource
    private DocumentaryMapper documentaryMapper;
    @Resource
    private DocumentaryUserMapper documentaryUserMapper;

    @Resource
    DocumentaryCommissionHelper documentaryCommissionHelper;

    @Resource
    LotteryTicketMapper lotteryTicketMapper;

    @Override
    public CommonListVO<FootballVO> footballMatchList() {
        CommonListVO<FootballVO> commonList = new CommonListVO<>();
        List<FootballVO> footballList = new ArrayList<>();
        //小于当前时间 的也不能投注了，
        //wyong edit 2023-11-12
        List<FootballMatchDO> footballWinEvenLoseDist = footballMatchMapper.selectList(new QueryWrapper<FootballMatchDO>().lambda().eq(FootballMatchDO::getState, BettingStateEnum.YES.getKey()).gt(FootballMatchDO::getDeadline, new Date()));
        Map<String, List<FootballMatchDO>> map = footballWinEvenLoseDist.stream().collect(Collectors.groupingBy(FootballMatchDO::getStartTime));
        //对map的key进行排序
        map = map.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        Integer id = 0;
        for (Map.Entry<String, List<FootballMatchDO>> entry : map.entrySet()) {
            FootballVO football = new FootballVO();
            football.setStartTime(entry.getKey());
            football.setCount(entry.getValue().size());
            List<FootballMatchVO> footballMatchList = new ArrayList<>();
            //排序
            List<FootballMatchDO> list = entry.getValue();
            list.sort(Comparator.comparing(FootballMatchDO::getNumber));
            for (FootballMatchDO footballMatch : list) {
                FootballMatchVO footballMatchVO = new FootballMatchVO();
                BeanUtils.copyProperties(footballMatch, footballMatchVO);
                //默认选择项为0，方便前端好控制选择了几项
                footballMatchVO.setChoiceCount(0);
                List<Map<String, Object>> mapList = new ArrayList<>();
                Map<String, Object> mapObj = null;

                /*============================让球组成list<map>结构==============================*/
                if (StrUtil.isNotBlank(footballMatch.getLetOdds())) {
                    String[] letArr = footballMatch.getLetOdds().split(",");
                    for (int i = 0; i < letArr.length; i++) {
                        mapObj = new HashMap<>(3);
                        mapObj.put("id", ++id);
                        mapObj.put("odds", letArr[i]);
                        mapObj.put("active", false);
                        if (i == 0) {
                            mapObj.put("describe", "胜");
                        } else if (i == 1) {
                            mapObj.put("describe", "平");
                        } else if (i == 2) {
                            mapObj.put("describe", "负");
                        }
                        mapObj.put("index", i);
                        mapList.add(mapObj);
                    }
                }
                //添加对对象中
                footballMatchVO.setLetOddsList(mapList);

                /*============================不让球组成list<map>===============================*/

                //清空原来的数据
                mapList = new ArrayList<>();
                if (StrUtil.isNotBlank(footballMatch.getNotLetOdds())) {
                    String[] notLetArr = footballMatch.getNotLetOdds().split(",");
                    for (int i = 0; i < notLetArr.length; i++) {
                        mapObj = new HashMap<>(3);
                        mapObj.put("id", ++id);
                        mapObj.put("odds", notLetArr[i]);
                        mapObj.put("active", false);
                        if (i == 0) {
                            mapObj.put("describe", "胜");
                        } else if (i == 1) {
                            mapObj.put("describe", "平");
                        } else if (i == 2) {
                            mapObj.put("describe", "负");
                        }
                        mapObj.put("index", i);
                        mapList.add(mapObj);
                    }
                }
                //添加对对象中
                footballMatchVO.setNotLetOddsList(mapList);

                /*==============================总进球组成list<map>============================*/

                //清空原来的数据
                mapList = new ArrayList<>();
                if (StrUtil.isNotBlank(footballMatch.getGoalOdds())) {
                    String[] goalItems = {"0", "1", "2", "3", "4", "5", "6", "7+"};
                    String[] goalArr = footballMatch.getGoalOdds().split(",");
                    for (int i = 0; i < goalArr.length; i++) {
                        mapObj = new HashMap<>(3);
                        mapObj.put("id", ++id);
                        mapObj.put("describe", goalItems[i]);
                        mapObj.put("odds", goalArr[i]);
                        mapObj.put("active", false);
                        mapObj.put("index", i);
                        mapList.add(mapObj);
                    }
                }
                //添加对对象中
                footballMatchVO.setGoalOddsList(mapList);


                /*==============================总比分组成list<map>============================*/

                //清空原来的数据
                mapList = new ArrayList<>();
                if (StrUtil.isNotBlank(footballMatch.getScoreOdds())) {
                    String[] scoreArr = footballMatch.getScoreOdds().split(",");
                    String[] scoreItems = {"1:0", "2:0", "2:1", "3:0", "3:1", "3:2", "4:0", "4:1", "4:2", "5:0", "5:1", "5:2", "胜其他", "0:0", "1:1", "2:2", "3:3", "平其他", "0:1", "0:2", "1:2", "0:3", "1:3", "2:3", "0:4", "1:4", "2:4", "0:5", "1:5", "2:5", "负其他"};
                    for (int i = 0; i < scoreArr.length; i++) {
                        mapObj = new HashMap<>(3);
                        mapObj.put("id", ++id);
                        mapObj.put("describe", scoreItems[i]);
                        mapObj.put("odds", scoreArr[i]);
                        mapObj.put("active", false);
                        mapObj.put("index", i);
                        mapList.add(mapObj);
                    }
                }
                //添加对对象中
                footballMatchVO.setScoreOddsList(mapList);


                /*==============================半全场组成list<map>============================*/

                //清空原来的数据
                mapList = new ArrayList<>();
                if (StrUtil.isNotBlank(footballMatch.getHalfWholeOdds())) {
                    String[] halfWholeItems = {"胜-胜", "胜-平", "胜-负", "平-胜", "平-平", "平-负", "负-胜", "负-平", "负-负"};
                    String[] halfWholeOddsArr = footballMatch.getHalfWholeOdds().split(",");
                    for (int i = 0; i < halfWholeOddsArr.length; i++) {
                        mapObj = new HashMap<>(3);
                        mapObj.put("id", ++id);
                        mapObj.put("describe", halfWholeItems[i]);
                        mapObj.put("odds", halfWholeOddsArr[i]);
                        mapObj.put("active", false);
                        mapObj.put("index", i);
                        mapList.add(mapObj);
                    }
                }
                //添加对对象中
                footballMatchVO.setHalfWholeOddsList(mapList);

                footballMatchList.add(footballMatchVO);
            }
            football.setFootballMatchList(footballMatchList);
            footballList.add(football);
        }
        commonList.setVoList(footballList);
        return commonList;
    }

    @Override
    public BallCalculationVO calculation(BallCalculationDTO ballCalculation) {
        return FootballUtil.calculation(ballCalculation.getFootballMatchList(), ballCalculation.getMultiple(), ballCalculation.getPssTypeList());
    }

    @Override
    @TenantIgnore
    public BaseVO award() {
        //查询未出票的订单
        log.debug("=======>[竞猜足球]=======");
        List<LotteryOrderDO> orderNotList = lotteryOrderMapper.selectList(new QueryWrapper<LotteryOrderDO>().lambda().eq(LotteryOrderDO::getState, LotteryOrderStateEnum.TO_BE_ISSUED.getKey()).eq(LotteryOrderDO::getType, LotteryOrderTypeEnum.FOOTBALL.getKey()));
        log.debug("=======>[竞猜足球][未出票] 记录数: {} ", orderNotList.size());
        for (LotteryOrderDO lotteryOrderDO : orderNotList) {
            log.debug("=======>[竞猜足球][未出票] 订单[{}]  彩种id:[{}] 验证是否已开奖  ", lotteryOrderDO.getOrderId(), lotteryOrderDO.getType());
            //查询下注的列表
            List<RacingBallDO> racingBallList = racingBallMapper.selectBatchIds(Convert.toList(Integer.class, lotteryOrderDO.getTargetIds()));
            Boolean flag = true;
            for (RacingBallDO racingBallDO : racingBallList) {
                FootballMatchDO footballMatch = footballMatchMapper.selectById(racingBallDO.getTargetId());
                //如果比赛还没有出结果直接跳出
                if (StrUtil.isBlank(footballMatch.getAward())) {
                    log.info("=======>[竞猜足球][未出票] 订单[{}]  彩种id:[{}] 赛事:[{}] 未有赛果 <<<<<<<< ", lotteryOrderDO.getOrderId(), lotteryOrderDO.getType(), footballMatch.getNumber());
                    flag = false;
                    break;
                }
            }
            if (flag) {
                log.error("=======>[竞猜足球][未出票] 订单[{}]  彩种id:[{}] 已有赛果未出票，退票 <<<<<<<< ", lotteryOrderDO.getOrderId(), lotteryOrderDO.getType());
                //如果订单为出票并且结果也出了情况下进行退票操作
                UserDO userDO = userMapper.selectById(lotteryOrderDO.getUserId());
                userDO.setGold(userDO.getGold().add(lotteryOrderDO.getPrice()));
                userMapper.updateById(userDO);
                //修改订单状态
                lotteryOrderDO.setState(LotteryOrderStateEnum.REFUND.getKey());
                lotteryOrderMapper.updateById(lotteryOrderDO);
                //添加钱包记录
                addRecord(lotteryOrderDO);
            }
        }
        //查询足球已经下注的订单列表
        List<LotteryOrderDO> orderList = lotteryOrderMapper.selectList(new QueryWrapper<LotteryOrderDO>().lambda().eq(LotteryOrderDO::getState, LotteryOrderStateEnum.TO_BE_AWARDED.getKey()).eq(LotteryOrderDO::getType, LotteryOrderTypeEnum.FOOTBALL.getKey()));
        log.debug("=======>[竞猜足球][待开奖]  数量:[{}]  ", orderList.size());
        Map<Integer, FootballMatchDO> matchMap = new HashMap<>();
        for (LotteryOrderDO order : orderList) {
            log.debug("=======>[竞猜足球][待开奖]  订单 :[{}]  start  ", order.getOrderId());
            //查询下注的列表
            List<RacingBallDO> racingBallList = racingBallMapper.selectBatchIds(Convert.toList(Integer.class, order.getTargetIds()));
            //用戶下注列表
            List<FootballMatchDTO> footballMatchList = new ArrayList<>();
            //每场比赛出奖比赛列表
            Map<String, String> resultMatch = new HashMap<>();
            Boolean flag = true;
            for (RacingBallDO racingBallDO : racingBallList) {
                //下注結果組成list
                footballMatchList.add(JSONUtil.toBean(racingBallDO.getContent(), FootballMatchDTO.class));
                //查询下注对应的比赛赛果
                FootballMatchDO footballMatch = null;
                if (matchMap.get(racingBallDO.getTargetId()) != null) {
                    footballMatch = matchMap.get(racingBallDO.getTargetId());
                } else {
                    footballMatch = footballMatchMapper.selectById(racingBallDO.getTargetId());
                    matchMap.put(racingBallDO.getTargetId(), footballMatch);
                }
                if (null == footballMatch) {
                    log.error("ERROR=======>[竞猜足球][待开奖]  订单 :[{}]  赛事[{}] 不存在，订单异常 <<<<<<<< ", order.getOrderId(), racingBallDO.getTargetId());
                    flag = false;
                    break;
                }
                //如果比赛还没有出结果直接跳出
                if (StrUtil.isBlank(footballMatch.getAward())) {
                    log.info("=======>[竞猜足球][待开奖]  订单 :[{}]  赛事[{}]未开奖 <<<<<<<< ", order.getOrderId(), footballMatch.getNumber());
                    flag = false;
                    break;
                }
                String award = buildResult(footballMatch.getHalfFullCourt(), footballMatch.getAward());
                resultMatch.put(footballMatch.getNumber(), award);
            }
            if (flag) {
                //对schemeDetails兑奖
                if (StringUtils.isBlank(order.getSchemeDetails())) {
                    log.error("============订单 [{}] 没有具体schemeDetail 不参与兑派奖==========", order.getOrderId());
                    continue;
                }
//                List<SportSchemeDetailsListVO> listVOList = JSONUtil.toList(order.getSchemeDetails(), SportSchemeDetailsListVO.class);
//                FootballUtil.awardSchemeDetails(listVOList, resultMatch);
//                //计算用户有没有中奖，中奖了把每一注的金额进行累加在返回
//                double price = listVOList.stream().filter(item -> item.isAward()).mapToDouble(item -> Double.valueOf(item.getMoney())).sum();
//                //反向保存一下数据
//                order.setSchemeDetails(JSON.toJSONString(listVOList));
                List<LotteryTicketDO> lotteryTicketDOS = lotteryTicketMapper.selectList(new QueryWrapper<LotteryTicketDO>().lambda().eq(LotteryTicketDO::getOrderId, order.getOrderId()));
                double price = FootballUtil.award(lotteryTicketDOS, resultMatch);
                for (LotteryTicketDO lotteryTicketDO : lotteryTicketDOS) {
                    lotteryTicketMapper.updateById(lotteryTicketDO);
                }
                //计算用户有没有中奖，中奖了把每一注的金额进行累加在返回
                //等于0相当于没有中奖
                log.debug("=======>[竞猜足球][待开奖]  订单 :[{}]  中奖【{}】  ", order.getOrderId(), price);
                if (price == 0) {
                    log.info("=======>[竞猜足球][待开奖]  订单 :[{}]  未中奖 ", order.getOrderId());
                    order.setState(LotteryOrderStateEnum.FAIL_TO_WIN.getKey());
                } else {
                    //具体处理
                    documentaryCommissionHelper.processCommiss("竞猜足球", order, price);
                }
                order.setUpdateTime(new Date());
                lotteryOrderMapper.updateById(order);
            }


        }
        return new BaseVO();
    }

    @TenantIgnore
    @Override
    public BaseVO openAward(LotteryOrderDO order) {
        //查询下注的列表
        List<RacingBallDO> racingBallList = racingBallMapper.selectBatchIds(Convert.toList(Integer.class, order.getTargetIds()));
        //用戶下注列表
        List<FootballMatchDTO> footballMatchList = new ArrayList<>();
        //每场比赛出奖比赛列表
        Map<String, String> resultMatch = new HashMap<>();
        Boolean flag = true;
        for (RacingBallDO racingBallDO : racingBallList) {
            //下注結果組成list
            footballMatchList.add(JSONUtil.toBean(racingBallDO.getContent(), FootballMatchDTO.class));
            //查询下注对应的比赛赛果
            FootballMatchDO footballMatch = footballMatchMapper.selectById(racingBallDO.getTargetId());

            if (null == footballMatch) {
                log.error("ERROR=======>[竞猜足球][待开奖]  订单 :[{}]  赛事[{}] 不存在，订单异常 <<<<<<<< ", order.getOrderId(), racingBallDO.getTargetId());
                flag = false;
                break;
            }
            //如果比赛还没有出结果直接跳出
            if (StrUtil.isBlank(footballMatch.getAward())) {
                log.info("=======>[竞猜足球][待开奖]  订单 :[{}]  赛事[{}]未开奖 <<<<<<<< ", order.getOrderId(), footballMatch.getNumber());
                flag = false;
                break;
            }
            String award = buildResult(footballMatch.getHalfFullCourt(), footballMatch.getAward());
            resultMatch.put(footballMatch.getNumber(), award);
        }
        double price = 0d;
        if (flag) {
            //对schemeDetails兑奖
//            if (StringUtils.isBlank(order.getSchemeDetails())) {
//                log.error("============订单 [{}] 没有具体schemeDetail 不参与兑派奖==========", order.getOrderId());
//                return BaseVO.builder().success(false).errorMsg("订单异常，没有具体schemeDetail").build();
//            }
//                List<SportSchemeDetailsListVO> listVOList = JSONUtil.toList(order.getSchemeDetails(), SportSchemeDetailsListVO.class);
//                FootballUtil.awardSchemeDetails(listVOList, resultMatch);
//                //计算用户有没有中奖，中奖了把每一注的金额进行累加在返回
//                double price = listVOList.stream().filter(item -> item.isAward()).mapToDouble(item -> Double.valueOf(item.getMoney())).sum();
//                //反向保存一下数据
//                order.setSchemeDetails(JSON.toJSONString(listVOList));
            List<LotteryTicketDO> lotteryTicketDOS = lotteryTicketMapper.selectList(new QueryWrapper<LotteryTicketDO>().lambda().eq(LotteryTicketDO::getOrderId, order.getOrderId()));
            price = FootballUtil.award(lotteryTicketDOS, resultMatch);
            for (LotteryTicketDO lotteryTicketDO : lotteryTicketDOS) {
                lotteryTicketMapper.updateById(lotteryTicketDO);
            }
            //计算用户有没有中奖，中奖了把每一注的金额进行累加在返回
            //等于0相当于没有中奖
            log.debug("=======>[竞猜足球][待开奖]  订单 :[{}]  中奖【{}】  ", order.getOrderId(), price);
            if (price == 0) {
                log.info("=======>[竞猜足球][待开奖]  订单 :[{}]  未中奖 ", order.getOrderId());
                order.setState(LotteryOrderStateEnum.FAIL_TO_WIN.getKey());
            } else {
                //具体处理
                documentaryCommissionHelper.processCommiss("竞猜足球", order, price);
            }
            order.setUpdateTime(new Date());
            lotteryOrderMapper.updateById(order);
        }
        return BaseVO.builder().success(true).errorMsg("奖金[" + price + "]").build();
    }

    /***
     * 负,胜,7,负-负,负其它
     * @param score
     * @param award
     * @return
     */
    private String buildResult(String score, String award) {
        if ("延期".equals(score)) {
            return score;
        }
        String[] bif = StringUtils.split(score, ":");
        String[] awards = StringUtils.split(award, ",");
        if (Integer.valueOf(bif[0]) + Integer.valueOf(bif[1]) > 7 || Integer.valueOf(bif[0]) > 5 || Integer.valueOf(bif[1]) > 5) {
            if (Integer.valueOf(bif[0]) > Integer.valueOf(bif[1]))
                awards[4] = "胜其他";
            else if (Integer.valueOf(bif[0]) == Integer.valueOf(bif[1]))
                awards[4] = "平其他";
            else if (Integer.valueOf(bif[0]) < Integer.valueOf(bif[1]))
                awards[4] = "负其他";
        }
        if (awards[1].length() == 1) {
            awards[1] = "让" + awards[1];
        }
//        int offset = Integer.valueOf(letball.trim());
//        if (offset + Integer.valueOf(bif[0]) > Integer.valueOf(bif[1])) {
//            awards[1] = "胜";
//        } else if (offset + Integer.valueOf(bif[0]) < Integer.valueOf(bif[1])) {
//            awards[1] = "负";
//        } else if (offset + Integer.valueOf(bif[0]) == Integer.valueOf(bif[1])) {
//            awards[1] = "平";
//        }
        return StringUtils.join(awards, ",");
    }

    private void addRecord(LotteryOrderDO lotteryOrder) {
        PayOrderDO payOrder = new PayOrderDO();
        payOrder.setOrderId(OrderNumberGenerationUtil.getOrderId());
        payOrder.setType(PayOrderTypeEnum.FOOTBALL_REFUND.getKey());
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
