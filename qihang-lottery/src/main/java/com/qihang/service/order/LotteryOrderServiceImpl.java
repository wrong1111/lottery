package com.qihang.service.order;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qihang.annotation.TenantIgnore;
import com.qihang.common.util.email.EmailUtils;
import com.qihang.common.util.order.OrderNumberGenerationUtil;
import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.controller.basketball.dto.BasketballMatchDTO;
import com.qihang.controller.beidan.dto.BeiDanMatchDTO;
import com.qihang.controller.football.dto.FootballMatchDTO;
import com.qihang.controller.order.admin.lottery.dto.*;
import com.qihang.controller.order.admin.lottery.vo.LotteryOrderQueryVO;
import com.qihang.controller.order.admin.lottery.vo.RacingBallVO;
import com.qihang.controller.order.app.lottery.dto.LotteryOrderDTO;
import com.qihang.controller.order.app.lottery.vo.BallInfoVO;
import com.qihang.controller.order.app.lottery.vo.LotteryOrderVO;
import com.qihang.controller.order.app.lottery.vo.YesterdayCentreOrderVO;
import com.qihang.controller.permutation.app.vo.PermutationRecordVO;
import com.qihang.controller.winburden.dto.WinBurdenMatchDTO;
import com.qihang.domain.ballgame.BallGameDO;
import com.qihang.domain.basketball.BasketballMatchDO;
import com.qihang.domain.beidan.BeiDanMatchDO;
import com.qihang.domain.documentary.DocumentaryDO;
import com.qihang.domain.documentary.DocumentaryUserDO;
import com.qihang.domain.football.FootballMatchDO;
import com.qihang.domain.log.LogDO;
import com.qihang.domain.order.LotteryOrderDO;
import com.qihang.domain.order.PayOrderDO;
import com.qihang.domain.permutation.PermutationDO;
import com.qihang.domain.racingball.RacingBallDO;
import com.qihang.domain.shop.ShopDO;
import com.qihang.domain.user.SysUserDO;
import com.qihang.domain.user.UserDO;
import com.qihang.domain.winburden.WinBurdenMatchDO;
import com.qihang.domain.withdrawal.WithdrawalDO;
import com.qihang.enumeration.error.ErrorCodeEnum;
import com.qihang.enumeration.order.lottery.LotteryOrderStateEnum;
import com.qihang.enumeration.order.lottery.LotteryOrderTypeEnum;
import com.qihang.enumeration.order.pay.PayOrderStateEnum;
import com.qihang.enumeration.order.pay.PayOrderTypeEnum;
import com.qihang.enumeration.order.pay.PayTypeEnum;
import com.qihang.mapper.ballgame.BallGameMapper;
import com.qihang.mapper.basketball.BasketballMatchMapper;
import com.qihang.mapper.beidan.BeiDanMatchMapper;
import com.qihang.mapper.documentary.DocumentaryMapper;
import com.qihang.mapper.documentary.DocumentaryUserMapper;
import com.qihang.mapper.football.FootballMatchMapper;
import com.qihang.mapper.log.LogMapper;
import com.qihang.mapper.order.LotteryOrderMapper;
import com.qihang.mapper.order.PayOrderMapper;
import com.qihang.mapper.permutation.PermutationMapper;
import com.qihang.mapper.racingball.RacingBallMapper;
import com.qihang.mapper.user.SysUserMapper;
import com.qihang.mapper.user.UserMapper;
import com.qihang.mapper.winburden.WinBurdenMatchMapper;
import com.qihang.mapper.withdrawal.WithdrawalMapper;
import com.qihang.service.shop.IShopService;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bright
 * @since 2022-10-10
 */
@Service
public class LotteryOrderServiceImpl extends ServiceImpl<LotteryOrderMapper, LotteryOrderDO> implements ILotteryOrderService {

    @Resource
    private LotteryOrderMapper lotteryOrderMapper;

    @Resource
    private BallGameMapper ballGameMapper;

    @Resource
    private PermutationMapper permutationMapper;

    @Resource
    private RacingBallMapper racingBallMapper;

    @Resource
    private FootballMatchMapper footballMatchMapper;

    @Resource
    private BasketballMatchMapper basketballMatchMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private PayOrderMapper payOrderMapper;

    @Resource
    private BeiDanMatchMapper beiDanMatchMapper;

    @Resource
    private DocumentaryMapper documentaryMapper;

    @Resource
    private DocumentaryUserMapper documentaryUserMapper;

    @Resource
    private IShopService shopService;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private EmailUtils emailUtils;

    @Resource
    private WinBurdenMatchMapper winBurdenMatchMapper;

    @Resource
    private LogMapper logMapper;

    @Resource
    private WithdrawalMapper withdrawalMapper;

