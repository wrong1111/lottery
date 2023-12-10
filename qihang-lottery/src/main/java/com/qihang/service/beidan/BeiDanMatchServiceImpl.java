package com.qihang.service.beidan;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qihang.annotation.TenantIgnore;
import com.qihang.common.util.order.OrderNumberGenerationUtil;
import com.qihang.common.util.reward.BeiDanUtil;
import com.qihang.common.util.reward.FootballUtil;
import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.controller.beidan.dto.BeiDanMatchDTO;
import com.qihang.controller.beidan.vo.BeiDanMatchVO;
import com.qihang.controller.beidan.vo.BeiDanVO;
import com.qihang.controller.order.admin.lottery.vo.SportSchemeDetailsListVO;
import com.qihang.controller.racingball.app.dto.BallCalculationDTO;
import com.qihang.controller.racingball.app.vo.BallCalculationVO;
import com.qihang.domain.beidan.BeiDanMatchDO;
import com.qihang.domain.beidan.BeiDanSFGGMatchDO;
import com.qihang.domain.documentary.DocumentaryDO;
import com.qihang.domain.documentary.DocumentaryUserDO;
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
import com.qihang.mapper.beidan.BeiDanMatchMapper;
import com.qihang.mapper.beidan.BeiDanSfggMatchMapper;
import com.qihang.mapper.documentary.DocumentaryMapper;
import com.qihang.mapper.documentary.DocumentaryUserMapper;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bright
 * @since 2022-11-02
 */
@Slf4j
@Service
public class BeiDanMatchServiceImpl extends ServiceImpl<BeiDanMatchMapper, BeiDanMatchDO> implements IBeiDanMatchService {

    @Resource
    private BeiDanMatchMapper beiDanMatchMapper;

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

    @Resource
    BeiDanSfggMatchMapper beiDanSfggMatchMapper;

    @Resource
    LotteryTicketMapper lotteryTicketMapper;