    @Override
    public CommonListVO<LotteryOrderVO> getLotteryOrderPage(LotteryOrderDTO lotteryOrder, Integer userId) {
        CommonListVO<LotteryOrderVO> commonList = new CommonListVO<>();
        //分页
        Page<LotteryOrderDO> page = new Page<>(lotteryOrder.getPageNo(), lotteryOrder.getPageSize());

        LambdaQueryWrapper<LotteryOrderDO> qw = new QueryWrapper<LotteryOrderDO>().lambda();

        if (ObjectUtil.isNotNull(lotteryOrder.getUserId())) {
            userId = lotteryOrder.getUserId();
        }
        //动态拼接查询条件
        qw.eq(StrUtil.isNotBlank(lotteryOrder.getState()), LotteryOrderDO::getState, lotteryOrder.getState());
        qw.eq(LotteryOrderDO::getUserId, userId);
        qw.eq(LotteryOrderDO::getUserId, userId);
        qw.ge(ObjectUtil.isNotNull(lotteryOrder.getStartTime()), LotteryOrderDO::getCreateTime, lotteryOrder.getStartTime());
        qw.le(ObjectUtil.isNotNull(lotteryOrder.getStartTime()), LotteryOrderDO::getCreateTime, lotteryOrder.getEndTime());
        qw.orderByDesc(LotteryOrderDO::getCreateTime);
        Page<LotteryOrderDO> lotteryOrderPage = lotteryOrderMapper.selectPage(page, qw);
        //转换vo
        List<LotteryOrderVO> orderList = BeanUtil.copyToList(lotteryOrderPage.getRecords(), LotteryOrderVO.class);
        for (LotteryOrderVO lotteryOrderVO : orderList) {
            //设置订单对应的彩票名字和logo
            LotteryOrderTypeEnum lotteryEnum = LotteryOrderTypeEnum.valueOFS(lotteryOrderVO.getType());
            BallGameDO ballGame = ballGameMapper.selectOne(new QueryWrapper<BallGameDO>().lambda().eq(BallGameDO::getName, lotteryEnum.getValue()));
            lotteryOrderVO.setBallName(ballGame.getName());
            lotteryOrderVO.setBallUrl(ballGame.getUrl());
        }
        commonList.setVoList(orderList);
        commonList.setTotal(lotteryOrderPage.getTotal());
        return commonList;
    }

    /*
     * 查询订单记录
     * */
    @Override
    @TenantIgnore
    public LotteryOrderVO getLotteryOrderById(Integer id, Integer userId) {
        LotteryOrderDO lotteryOrder = lotteryOrderMapper.selectById(id);
        //转换vo
        LotteryOrderVO lotteryOrderVO = BeanUtil.copyProperties(lotteryOrder, LotteryOrderVO.class);
        List<DocumentaryUserDO> documentaryUserList = documentaryUserMapper.selectList(new QueryWrapper<DocumentaryUserDO>().lambda().eq(DocumentaryUserDO::getLotteryOrderId, lotteryOrder.getId()).eq(DocumentaryUserDO::getUserId, userId));
        if (CollUtil.isNotEmpty(documentaryUserList)) {
            //根据订单id查询发单详情
            DocumentaryDO documentary = documentaryMapper.selectOne(new QueryWrapper<DocumentaryDO>().lambda().eq(DocumentaryDO::getId, documentaryUserList.get(0).getDocumentaryId()));
            if (ObjectUtil.isNull(documentary)) {
                lotteryOrderVO.setDocumentaryFlag(false);
            } else {
                //如果是跟单订单
                lotteryOrderVO.setDocumentaryFlag(true);
                lotteryOrderVO.setOpenFlag(documentary.getState().equals("0") ? true : false);
                if (documentary.getState().equals("1")) {
                    //查询比赛是否已经截止下注
                    List<Integer> ids = new ArrayList<>();
                    List<RacingBallDO> ballList = racingBallMapper.selectBatchIds(Convert.toList(Integer.class, lotteryOrder.getTargetIds()));
                    for (RacingBallDO racingBallDO : ballList) {
                        ids.add(racingBallDO.getTargetId());
                    }
                    if (lotteryOrder.getType().equals(LotteryOrderTypeEnum.FOOTBALL.getKey())) {
                        List<FootballMatchDO> footballMatchList = footballMatchMapper.selectBatchIds(ids);
                        //按截止时间升序
                        footballMatchList = footballMatchList.stream().sorted(Comparator.comparing(FootballMatchDO::getDeadline)).collect(Collectors.toList());
                        lotteryOrderVO.setIsEnd(footballMatchList.get(0).getState().equals("0") ? true : false);
                        lotteryOrderVO.setDeadline(footballMatchList.get(0).getDeadline());
                    } else if (lotteryOrder.getType().equals(LotteryOrderTypeEnum.BASKETBALL.getKey())) {
                        List<BasketballMatchDO> basketballMatchList = basketballMatchMapper.selectBatchIds(ids);
                        //按截止时间升序
                        basketballMatchList = basketballMatchList.stream().sorted(Comparator.comparing(BasketballMatchDO::getDeadline)).collect(Collectors.toList());
                        lotteryOrderVO.setIsEnd(basketballMatchList.get(0).getState().equals("0") ? true : false);
                        lotteryOrderVO.setDeadline(basketballMatchList.get(0).getDeadline());
                    }
                }
            }
        }
        //设置订单对应的彩票名字和logo
        LotteryOrderTypeEnum lotteryEnum = LotteryOrderTypeEnum.valueOFS(lotteryOrderVO.getType());
        BallGameDO ballGame = ballGameMapper.selectList(new QueryWrapper<BallGameDO>().lambda().eq(BallGameDO::getName, lotteryEnum.getValue())).get(0);
        lotteryOrderVO.setBallName(ballGame.getName());
        lotteryOrderVO.setBallUrl(ballGame.getUrl());
        //下注的所有id转换成list
        List<Integer> ids = Convert.toList(Integer.class, lotteryOrder.getTargetIds());

        //wyong edit 福彩3D
        if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.ARRAY.getKey())
                || StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.ARRANGE.getKey())
                || StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.SEVEN_STAR.getKey())
                || StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.GRAND_LOTTO.getKey())
                || StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.FC3D.getKey())
                || StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.FCSSQ.getKey())
                || StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.FCKL8.getKey())
                || StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.FCQLC.getKey())
        ) {
            //批量查询下注列表
            List<PermutationDO> permutationList = permutationMapper.selectBatchIds(ids);
            //竞彩列表 由于都是一样的直接取第一个就行
            lotteryOrderVO.setNotes(permutationList.stream().mapToInt(PermutationDO::getNotes).sum());
            lotteryOrderVO.setTimes(permutationList.get(0).getTimes());
            List<PermutationRecordVO> list = BeanUtil.copyToList(permutationList, PermutationRecordVO.class);
            lotteryOrderVO.setRecordList(list);
        } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.FOOTBALL.getKey()) || StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.BASKETBALL.getKey()) || StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.SINGLE.getKey()) || StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.VICTORY_DEFEAT.getKey()) || StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.REN_JIU.getKey())) {
            //竞球数据
            List<RacingBallDO> racingBallList = racingBallMapper.selectBatchIds(ids);
            //因为都是一样的取一个值就可以
            lotteryOrderVO.setNotes(racingBallList.get(0).getNotes());
            lotteryOrderVO.setTimes(racingBallList.get(0).getTimes());
            lotteryOrderVO.setPssTypeList(Convert.toList(Integer.class, racingBallList.get(0).getType()));
            List<BallInfoVO> ballInfoList = new ArrayList<>();
            //比赛数据
            for (RacingBallDO racingBallDO : racingBallList) {
                BallInfoVO ballInfo = new BallInfoVO();
                //根据类型组合不同的数据
                if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.FOOTBALL.getKey())) {
                    FootballMatchDTO footballMatch = JSONUtil.toBean(racingBallDO.getContent(), FootballMatchDTO.class);
                    //根据id查询足球数据
                    FootballMatchDO footballMatchDO = footballMatchMapper.selectById(footballMatch.getId());
                    ballInfo.setHomeTeam(footballMatch.getHomeTeam());
                    ballInfo.setVisitingTeam(footballMatch.getVisitingTeam());
                    ballInfo.setNumber(footballMatch.getNumber());
                    ballInfo.setAward(footballMatchDO.getAward());
                    //投注内容
                    ballInfo.setContent(racingBallDO.getContent());
                    //赛果
                    ballInfo.setHalfFullCourt(footballMatchDO.getHalfFullCourt());
                    ballInfo.setLetBall(footballMatch.getLetBall());
                    ballInfoList.add(ballInfo);
                } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.BASKETBALL.getKey())) {
                    BasketballMatchDTO basketballMatch = JSONUtil.toBean(racingBallDO.getContent(), BasketballMatchDTO.class);
                    //根据id查询足球数据
                    BasketballMatchDO basketballMatchDO = basketballMatchMapper.selectById(basketballMatch.getId());
                    ballInfo.setHomeTeam(basketballMatch.getHomeTeam());
                    ballInfo.setVisitingTeam(basketballMatch.getVisitingTeam());
                    ballInfo.setNumber(basketballMatch.getNumber());
                    ballInfo.setAward(basketballMatchDO.getAward());
                    //投注内容
                    ballInfo.setContent(racingBallDO.getContent());
                    //赛果
                    ballInfo.setHalfFullCourt(basketballMatchDO.getHalfFullCourt());
                    ballInfo.setLetBall(basketballMatch.getCedePoints());
                    ballInfoList.add(ballInfo);
                } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.SINGLE.getKey())) {
                    BeiDanMatchDTO beiDanMatch = JSONUtil.toBean(racingBallDO.getContent(), BeiDanMatchDTO.class);
                    //根据id查询足球数据
                    BeiDanMatchDO beiDanMatchDO = beiDanMatchMapper.selectById(beiDanMatch.getId());
                    ballInfo.setHomeTeam(beiDanMatchDO.getHomeTeam());
                    ballInfo.setVisitingTeam(beiDanMatchDO.getVisitingTeam());
                    ballInfo.setNumber(beiDanMatchDO.getNumber());
                    ballInfo.setAward(beiDanMatchDO.getAward());
                    //投注内容
                    ballInfo.setContent(racingBallDO.getContent());
                    //赛果
                    ballInfo.setHalfFullCourt(beiDanMatchDO.getHalfFullCourt());
                    ballInfo.setLetBall(beiDanMatchDO.getLetBall());
                    ballInfo.setBonusOdds(beiDanMatchDO.getBonusOdds());
                    ballInfoList.add(ballInfo);
                } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.VICTORY_DEFEAT.getKey()) || StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.REN_JIU.getKey())) {
                    WinBurdenMatchDTO winBurdenMatch = JSONUtil.toBean(racingBallDO.getContent(), WinBurdenMatchDTO.class);
                    //根据id查询足球数据
                    WinBurdenMatchDO winBurdenMatchDO = winBurdenMatchMapper.selectById(winBurdenMatch.getId());
                    ballInfo.setHomeTeam(winBurdenMatchDO.getHomeTeam());
                    ballInfo.setVisitingTeam(winBurdenMatchDO.getVisitingTeam());
                    ballInfo.setNumber(winBurdenMatchDO.getNumber());
                    ballInfo.setAward(winBurdenMatchDO.getAward());
                    //投注内容
                    ballInfo.setContent(racingBallDO.getContent());
                    ballInfoList.add(ballInfo);
                }
            }
            lotteryOrderVO.setBallInfoList(ballInfoList);
        }
        return lotteryOrderVO;
    }

    final String LINE_SPILTER = " | ";

    /*
     后台 订单查询
     */
    @Override
    public CommonListVO<LotteryOrderQueryVO> getAdminLotteryOrderPage(LotteryOrderQueryDTO lotteryOrderQuery) {
        CommonListVO<LotteryOrderQueryVO> commonList = new CommonListVO<>();
        commonList.setTotal(0L);
        commonList.setVoList(new ArrayList<>());
        Integer userId = null;
        UserDO user = userMapper.selectOne(new QueryWrapper<UserDO>().lambda().eq(UserDO::getPhone, lotteryOrderQuery.getPhone()));
        if (ObjectUtil.isNotNull(user)) {
            userId = user.getId();
        } else {
            if (StrUtil.isNotBlank(lotteryOrderQuery.getPhone())) {
                return commonList;
            }
        }
        //分页
        Page<LotteryOrderDO> page = new Page<>(lotteryOrderQuery.getPageNo(), lotteryOrderQuery.getPageSize());
        LambdaQueryWrapper<LotteryOrderDO> qw = new QueryWrapper<LotteryOrderDO>().lambda();
        //动态拼接查询条件
        qw.eq(ObjectUtil.isNotNull(userId), LotteryOrderDO::getUserId, userId);
        qw.eq(StrUtil.isNotBlank(lotteryOrderQuery.getOrderId()), LotteryOrderDO::getOrderId, lotteryOrderQuery.getOrderId());
        qw.eq(StrUtil.isNotBlank(lotteryOrderQuery.getState()), LotteryOrderDO::getState, lotteryOrderQuery.getState());
        qw.eq(StrUtil.isNotBlank(lotteryOrderQuery.getType()), LotteryOrderDO::getType, lotteryOrderQuery.getType());
        qw.orderByDesc(LotteryOrderDO::getCreateTime);
        Page<LotteryOrderDO> lotteryOrderPage = lotteryOrderMapper.selectPage(page, qw);
        commonList.setTotal(lotteryOrderPage.getTotal());
        List<LotteryOrderQueryVO> lotteryOrderQueryList = new ArrayList<>();
        for (LotteryOrderDO lotteryOrder : lotteryOrderPage.getRecords()) {
            LotteryOrderQueryVO lotteryOrderQueryVO = new LotteryOrderQueryVO();
            BeanUtils.copyProperties(lotteryOrder, lotteryOrderQueryVO);
            UserDO userDO = userMapper.selectById(lotteryOrder.getUserId());
            lotteryOrderQueryVO.setNickname(userDO.getNickname());
            //上级
            UserDO parentUser = userMapper.selectById(userDO.getPid());
            if (ObjectUtil.isNotNull(parentUser)) {
                lotteryOrderQueryVO.setParentName(parentUser.getNickname());
            }
            if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.ARRAY.getKey())
                    || StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.ARRANGE.getKey())
                    || StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.SEVEN_STAR.getKey())
                    || StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.GRAND_LOTTO.getKey())
                    || StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.FC3D.getKey())
                    || StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.FCKL8.getKey())
                    || StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.FCQLC.getKey())
                    || StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.FCSSQ.getKey())
            ) {
                List<RacingBallVO> ballList = new ArrayList<>();
                //排列3组装数据
                List<PermutationDO> threeList = permutationMapper.selectBatchIds(Convert.toList(Integer.class, lotteryOrder.getTargetIds()));
                for (PermutationDO permutation : threeList) {
                    RacingBallVO racingBall = new RacingBallVO();
                    racingBall.setNotes(permutation.getNotes());
                    racingBall.setTimes(permutation.getTimes());
                    racingBall.setType(permutation.getMode());
                    racingBall.setNo(permutation.getStageNumber().toString());
                    String str = "";
                    //排列3，排列5，七星彩，福彩3D，对排位敏感的。
                    if (permutation.getType().equals(LotteryOrderTypeEnum.ARRAY.getKey())) {
                        if (StrUtil.isNotBlank(permutation.getHundred())) {
                            str += permutation.getHundred() + LINE_SPILTER;
                        }
                        if (StrUtil.isNotBlank(permutation.getTen())) {
                            str += permutation.getTen() + LINE_SPILTER;
                        }
                        if (StrUtil.isNotBlank(permutation.getIndividual())) {
                            str += permutation.getIndividual();
                        }
                        if (permutation.getMode().equals("1") || permutation.getMode().equals("2")) {
                            List<BallState> ballStateList = JSONUtil.toList(permutation.getIndividual(), BallState.class);
                            str = getBallAll(ballStateList);
                        } else if (permutation.getMode().equals("3")) {
                            str = permutation.getIndividual();
                        }
                    } else if (permutation.getType().equals(LotteryOrderTypeEnum.ARRANGE.getKey())
                            || permutation.getType().equals(LotteryOrderTypeEnum.SEVEN_STAR.getKey())
                            || permutation.getType().equals(LotteryOrderTypeEnum.FC3D.getKey())
                    ) {

                        if (StrUtil.isNotBlank(permutation.getHundredMyriad())) {
                            str += permutation.getHundredMyriad() + LINE_SPILTER;
                        }
                        if (StrUtil.isNotBlank(permutation.getTenMyriad())) {
                            str += permutation.getTenMyriad() + LINE_SPILTER;
                        }
                        if (StrUtil.isNotBlank(permutation.getMyriad())) {
                            str += permutation.getMyriad() + LINE_SPILTER;
                        }
                        if (StrUtil.isNotBlank(permutation.getKilo())) {
                            str += permutation.getKilo() + LINE_SPILTER;
                        }
                        if (StrUtil.isNotBlank(permutation.getHundred())) {
                            str += permutation.getHundred() + LINE_SPILTER;
                        }
                        if (StrUtil.isNotBlank(permutation.getTen())) {
                            str += permutation.getTen() + LINE_SPILTER;
                        }
                        if (StrUtil.isNotBlank(permutation.getIndividual())) {
                            str += permutation.getIndividual();
                        }
                        //七星彩 MODE=0
                        //福彩3D 组三
                        if (permutation.getType().equals(LotteryOrderTypeEnum.FC3D.getKey()) && "1".equals(permutation.getMode())) {
                            List<BallState> tenList = JSONUtil.toList(permutation.getIndividual(), BallState.class);
                            str = getBallAll(tenList);
                        } else if (permutation.getType().equals(LotteryOrderTypeEnum.FC3D.getKey()) && "2".equals(permutation.getMode())) {
                            List<BallState> tenList = JSONUtil.toList(permutation.getIndividual(), BallState.class);
                            str = getBallAll(tenList);
                        }
                    } else if (permutation.getType().equals(LotteryOrderTypeEnum.GRAND_LOTTO.getKey())
                            || permutation.getType().equals(LotteryOrderTypeEnum.FCSSQ.getKey())) {
                        //大乐透 8 ，双色球 24 分前后区，并且有胆拖存在
                        str = "";
                        if (StringUtils.isNotBlank(permutation.getTen())) {
                            List<BallState> tenList = JSONUtil.toList(permutation.getTen(), BallState.class);
                            str = getBallAll(tenList);
                        }
                        if (StringUtils.isNotBlank(permutation.getIndividual())) {
                            str += LINE_SPILTER;
                            List<BallState> ballStateList = JSONUtil.toList(permutation.getIndividual(), BallState.class);
                            String ball = getBallAll(ballStateList);
                            if (StringUtils.isNotBlank(ball)) {
                                str += ball;
                            }
                        }

                        //七乐彩
                    } else if (permutation.getType().equals(LotteryOrderTypeEnum.FCQLC.getKey())) {
                        List<BallState> ballStateList = JSONUtil.toList(permutation.getIndividual(), BallState.class);
                        str = getBallAll(ballStateList);
                    } else if (permutation.getType().equals(LotteryOrderTypeEnum.FCKL8.getKey())) {
                        List<BallState> ballStateList = JSONUtil.toList(permutation.getIndividual(), BallState.class);
                        str = getBallAll(ballStateList);
                    }
                    racingBall.setContent(str);
                    racingBall.setReward(permutation.getReward());
                    ballList.add(racingBall);
                }
                lotteryOrderQueryVO.setRacingBallList(ballList);
            } else {
                List<RacingBallDO> ballList = racingBallMapper.selectBatchIds(Convert.toList(Integer.class, lotteryOrder.getTargetIds()));
                List<RacingBallVO> racingBallVOList = new ArrayList<>();
                for (RacingBallDO racingBallDO : ballList) {
                    RacingBallVO racingBall = new RacingBallVO();
                    BeanUtils.copyProperties(racingBallDO, racingBall);
                    //根据订单类型查询对应的比赛赛果
                    if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.FOOTBALL.getKey())) {
                        FootballMatchDO footballMatchDO = footballMatchMapper.selectById(racingBallDO.getTargetId());
                        if (ObjectUtil.isNotNull(footballMatchDO)) {
                            racingBall.setReward(footballMatchDO.getHalfFullCourt());
                        }
                    } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.BASKETBALL.getKey())) {
                        BasketballMatchDO basketballMatchDO = basketballMatchMapper.selectById(racingBallDO.getTargetId());
                        if (ObjectUtil.isNotNull(basketballMatchDO)) {
                            racingBall.setReward(basketballMatchDO.getHalfFullCourt());
                        }
                    } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.SINGLE.getKey())) {
                        BeiDanMatchDO beiDanMatchDO = beiDanMatchMapper.selectById(racingBallDO.getTargetId());
                        if (ObjectUtil.isNotNull(beiDanMatchDO)) {
                            racingBall.setReward(beiDanMatchDO.getHalfFullCourt());
                        }
                    } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.VICTORY_DEFEAT.getKey()) || StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.REN_JIU.getKey())) {
                        WinBurdenMatchDO winBurdenMatchDO = winBurdenMatchMapper.selectById(racingBallDO.getTargetId());
                        if (ObjectUtil.isNotNull(winBurdenMatchDO)) {
                            racingBall.setReward(winBurdenMatchDO.getAward());
                        }
                    }
                    racingBallVOList.add(racingBall);
                }
                lotteryOrderQueryVO.setRacingBallList(racingBallVOList);
            }
            lotteryOrderQueryList.add(lotteryOrderQueryVO);
        }
        commonList.setVoList(lotteryOrderQueryList);
        return commonList;
    }


    public String getBallAll(List<BallState> states) {
        return states.stream()
                .map((item) -> item.isGallbladder ? item.num + "[胆]" : item.num)
                .collect(Collectors.joining(","));
    }

    @Data
    private class BallState {
        String num;
        Boolean isGallbladder;
        Boolean active;

        public boolean isGallbladder() {
            return isGallbladder;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseVO ticketing(TicketingDTO ticketing) {
        //如果没有id就是一键出票
        if (ObjectUtil.isNull(ticketing.getId())) {
            List<LotteryOrderDO> orderList = lotteryOrderMapper.selectList(new QueryWrapper<LotteryOrderDO>().lambda().eq(LotteryOrderDO::getState, LotteryOrderStateEnum.TO_BE_ISSUED.getKey()));
            if (CollUtil.isEmpty(orderList)) {
                return new BaseVO(false, ErrorCodeEnum.E076.getKey(), ErrorCodeEnum.E076.getValue());
            }
            for (LotteryOrderDO lotteryOrderDO : orderList) {
                //根据租户id查询店铺余额
                ShopDO shop = shopService.getShopById(lotteryOrderDO.getTenantId());
                //需要的手续费
                BigDecimal procedures = lotteryOrderDO.getPrice().multiply(new BigDecimal(0.007));
                procedures = procedures.setScale(2, RoundingMode.HALF_UP);
                if (shop.getBalance().compareTo(procedures) == -1) {
                    return new BaseVO(false, ErrorCodeEnum.E081.getKey(), ErrorCodeEnum.E081.getValue());
                }
                //扣除店铺余额
                shop.setBalance(shop.getBalance().subtract(procedures));
                shopService.updateById(shop);

                lotteryOrderDO.setState(LotteryOrderStateEnum.TO_BE_AWARDED.getKey());
                lotteryOrderDO.setTicketingTime(new Date());
                //根据未出票的条件一键改为出票
                lotteryOrderMapper.updateById(lotteryOrderDO);
            }
        } else {
            //根据订单id查询租户id
            LotteryOrderDO lotteryOrderDO = lotteryOrderMapper.selectById(ticketing.getId());
            //拒绝处理
            if (ticketing.getState().equals(LotteryOrderStateEnum.REFUSE.getKey())) {
                lotteryOrderDO.setState(ticketing.getState());
                lotteryOrderMapper.updateById(lotteryOrderDO);
                //如果拒绝了需要退还用户金额
                UserDO userDO = userMapper.selectById(lotteryOrderDO.getUserId());
                userDO.setGold(userDO.getGold().add(lotteryOrderDO.getPrice()));
                userMapper.updateById(userDO);
                //添加钱包记录
                addPayRecord(lotteryOrderDO);
                return new BaseVO();
            }
            //出票处理
            //根据租户id查询店铺余额
            ShopDO shop = shopService.getShopById(lotteryOrderDO.getTenantId());
            //需要的手续费
            BigDecimal procedures = lotteryOrderDO.getPrice().multiply(new BigDecimal(0.007));
            procedures = procedures.setScale(2, RoundingMode.HALF_UP);
            if (shop.getBalance().compareTo(procedures) == -1) {
                return new BaseVO(false, ErrorCodeEnum.E081.getKey(), ErrorCodeEnum.E081.getValue());
            }
            //扣除店铺余额
            shop.setBalance(shop.getBalance().subtract(procedures));
            shopService.updateById(shop);
            //修改出票状态
            lotteryOrderDO.setState(ticketing.getState());
            lotteryOrderDO.setTicketingTime(new Date());
            lotteryOrderMapper.updateById(lotteryOrderDO);
        }
        return new BaseVO();
    }

    @Override
    public BaseVO award(AwardDTO award) {
        //如果没有id就是一键派奖
        if (ObjectUtil.isNotNull(award.getId())) {
            LotteryOrderDO lotteryOrder = lotteryOrderMapper.selectById(award.getId());
            if (!StrUtil.equals(lotteryOrder.getState(), LotteryOrderStateEnum.WAITING_AWARD.getKey())) {
                return new BaseVO(false, ErrorCodeEnum.E075.getKey(), ErrorCodeEnum.E075.getValue());
            }
            //根据租户id查询店铺余额
            ShopDO shop = shopService.getShopById(lotteryOrder.getTenantId());
            //需要的手续费
            BigDecimal procedures = lotteryOrder.getPrice().multiply(new BigDecimal(0.007));
            procedures = procedures.setScale(2, RoundingMode.HALF_UP);
            if (shop.getBalance().compareTo(procedures) == -1) {
                return new BaseVO(false, ErrorCodeEnum.E081.getKey(), ErrorCodeEnum.E081.getValue());
            }
            //扣除店铺余额
            shop.setBalance(shop.getBalance().subtract(procedures));
            shopService.updateById(shop);

            //加金额
            UserDO user = userMapper.selectById(lotteryOrder.getUserId());
            user.setPrice(user.getPrice().add(lotteryOrder.getWinPrice()));
            userMapper.updateById(user);
            //修改订单为已派奖
            lotteryOrder.setState(LotteryOrderStateEnum.ALREADY_AWARD.getKey());
            lotteryOrderMapper.updateById(lotteryOrder);
            //添加钱包记录
            addRecord(lotteryOrder);
        } else {
            //一键派奖
            List<LotteryOrderDO> orderList = lotteryOrderMapper.selectList(new QueryWrapper<LotteryOrderDO>().lambda().eq(LotteryOrderDO::getState, LotteryOrderStateEnum.WAITING_AWARD.getKey()));
            if (CollUtil.isEmpty(orderList)) {
                return new BaseVO(false, ErrorCodeEnum.E077.getKey(), ErrorCodeEnum.E077.getValue());
            }
            for (LotteryOrderDO lotteryOrder : orderList) {
                //根据租户id查询店铺余额
                ShopDO shop = shopService.getShopById(lotteryOrder.getTenantId());
                //需要的手续费
                BigDecimal procedures = lotteryOrder.getPrice().multiply(new BigDecimal(0.007));
                procedures = procedures.setScale(2, RoundingMode.HALF_UP);
                if (shop.getBalance().compareTo(procedures) == -1) {
                    return new BaseVO(false, ErrorCodeEnum.E081.getKey(), ErrorCodeEnum.E081.getValue());
                }
                //扣除店铺余额
                shop.setBalance(shop.getBalance().subtract(procedures));
                shopService.updateById(shop);

                UserDO user = userMapper.selectById(lotteryOrder.getUserId());
                //加金额
                user.setPrice(user.getPrice().add(lotteryOrder.getWinPrice()));
                userMapper.updateById(user);
                //修改订单为已派奖
                lotteryOrder.setState(LotteryOrderStateEnum.ALREADY_AWARD.getKey());
                lotteryOrderMapper.updateById(lotteryOrder);
                //添加钱包记录
                addRecord(lotteryOrder);
            }
        }
        return new BaseVO();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseVO retreat(Integer id) {
        LotteryOrderDO lotteryOrder = lotteryOrderMapper.selectById(id);
        if (StrUtil.equals(lotteryOrder.getState(), LotteryOrderStateEnum.REFUND.getKey())) {
            return new BaseVO(false, ErrorCodeEnum.E078.getKey(), ErrorCodeEnum.E078.getValue());
        }
        if (!StrUtil.equals(lotteryOrder.getState(), LotteryOrderStateEnum.TO_BE_ISSUED.getKey()) && !StrUtil.equals(lotteryOrder.getState(), LotteryOrderStateEnum.TO_BE_AWARDED.getKey())) {
            return new BaseVO(false, ErrorCodeEnum.E083.getKey(), ErrorCodeEnum.E083.getValue());
        }
        UserDO userDO = userMapper.selectById(lotteryOrder.getUserId());
        userDO.setGold(userDO.getGold().add(lotteryOrder.getPrice()));
        userMapper.updateById(userDO);
        //修改状态
        lotteryOrder.setState(LotteryOrderStateEnum.REFUND.getKey());
        lotteryOrderMapper.updateById(lotteryOrder);
        //添加钱包记录
        addPayRecord(lotteryOrder);
        return new BaseVO();
    }

    @Override
    public BaseVO actualVote(ActualVoteDTO actualVoteDTO) {
        LotteryOrderDO lotteryOrder = new LotteryOrderDO();
        BeanUtils.copyProperties(actualVoteDTO, lotteryOrder);
        lotteryOrderMapper.updateById(lotteryOrder);
        return new BaseVO();
    }

    @Override
    public CommonListVO<YesterdayCentreOrderVO> centre() {
        CommonListVO<YesterdayCentreOrderVO> commonList = new CommonListVO<>();
        Date start = DateUtil.parse(DateUtil.today() + " 00:00:00");
        Date end = DateUtil.parse(DateUtil.today() + " 23:59:59");
        //查询昨日的中奖订单
        List<LotteryOrderDO> orderList = lotteryOrderMapper.selectList(new QueryWrapper<LotteryOrderDO>().lambda()
//                .ge(LotteryOrderDO::getUpdateTime, start)
//                .le(LotteryOrderDO::getUpdateTime, end)
                .and(lq -> lq.eq(LotteryOrderDO::getState, LotteryOrderStateEnum.WAITING_AWARD.getKey())
                        .or()
                        .eq(LotteryOrderDO::getState, LotteryOrderStateEnum.ALREADY_AWARD.getKey())));
        List<YesterdayCentreOrderVO> list = new ArrayList<>();
        for (LotteryOrderDO lotteryOrder : orderList) {
            LotteryOrderTypeEnum lotteryOrderTypeEnum = LotteryOrderTypeEnum.valueOFS(lotteryOrder.getType());
            UserDO user = userMapper.selectById(lotteryOrder.getUserId());
            YesterdayCentreOrderVO yesterdayCentreOrder = new YesterdayCentreOrderVO();
            String msg = "[" + lotteryOrderTypeEnum.getValue() + "]" + user.getPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2") + "喜中" + lotteryOrder.getWinPrice() + "元";
            yesterdayCentreOrder.setMsg(msg);
            list.add(yesterdayCentreOrder);
        }
        //随机排列数据
        Collections.shuffle(list);
        commonList.setVoList(list);
        return commonList;
    }

    @Override
    @TenantIgnore
    public void NoTicketIssuedSedEmail() {
        List<SysUserDO> sysUserList = sysUserMapper.selectList(new QueryWrapper<SysUserDO>().lambda().isNotNull(SysUserDO::getEmail));
        if (CollUtil.isNotEmpty(sysUserList)) {
            for (SysUserDO sysUserDO : sysUserList) {
                Long count = lotteryOrderMapper.selectCount(new QueryWrapper<LotteryOrderDO>().lambda().eq(LotteryOrderDO::getState, LotteryOrderStateEnum.TO_BE_ISSUED.getKey()).eq(LotteryOrderDO::getTenantId, sysUserDO.getTenantId()));
                ShopDO shop = shopService.getShopById(sysUserDO.getTenantId());
                if (count >= 1 && StrUtil.isNotBlank(sysUserDO.getEmail())) {
                    emailUtils.NoTicketIssuedSedEmail("你有" + count + "条订单未出票", sysUserDO.getEmail(), shop.getName());
                }
            }
        }
    }

    @Override
    @TenantIgnore
    @Transactional(rollbackFor = Exception.class)
    public BaseVO clearFlow(OrderFlowWaterDTO orderFlowWater, String user) {
        SysUserDO sysUserDO = sysUserMapper.selectOne(new QueryWrapper<SysUserDO>().lambda().eq(SysUserDO::getUsername, user));
        List<LotteryOrderDO> orderList = lotteryOrderMapper.selectList(new QueryWrapper<LotteryOrderDO>().lambda()
                .eq(LotteryOrderDO::getTenantId, sysUserDO.getTenantId())
                .ge(LotteryOrderDO::getCreateTime, orderFlowWater.getStartTime())
                .le(LotteryOrderDO::getCreateTime, orderFlowWater.getEndTime()));
        for (LotteryOrderDO lotteryOrder : orderList) {
            documentaryMapper.delete(new QueryWrapper<DocumentaryDO>().lambda().eq(DocumentaryDO::getLotteryOrderId, lotteryOrder.getId()));
            documentaryUserMapper.delete(new QueryWrapper<DocumentaryUserDO>().lambda().eq(DocumentaryUserDO::getLotteryOrderId, lotteryOrder.getId()));
            lotteryOrderMapper.deleteById(lotteryOrder.getId());
            if (lotteryOrder.getType().equals(LotteryOrderTypeEnum.ARRAY.getKey())
                    || lotteryOrder.getType().equals(LotteryOrderTypeEnum.ARRANGE.getKey()) ||
                    lotteryOrder.getType().equals(LotteryOrderTypeEnum.SEVEN_STAR.getKey()) ||
                    lotteryOrder.getType().equals(LotteryOrderTypeEnum.GRAND_LOTTO.getKey())) {
                permutationMapper.deleteBatchIds(Convert.toList(Integer.class, lotteryOrder.getTargetIds()));
            } else {
                racingBallMapper.deleteBatchIds(Convert.toList(Integer.class, lotteryOrder.getTargetIds()));
            }
        }
        logMapper.delete(new QueryWrapper<LogDO>().lambda()
                .eq(LogDO::getTenantId, sysUserDO.getTenantId())
                .ge(LogDO::getCreateTime, orderFlowWater.getStartTime())
                .le(LogDO::getCreateTime, orderFlowWater.getEndTime()));
        payOrderMapper.delete(new QueryWrapper<PayOrderDO>().lambda()
                .eq(PayOrderDO::getTenantId, sysUserDO.getTenantId())
                .ge(PayOrderDO::getCreateTime, orderFlowWater.getStartTime())
                .le(PayOrderDO::getCreateTime, orderFlowWater.getEndTime()));
        withdrawalMapper.delete(new QueryWrapper<WithdrawalDO>().lambda()
                .eq(WithdrawalDO::getTenantId, sysUserDO.getTenantId())
                .ge(WithdrawalDO::getCreateTime, orderFlowWater.getStartTime())
                .le(WithdrawalDO::getCreateTime, orderFlowWater.getEndTime()));
        return new BaseVO();
    }

    private void addRecord(LotteryOrderDO lotteryOrder) {
        PayOrderDO payOrder = new PayOrderDO();
        payOrder.setOrderId(OrderNumberGenerationUtil.getOrderId());
        String type = "";
        if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.FOOTBALL.getKey())) {
            type = PayOrderTypeEnum.FOOTBALL_AWARD.getKey();
        } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.BASKETBALL.getKey())) {
            type = PayOrderTypeEnum.BASKETBALL_AWARD.getKey();
        } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.SINGLE.getKey())) {
            type = PayOrderTypeEnum.SINGLE_AWARD.getKey();
        } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.ARRAY.getKey())) {
            type = PayOrderTypeEnum.ARRAY_AWARD.getKey();
        } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.ARRANGE.getKey())) {
            type = PayOrderTypeEnum.ARRANGE_AWARD.getKey();
        } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.SEVEN_STAR.getKey())) {
            type = PayOrderTypeEnum.SEVEN_STAR_AWARD.getKey();
        } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.VICTORY_DEFEAT.getKey())) {
            type = PayOrderTypeEnum.VICTORY_DEFEAT_AWARD.getKey();
        } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.REN_JIU.getKey())) {
            type = PayOrderTypeEnum.REN_JIU_AWARD.getKey();
        } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.GRAND_LOTTO.getKey())) {
            type = PayOrderTypeEnum.GRAND_LOTTO_AWARD.getKey();
            //wyong edit 福彩3D派奖
        } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.FC3D.getKey())) {
            type = PayOrderTypeEnum.FC3D_AWARD.getKey();
        }
        payOrder.setType(type);
        payOrder.setState(PayOrderStateEnum.PAYMENT.getKey());
        payOrder.setCreateTime(new Date());
        payOrder.setUpdateTime(new Date());
        payOrder.setPayType(PayTypeEnum.APP.getKey());
        payOrder.setUserId(lotteryOrder.getUserId());
        payOrder.setPrice(lotteryOrder.getWinPrice());
        payOrderMapper.insert(payOrder);
    }

    private void addPayRecord(LotteryOrderDO lotteryOrder) {
        PayOrderDO payOrder = new PayOrderDO();
        payOrder.setOrderId(OrderNumberGenerationUtil.getOrderId());
        String type = "";
        if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.FOOTBALL.getKey())) {
            type = PayOrderTypeEnum.FOOTBALL_REFUND.getKey();
        } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.BASKETBALL.getKey())) {
            type = PayOrderTypeEnum.BASKETBALL_REFUND.getKey();
        } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.SINGLE.getKey())) {
            type = PayOrderTypeEnum.SINGLE_REFUND.getKey();
        } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.ARRAY.getKey())) {
            type = PayOrderTypeEnum.ARRAY_REFUND.getKey();
        } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.ARRANGE.getKey())) {
            type = PayOrderTypeEnum.ARRANGE_REFUND.getKey();
        } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.SEVEN_STAR.getKey())) {
            type = PayOrderTypeEnum.SEVEN_STAR_REFUND.getKey();
        } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.VICTORY_DEFEAT.getKey())) {
            type = PayOrderTypeEnum.VICTORY_DEFEAT_REFUND.getKey();
        } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.REN_JIU.getKey())) {
            type = PayOrderTypeEnum.REN_JIU_REFUND.getKey();
        } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.GRAND_LOTTO.getKey())) {
            type = PayOrderTypeEnum.GRAND_LOTTO_REFUND.getKey();
        }
        payOrder.setType(type);
        payOrder.setState(PayOrderStateEnum.PAYMENT.getKey());
        payOrder.setCreateTime(new Date());
        payOrder.setUpdateTime(new Date());
        payOrder.setPayType(PayTypeEnum.APP.getKey());
        payOrder.setUserId(lotteryOrder.getUserId());
        payOrder.setPrice(lotteryOrder.getPrice());
        payOrderMapper.insert(payOrder);
    }
}