    @Override
    public CommonListVO<BeiDanVO> beiDanMatchList() {
        long c = System.currentTimeMillis();
        CommonListVO<BeiDanVO> commonList = new CommonListVO<>();
        List<BeiDanVO> beiDanList = new ArrayList<>();
        //小于当前时间 不展示
        List<BeiDanMatchDO> beiDanMatchDataList = beiDanMatchMapper.selectList(new QueryWrapper<BeiDanMatchDO>().lambda().eq(BeiDanMatchDO::getState, BettingStateEnum.YES.getKey()).gt(BeiDanMatchDO::getDeadline, new Date()));
        Map<String, List<BeiDanMatchDO>> map = beiDanMatchDataList.stream().collect(Collectors.groupingBy(BeiDanMatchDO::getStartTime));
        //对map的key进行排序
        map = map.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        Integer id = 0;
        for (Map.Entry<String, List<BeiDanMatchDO>> entry : map.entrySet()) {
            BeiDanVO beiDan = new BeiDanVO();
            beiDan.setStartTime(entry.getKey());
            beiDan.setCount(entry.getValue().size());
            List<BeiDanMatchVO> beiDanMatchList = new ArrayList<>();
            //排序
            List<BeiDanMatchDO> list = entry.getValue();
            list = list.stream().sorted(Comparator.comparing(data -> Integer.valueOf(data.getNumber()))).collect(Collectors.toList());
            for (BeiDanMatchDO beiDanMatch : list) {
                BeiDanMatchVO beiDanMatchVO = new BeiDanMatchVO();
                BeanUtils.copyProperties(beiDanMatch, beiDanMatchVO);
                //默认选择项为0，方便前端好控制选择了几项
                beiDanMatchVO.setChoiceCount(0);
                List<Map<String, Object>> mapList = new ArrayList<>();
                Map<String, Object> mapObj = null;

                /*============================让球组成list<map>结构==============================*/
                if (StrUtil.isNotBlank(beiDanMatch.getLetOdds())) {
                    String[] letArr = beiDanMatch.getLetOdds().split(",");
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
                beiDanMatchVO.setLetOddsList(mapList);

                /*==============================总进球组成list<map>============================*/

                //清空原来的数据
                mapList = new ArrayList<>();
                if (StrUtil.isNotBlank(beiDanMatch.getGoalOdds())) {
                    String[] goalItems = {"0", "1", "2", "3", "4", "5", "6", "7+"};
                    String[] goalArr = beiDanMatch.getGoalOdds().split(",");
                    for (int i = 0; i < goalArr.length; i++) {
                        mapObj = new HashMap<>(3);
                        mapObj.put("id", ++id);
                        mapObj.put("describe", goalItems[i]);
                        if (goalArr[i].equals("--")) {
                            mapObj.put("odds", 0);
                        } else {
                            mapObj.put("odds", goalArr[i]);
                        }
                        mapObj.put("active", false);
                        mapObj.put("index", i);
                        mapList.add(mapObj);
                    }
                }
                //添加对对象中
                beiDanMatchVO.setGoalOddsList(mapList);


                /*==============================总比分组成list<map>============================*/

                //清空原来的数据
                mapList = new ArrayList<>();
                if (StrUtil.isNotBlank(beiDanMatch.getScoreOdds())) {
                    String[] scoreArr = beiDanMatch.getScoreOdds().split(",");
                    String[] scoreItems = {"胜其他", "1:0", "2:0", "2:1", "3:0", "3:1", "3:2", "4:0", "4:1", "4:2", "平其他", "0:0", "1:1", "2:2", "3:3", "负其他", "0:1", "0:2", "1:2", "0:3", "1:3", "2:3", "0:4", "1:4", "2:4"};
                    for (int i = 0; i < scoreArr.length; i++) {
                        mapObj = new HashMap<>(3);
                        mapObj.put("id", ++id);
                        mapObj.put("describe", scoreItems[i]);
                        if (scoreArr[i].equals("--")) {
                            mapObj.put("odds", 0);
                        } else {
                            mapObj.put("odds", scoreArr[i]);
                        }
                        mapObj.put("active", false);
                        mapObj.put("index", i);
                        mapList.add(mapObj);
                    }
                }
                //添加对对象中
                beiDanMatchVO.setScoreOddsList(mapList);


                /*==============================半全场组成list<map>============================*/

                //清空原来的数据
                mapList = new ArrayList<>();
                if (StrUtil.isNotBlank(beiDanMatch.getHalfWholeOdds())) {
                    String[] halfWholeItems = {"胜-胜", "胜-平", "胜-负", "平-胜", "平-平", "平-负", "负-胜", "负-平", "负-负"};
                    String[] halfWholeOddsArr = beiDanMatch.getHalfWholeOdds().split(",");
                    for (int i = 0; i < halfWholeOddsArr.length; i++) {
                        mapObj = new HashMap<>(3);
                        mapObj.put("id", ++id);
                        mapObj.put("describe", halfWholeItems[i]);
                        if (halfWholeOddsArr[i].equals("--")) {
                            mapObj.put("odds", 0);
                        } else {
                            mapObj.put("odds", halfWholeOddsArr[i]);
                        }
                        mapObj.put("active", false);
                        mapObj.put("index", i);
                        mapList.add(mapObj);
                    }
                }
                //添加对对象中
                beiDanMatchVO.setHalfWholeOddsList(mapList);

                /*==============================单双组成list<map>============================*/

                //清空原来的数据
                mapList = new ArrayList<>();
                if (StrUtil.isNotBlank(beiDanMatch.getHalfWholeOdds())) {
                    String[] oddEvenItems = {"上单", "上双", "下单", "下双"};
                    String[] oddEvenItemArr = beiDanMatch.getOddEvenOdds().split(",");
                    for (int i = 0; i < oddEvenItemArr.length; i++) {
                        mapObj = new HashMap<>(3);
                        mapObj.put("id", ++id);
                        mapObj.put("describe", oddEvenItems[i]);
                        if (oddEvenItemArr[i].equals("--")) {
                            mapObj.put("odds", 0);
                        } else {
                            mapObj.put("odds", oddEvenItemArr[i]);
                        }
                        mapObj.put("active", false);
                        mapObj.put("index", i);
                        mapList.add(mapObj);
                    }
                }
                //添加对对象中
                beiDanMatchVO.setOddEvenOdds(mapList);


                beiDanMatchList.add(beiDanMatchVO);
            }
            beiDan.setBeiDanMatchList(beiDanMatchList);
            beiDanList.add(beiDan);
        }
        log.info("=======>[北单]=======cost {}", (System.currentTimeMillis() - c));
        commonList.setVoList(beiDanList);
        return commonList;
    }

    @Override
    public BallCalculationVO calculation(BallCalculationDTO ballCalculation) {
        return BeiDanUtil.calculation(ballCalculation.getBeiDanMatchList(), ballCalculation.getMultiple(), ballCalculation.getPssTypeList());
    }

    @Override
    @TenantIgnore
    public BaseVO award() {
        log.debug("=======>[北单]=======");
        //查询未出票的订单
        List<LotteryOrderDO> orderNotList = lotteryOrderMapper.selectList(new QueryWrapper<LotteryOrderDO>().lambda().eq(LotteryOrderDO::getState, LotteryOrderStateEnum.TO_BE_ISSUED.getKey()).eq(LotteryOrderDO::getType, LotteryOrderTypeEnum.SINGLE.getKey()));
        log.debug("=======>[北单][未出票] 记录数: {} ", orderNotList.size());
        for (LotteryOrderDO lotteryOrderDO : orderNotList) {
            //胜负过关
            Boolean flag = true;
            //查询下注的列表
            List<RacingBallDO> racingBallList = racingBallMapper.selectBatchIds(Convert.toList(Integer.class, lotteryOrderDO.getTargetIds()));
            if (lotteryOrderDO.getType().equals(LotteryOrderTypeEnum.SIGLE_SFGG.getKey())) {
                for (RacingBallDO racingBallDO : racingBallList) {
                    BeiDanSFGGMatchDO beiDanMatch = beiDanSfggMatchMapper.selectById(racingBallDO.getTargetId());
                    //如果比赛还没有出结果直接跳出
                    if (StrUtil.isBlank(beiDanMatch.getAward())) {
                        flag = false;
                        break;
                    }
                }
            } else {
                for (RacingBallDO racingBallDO : racingBallList) {
                    BeiDanMatchDO beiDanMatch = beiDanMatchMapper.selectById(racingBallDO.getTargetId());
                    //如果比赛还没有出结果直接跳出
                    if (StrUtil.isBlank(beiDanMatch.getAward())) {
                        flag = false;
                        break;
                    }
                }
            }
            if (flag) {
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

        awardBeidan();
        awardBeidanSfgg();
        return new BaseVO();
    }

    @Override
    public BaseVO openAward(LotteryOrderDO order) {
        //查询下注的列表
        List<RacingBallDO> racingBallList = racingBallMapper.selectBatchIds(Convert.toList(Integer.class, order.getTargetIds()));
        //用戶下注列表
        List<BeiDanMatchDTO> beiDanMatchList = new ArrayList<>();
        //每场比赛出奖比赛列表
        List<String> list = new ArrayList<>();
        //出奖赔率
        List<String> bonusOddsList = new ArrayList<>();
        Boolean flag = true;
        Map<String, String> resultMatch = new HashMap<>();
        Map<String, String> bonusMap = new HashMap<>();
        for (RacingBallDO racingBallDO : racingBallList) {
            //下注結果組成list
            beiDanMatchList.add(JSONUtil.toBean(racingBallDO.getContent(), BeiDanMatchDTO.class));
            //查询下注对应的比赛赛果
            BeiDanMatchDO beiDanMatch = beiDanMatchMapper.selectById(racingBallDO.getTargetId());
            //如果比赛和赔率还没有出结果直接跳出 由于赔率不是同时出的，所有也需要做如下判断处理
            if (StrUtil.isBlank(beiDanMatch.getAward()) || beiDanMatch.getBonusOdds().indexOf("-") != -1) {
                log.debug("=======>[北单][待开奖] 订单 [{}] 赛事[{}]未出赛果 或赔率 ", order.getOrderId(), beiDanMatch.getNumber());
                flag = false;
                break;
            }
            list.add(beiDanMatch.getAward());
            bonusOddsList.add(beiDanMatch.getBonusOdds());
            resultMatch.put(beiDanMatch.getNumber(), beiDanMatch.getAward());
            bonusMap.put(beiDanMatch.getNumber(), beiDanMatch.getBonusOdds());
        }
        double price = 0d;
        if (flag) {
            //对schemeDetails兑奖
//            if (StringUtils.isBlank(order.getSchemeDetails())) {
//                log.error("============订单 [{}] 没有具体schemeDetail 不参与兑派奖==========", order.getOrderId());
//                continue;
//            }

            /***
             //改为从lotteryTicket表进行兑奖
             //                List<SportSchemeDetailsListVO> listVOList = JSONUtil.toList(order.getSchemeDetails(), SportSchemeDetailsListVO.class);
             //                BeiDanUtil.awardSchemeDetails(listVOList, resultMatch, bonusMap);
             //计算用户有没有中奖，中奖了把每一注的金额进行累加在返回
             double price = 0d;//listVOList.stream().filter(item -> item.isAward()).mapToDouble(item -> Double.valueOf(item.getMoney())).sum();
             //反向保存一下数据
             //order.setSchemeDetails(JSON.toJSONString(listVOList));
             **/
            List<LotteryTicketDO> lotteryOrderDOS = lotteryTicketMapper.selectList(new QueryWrapper<LotteryTicketDO>().lambda().eq(LotteryTicketDO::getOrderId, order.getOrderId()));
            price = BeiDanUtil.award(lotteryOrderDOS, resultMatch, bonusMap);
            //修改lotteryTicket表中的状态
            for (LotteryTicketDO ticketDO : lotteryOrderDOS) {
                lotteryTicketMapper.updateById(ticketDO);
            }
            //等于0相当于没有中奖
            log.debug("=======>[北单][待开奖] 订单 [{}] 中奖[{}] ", order.getOrderId(), price);
            if (price == 0) {
                log.debug("=======>[北单][未中奖] 订单[{}] 未中奖  ", order.getOrderId());
                order.setState(LotteryOrderStateEnum.FAIL_TO_WIN.getKey());
            } else {
                //已经中奖
                //给订单分佣处理。
                documentaryCommissionHelper.processCommiss("北单", order, price);
            }
            order.setUpdateTime(new Date());
            lotteryOrderMapper.updateById(order);
            log.debug("=======>[北单]  订单 [{}] 中奖金额[{}] 完成 <<<<<<<<<< ", order.getOrderId(), price);
        } else {
            price = -1;

        }
        return BaseVO.builder().success(true).errorMsg("奖金[" + price + "]").build();
    }

    @Override
    public BaseVO openAwardSfgg(LotteryOrderDO order) {
        //查询下注的列表
        List<RacingBallDO> racingBallList = racingBallMapper.selectBatchIds(Convert.toList(Integer.class, order.getTargetIds()));
        //用戶下注列表
        List<BeiDanMatchDTO> beiDanMatchList = new ArrayList<>();
        //每场比赛出奖比赛列表
        List<String> list = new ArrayList<>();
        //出奖赔率
        List<String> bonusOddsList = new ArrayList<>();
        Boolean flag = true;
        Map<String, String> resultMatch = new HashMap<>();
        Map<String, String> bonusMap = new HashMap<>();
        for (RacingBallDO racingBallDO : racingBallList) {
            //下注結果組成list
            beiDanMatchList.add(JSONUtil.toBean(racingBallDO.getContent(), BeiDanMatchDTO.class));
            //查询下注对应的比赛赛果
            BeiDanSFGGMatchDO beiDanMatch = beiDanSfggMatchMapper.selectById(racingBallDO.getTargetId());
            //如果比赛和赔率还没有出结果直接跳出 由于赔率不是同时出的，所有也需要做如下判断处理
            if (StrUtil.isBlank(beiDanMatch.getAward()) || beiDanMatch.getBonusOdds().indexOf("-") != -1) {
                log.debug("=======>[北单胜负过关][待开奖] 订单 [{}] 赛事[{}]未出赛果 或赔率 ", order.getOrderId(), beiDanMatch.getNumber());
                flag = false;
                break;
            }
            list.add(beiDanMatch.getAward());
            bonusOddsList.add(beiDanMatch.getBonusOdds());
            resultMatch.put(beiDanMatch.getNumber(), beiDanMatch.getAward());
            bonusMap.put(beiDanMatch.getNumber(), beiDanMatch.getBonusOdds());
        }
        double price = 0d;
        if (flag) {
            //对schemeDetails兑奖
//            if (StringUtils.isBlank(order.getSchemeDetails())) {
//                log.error("============胜负过关订单 [{}] 没有具体schemeDetail 不参与兑派奖==========", order.getOrderId());
//                continue;
//            }
//                List<SportSchemeDetailsListVO> listVOList = JSONUtil.toList(order.getSchemeDetails(), SportSchemeDetailsListVO.class);
//                BeiDanUtil.awardSchemeDetails(listVOList, resultMatch, bonusMap);
//                //计算用户有没有中奖，中奖了把每一注的金额进行累加在返回
//                double price = listVOList.stream().filter(item -> item.isAward()).mapToDouble(item -> Double.valueOf(item.getMoney())).sum();
//                //反向保存一下数据
//                order.setSchemeDetails(JSON.toJSONString(listVOList));

            List<LotteryTicketDO> lotteryOrderDOS = lotteryTicketMapper.selectList(new QueryWrapper<LotteryTicketDO>().lambda().eq(LotteryTicketDO::getOrderId, order.getOrderId()));
            price = BeiDanUtil.award(lotteryOrderDOS, resultMatch, bonusMap);
            //修改lotteryTicket表中的状态
            for (LotteryTicketDO ticketDO : lotteryOrderDOS) {
                lotteryTicketMapper.updateById(ticketDO);
            }
            //等于0相当于没有中奖
            log.debug("=======>[北单胜负过关][待开奖] 订单 [{}] 中奖[{}] ", order.getOrderId(), price);
            if (price == 0) {
                log.debug("=======>[北单胜负过关][未中奖] 订单[{}] 未中奖  ", order.getOrderId());
                order.setState(LotteryOrderStateEnum.FAIL_TO_WIN.getKey());
            } else {
                //已经中奖
                //给订单分佣处理。
                documentaryCommissionHelper.processCommiss("北单胜负过关", order, price);
            }
            order.setUpdateTime(new Date());
            lotteryOrderMapper.updateById(order);
            log.debug("=======>[北单胜负过关]  订单 [{}] 中奖金额[{}] 完成 <<<<<<<<<< ", order.getOrderId(), price);
        } else {
            price = -1;
        }
        return BaseVO.builder().success(true).errorMsg("奖金[" + price + "]").build();
    }

    private void awardBeidanSfgg() {
        //查询北单已经下注的订单列表
        List<LotteryOrderDO> orderList = lotteryOrderMapper.selectList(new QueryWrapper<LotteryOrderDO>().lambda().eq(LotteryOrderDO::getState, LotteryOrderStateEnum.TO_BE_AWARDED.getKey()).eq(LotteryOrderDO::getType, LotteryOrderTypeEnum.SIGLE_SFGG.getKey()));
        log.debug("=======>[北单胜负过关][待开奖] 记录数: {} ", orderList.size());

        Map<Integer, BeiDanSFGGMatchDO> matchMap = new HashMap<>();
        for (LotteryOrderDO order : orderList) {
            //查询下注的列表
            List<RacingBallDO> racingBallList = racingBallMapper.selectBatchIds(Convert.toList(Integer.class, order.getTargetIds()));
            //用戶下注列表
            List<BeiDanMatchDTO> beiDanMatchList = new ArrayList<>();
            //每场比赛出奖比赛列表
            List<String> list = new ArrayList<>();
            //出奖赔率
            List<String> bonusOddsList = new ArrayList<>();
            Boolean flag = true;
            Map<String, String> resultMatch = new HashMap<>();
            Map<String, String> bonusMap = new HashMap<>();
            for (RacingBallDO racingBallDO : racingBallList) {
                //下注結果組成list
                beiDanMatchList.add(JSONUtil.toBean(racingBallDO.getContent(), BeiDanMatchDTO.class));
                //查询下注对应的比赛赛果
                BeiDanSFGGMatchDO beiDanMatch = null;
                if (matchMap.get(racingBallDO.getTargetId()) != null) {
                    beiDanMatch = matchMap.get(racingBallDO.getTargetId());
                } else {
                    beiDanMatch = beiDanSfggMatchMapper.selectById(racingBallDO.getTargetId());
                    matchMap.put(racingBallDO.getTargetId(), beiDanMatch);
                }
                //如果比赛和赔率还没有出结果直接跳出 由于赔率不是同时出的，所有也需要做如下判断处理
                if (StrUtil.isBlank(beiDanMatch.getAward()) || beiDanMatch.getBonusOdds().indexOf("-") != -1) {
                    log.debug("=======>[北单胜负过关][待开奖] 订单 [{}] 赛事[{}]未出赛果 或赔率 ", order.getOrderId(), beiDanMatch.getNumber());
                    flag = false;
                    break;
                }
                list.add(beiDanMatch.getAward());
                bonusOddsList.add(beiDanMatch.getBonusOdds());
                resultMatch.put(beiDanMatch.getNumber(), beiDanMatch.getAward());
                bonusMap.put(beiDanMatch.getNumber(), beiDanMatch.getBonusOdds());
            }
            if (flag) {
                //对schemeDetails兑奖
                if (StringUtils.isBlank(order.getSchemeDetails())) {
                    log.error("============胜负过关订单 [{}] 没有具体schemeDetail 不参与兑派奖==========", order.getOrderId());
                    continue;
                }
//                List<SportSchemeDetailsListVO> listVOList = JSONUtil.toList(order.getSchemeDetails(), SportSchemeDetailsListVO.class);
//                BeiDanUtil.awardSchemeDetails(listVOList, resultMatch, bonusMap);
//                //计算用户有没有中奖，中奖了把每一注的金额进行累加在返回
//                double price = listVOList.stream().filter(item -> item.isAward()).mapToDouble(item -> Double.valueOf(item.getMoney())).sum();
//                //反向保存一下数据
//                order.setSchemeDetails(JSON.toJSONString(listVOList));

                List<LotteryTicketDO> lotteryOrderDOS = lotteryTicketMapper.selectList(new QueryWrapper<LotteryTicketDO>().lambda().eq(LotteryTicketDO::getOrderId, order.getOrderId()));
                double price = BeiDanUtil.award(lotteryOrderDOS, resultMatch, bonusMap);
                //修改lotteryTicket表中的状态
                for (LotteryTicketDO ticketDO : lotteryOrderDOS) {
                    lotteryTicketMapper.updateById(ticketDO);
                }
                //等于0相当于没有中奖
                log.debug("=======>[北单胜负过关][待开奖] 订单 [{}] 中奖[{}] ", order.getOrderId(), price);
                if (price == 0) {
                    log.debug("=======>[北单胜负过关][未中奖] 订单[{}] 未中奖  ", order.getOrderId());
                    order.setState(LotteryOrderStateEnum.FAIL_TO_WIN.getKey());
                } else {
                    //已经中奖
                    //给订单分佣处理。
                    documentaryCommissionHelper.processCommiss("北单胜负过关", order, price);
                }
                order.setUpdateTime(new Date());
                lotteryOrderMapper.updateById(order);
                log.debug("=======>[北单胜负过关]  订单 [{}] 中奖金额[{}] 完成 <<<<<<<<<< ", order.getOrderId(), price);
            }
        }
    }

    private void awardBeidan() {
        //查询北单已经下注的订单列表
        List<LotteryOrderDO> orderList = lotteryOrderMapper.selectList(new QueryWrapper<LotteryOrderDO>().lambda().eq(LotteryOrderDO::getState, LotteryOrderStateEnum.TO_BE_AWARDED.getKey()).eq(LotteryOrderDO::getType, LotteryOrderTypeEnum.SINGLE.getKey()));
        log.debug("=======>[北单][待开奖] 记录数: {} ", orderList.size());

        Map<Integer, BeiDanMatchDO> matchMap = new HashMap<>();
        for (LotteryOrderDO order : orderList) {
            //查询下注的列表
            List<RacingBallDO> racingBallList = racingBallMapper.selectBatchIds(Convert.toList(Integer.class, order.getTargetIds()));
            //用戶下注列表
            List<BeiDanMatchDTO> beiDanMatchList = new ArrayList<>();
            //每场比赛出奖比赛列表
            List<String> list = new ArrayList<>();
            //出奖赔率
            List<String> bonusOddsList = new ArrayList<>();
            Boolean flag = true;
            Map<String, String> resultMatch = new HashMap<>();
            Map<String, String> bonusMap = new HashMap<>();
            for (RacingBallDO racingBallDO : racingBallList) {
                //下注結果組成list
                beiDanMatchList.add(JSONUtil.toBean(racingBallDO.getContent(), BeiDanMatchDTO.class));
                //查询下注对应的比赛赛果
                BeiDanMatchDO beiDanMatch = null;
                if (matchMap.get(racingBallDO.getTargetId()) != null) {
                    beiDanMatch = matchMap.get(racingBallDO.getTargetId());
                } else {
                    beiDanMatch = beiDanMatchMapper.selectById(racingBallDO.getTargetId());
                    matchMap.put(racingBallDO.getTargetId(), beiDanMatch);
                }
                //如果比赛和赔率还没有出结果直接跳出 由于赔率不是同时出的，所有也需要做如下判断处理
                if (StrUtil.isBlank(beiDanMatch.getAward()) || beiDanMatch.getBonusOdds().indexOf("-") != -1) {
                    log.debug("=======>[北单][待开奖] 订单 [{}] 赛事[{}]未出赛果 或赔率 ", order.getOrderId(), beiDanMatch.getNumber());
                    flag = false;
                    break;
                }
                list.add(beiDanMatch.getAward());
                bonusOddsList.add(beiDanMatch.getBonusOdds());
                resultMatch.put(beiDanMatch.getNumber(), beiDanMatch.getAward());
                bonusMap.put(beiDanMatch.getNumber(), beiDanMatch.getBonusOdds());
            }
            if (flag) {
                //对schemeDetails兑奖
                if (StringUtils.isBlank(order.getSchemeDetails())) {
                    log.error("============订单 [{}] 没有具体schemeDetail 不参与兑派奖==========", order.getOrderId());
                    continue;
                }

                /***
                 //改为从lotteryTicket表进行兑奖
                 //                List<SportSchemeDetailsListVO> listVOList = JSONUtil.toList(order.getSchemeDetails(), SportSchemeDetailsListVO.class);
                 //                BeiDanUtil.awardSchemeDetails(listVOList, resultMatch, bonusMap);
                 //计算用户有没有中奖，中奖了把每一注的金额进行累加在返回
                 double price = 0d;//listVOList.stream().filter(item -> item.isAward()).mapToDouble(item -> Double.valueOf(item.getMoney())).sum();
                 //反向保存一下数据
                 //order.setSchemeDetails(JSON.toJSONString(listVOList));
                 **/
                List<LotteryTicketDO> lotteryOrderDOS = lotteryTicketMapper.selectList(new QueryWrapper<LotteryTicketDO>().lambda().eq(LotteryTicketDO::getOrderId, order.getOrderId()));
                double price = BeiDanUtil.award(lotteryOrderDOS, resultMatch, bonusMap);
                //修改lotteryTicket表中的状态
                for (LotteryTicketDO ticketDO : lotteryOrderDOS) {
                    lotteryTicketMapper.updateById(ticketDO);
                }
                //等于0相当于没有中奖
                log.debug("=======>[北单][待开奖] 订单 [{}] 中奖[{}] ", order.getOrderId(), price);
                if (price == 0) {
                    log.debug("=======>[北单][未中奖] 订单[{}] 未中奖  ", order.getOrderId());
                    order.setState(LotteryOrderStateEnum.FAIL_TO_WIN.getKey());
                } else {
                    //已经中奖
                    //给订单分佣处理。
                    documentaryCommissionHelper.processCommiss("北单", order, price);
                }
                order.setUpdateTime(new Date());
                lotteryOrderMapper.updateById(order);
                log.debug("=======>[北单]  订单 [{}] 中奖金额[{}] 完成 <<<<<<<<<< ", order.getOrderId(), price);
            }
        }
    }

    private void addRecord(LotteryOrderDO lotteryOrder, Integer tenantId) {
        PayOrderDO payOrder = new PayOrderDO();
        payOrder.setOrderId(OrderNumberGenerationUtil.getOrderId());
        String type = PayOrderTypeEnum.SINGLE_REFUND.getKey();
        payOrder.setType(type);
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
