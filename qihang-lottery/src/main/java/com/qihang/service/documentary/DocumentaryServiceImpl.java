package com.qihang.service.documentary;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qihang.annotation.TenantIgnore;
import com.qihang.common.util.order.OrderNumberGenerationUtil;
import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.constant.Constant;
import com.qihang.controller.basketball.dto.BasketballMatchDTO;
import com.qihang.controller.beidan.dto.BeiDanMatchDTO;
import com.qihang.controller.documentary.app.dto.CreateDocumentaryDTO;
import com.qihang.controller.documentary.app.dto.CreateDocumentaryUserDTO;
import com.qihang.controller.documentary.app.vo.*;
import com.qihang.controller.football.dto.FootballMatchDTO;
import com.qihang.controller.order.app.lottery.vo.BallInfoVO;
import com.qihang.controller.racingball.app.dto.BallCalculationDTO;
import com.qihang.controller.racingball.app.vo.BallCalculationVO;
import com.qihang.controller.racingball.app.vo.RacingBallOrderVO;
import com.qihang.controller.winburden.dto.WinBurdenMatchDTO;
import com.qihang.domain.ballgame.BallGameDO;
import com.qihang.domain.basketball.BasketballMatchDO;
import com.qihang.domain.beidan.BeiDanMatchDO;
import com.qihang.domain.documentary.DocumentaryDO;
import com.qihang.domain.documentary.DocumentaryUserDO;
import com.qihang.domain.follow.FollowDO;
import com.qihang.domain.football.FootballMatchDO;
import com.qihang.domain.order.LotteryOrderDO;
import com.qihang.domain.order.PayOrderDO;
import com.qihang.domain.permutation.PermutationAwardDO;
import com.qihang.domain.permutation.PermutationDO;
import com.qihang.domain.racingball.RacingBallDO;
import com.qihang.domain.user.UserDO;
import com.qihang.domain.winburden.WinBurdenMatchDO;
import com.qihang.enumeration.error.ErrorCodeEnum;
import com.qihang.enumeration.follow.FollowEnum;
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
import com.qihang.mapper.follow.FollowMapper;
import com.qihang.mapper.football.FootballMatchMapper;
import com.qihang.mapper.order.LotteryOrderMapper;
import com.qihang.mapper.order.PayOrderMapper;
import com.qihang.mapper.permutation.PermutationAwardMapper;
import com.qihang.mapper.permutation.PermutationMapper;
import com.qihang.mapper.racingball.RacingBallMapper;
import com.qihang.mapper.user.UserMapper;
import com.qihang.mapper.winburden.WinBurdenMatchMapper;
import com.qihang.service.basketball.IBasketballMatchService;
import com.qihang.service.beidan.IBeiDanMatchService;
import com.qihang.service.documentary.forest.BasketballHelper;
import com.qihang.service.documentary.forest.FootballHelper;
import com.qihang.service.football.IFootballMatchService;
import com.qihang.service.winburden.IWinBurdenMatchService;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author bright
 * @since 2022-11-10
 */
@Service
public class DocumentaryServiceImpl extends ServiceImpl<DocumentaryMapper, DocumentaryDO> implements IDocumentaryService {

    @Resource
    private DocumentaryMapper documentaryMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private LotteryOrderMapper lotteryOrderMapper;

    @Resource
    private FollowMapper followMapper;

    @Resource
    private RacingBallMapper racingBallMapper;

    @Resource
    private BasketballMatchMapper basketballMatchMapper;

    @Resource
    private FootballMatchMapper footballMatchMapper;

    @Resource
    BeiDanMatchMapper beiDanMatchMapper;

    @Resource
    WinBurdenMatchMapper winBurdenMatchMapper;

    @Resource
    private BallGameMapper ballGameMapper;

    @Resource
    private DocumentaryUserMapper documentaryUserMapper;

    @Resource
    PermutationAwardMapper permutationAwardMapper;


    @Resource
    PermutationMapper permutationMapper;

    @Override
    @TenantIgnore
    public BaseVO create(CreateDocumentaryDTO createDocumentary, Integer userId) {
        DocumentaryDO documentary = new DocumentaryDO();
        BeanUtils.copyProperties(createDocumentary, documentary);
        documentary.setUserId(userId);
        documentary.setCreateTime(new Date());
        documentary.setUpdateTime(new Date());
        documentaryMapper.insert(documentary);
        return new BaseVO();
    }

    @Override
    @TenantIgnore
    public DocumentarySagaVO ranking() {
        DocumentarySagaVO documentarySaga = new DocumentarySagaVO();
        List<DocumentaryDO> documentaryList = documentaryMapper.selectList(new QueryWrapper<DocumentaryDO>().select("DISTINCT user_id"));
        //达人用户列表
        List<UserRankingVO> expertUserList = new ArrayList<>();

        //连红用户列表
        List<UserRankingVO> lianHongUserList = new ArrayList<>();
        //胜率用户列表
        List<UserRankingVO> winUserList = new ArrayList<>();
        //盈利用户列表
        List<UserRankingVO> profitUserList = new ArrayList<>();
        for (DocumentaryDO documentary : documentaryList) {
            //查询用户
            UserDO user = userMapper.selectById(documentary.getUserId());
            UserRankingVO userRanking = new UserRankingVO();
            BeanUtils.copyProperties(user, userRanking);
            //设置默认值
            userRanking.setLianHongCount(0);
            userRanking.setTotalPrice(BigDecimal.ZERO);
            userRanking.setWincCount(0);
            //根据用户id查询这个用户发布的跟单列表
            List<DocumentaryDO> list = documentaryMapper.selectList(new QueryWrapper<DocumentaryDO>().lambda().eq(DocumentaryDO::getUserId, documentary.getUserId()));
            //查询他的投注信息
            List<Integer> ids = new ArrayList<>();
            for (DocumentaryDO documentaryDO : list) {
                ids.add(documentaryDO.getLotteryOrderId());
            }
            //最近几场中奖的总金额
            BigDecimal totalPrice = new BigDecimal(0);
            //中奖数量
            Integer winCount = 0;
            List<LotteryOrderDO> orderList = lotteryOrderMapper.selectList(new QueryWrapper<LotteryOrderDO>().lambda().in(LotteryOrderDO::getId, ids));
            for (LotteryOrderDO lotteryOrderDO : orderList) {
                if (lotteryOrderDO.getState().equals(LotteryOrderStateEnum.WAITING_AWARD.getKey()) || lotteryOrderDO.getState().equals(LotteryOrderStateEnum.ALREADY_AWARD.getKey())) {
                    winCount++;
                    totalPrice = totalPrice.add(lotteryOrderDO.getWinPrice());
                }
            }

            userRanking.setWincCount(winCount);
            expertUserList.add(userRanking);

            //获取最近的下单数量
            Integer count = orderList.size();
            //计算胜率 100/下单的数量*中奖的数量
            Integer win = 0;
            if (winCount != 0) {
                win = 100 / count * winCount;
            }

            userRanking.setWin(win);
            winUserList.add(userRanking);

            //匹配查询最近30场的订单连中数量
            orderList = lotteryOrderMapper.selectList(new QueryWrapper<LotteryOrderDO>().lambda().in(LotteryOrderDO::getId, ids).orderByDesc(LotteryOrderDO::getCreateTime).last("limit 30"));
            Integer lianHongCount = 0;
            List<Integer> lianHongList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(orderList)) {
                for (LotteryOrderDO lotteryOrderDO : orderList) {
                    if (lotteryOrderDO.getState().equals(LotteryOrderStateEnum.WAITING_AWARD.getKey()) || lotteryOrderDO.getState().equals(LotteryOrderStateEnum.ALREADY_AWARD.getKey())) {
                        lianHongCount++;
                    } else {
                        lianHongCount = 0;
                    }
                    lianHongList.add(lianHongCount);
                }
                if (!CollectionUtils.isEmpty(lianHongList)) {
                    userRanking.setLianHongCount(lianHongList.stream().max(Integer::compareTo).get());
                } else {
                    userRanking.setLianHongCount(0);
                }
            }
            lianHongUserList.add(userRanking);

            userRanking.setTotalPrice(totalPrice);
            profitUserList.add(userRanking);
        }

        //达人排行榜倒叙排序
        expertUserList = expertUserList.stream().sorted(Comparator.comparing(UserRankingVO::getWincCount).reversed()).limit(4).collect(Collectors.toList());
        //连红排序
        lianHongUserList = lianHongUserList.stream().sorted(Comparator.comparing(UserRankingVO::getLianHongCount).reversed()).limit(4).collect(Collectors.toList());
        //胜率排序
        winUserList = winUserList.stream().sorted(Comparator.comparing(UserRankingVO::getWin).reversed()).limit(4).collect(Collectors.toList());
        //中奖总金额排序
        profitUserList = profitUserList.stream().sorted(Comparator.comparing(UserRankingVO::getTotalPrice).reversed()).limit(4).collect(Collectors.toList());

        documentarySaga.setLianHongUserList(lianHongUserList);
        documentarySaga.setExpertUserList(expertUserList);
        documentarySaga.setWinUserList(winUserList);
        documentarySaga.setProfitUserList(profitUserList);
        return documentarySaga;
    }

    @Override
    @TenantIgnore
    public DocumentaryDetailsVO documentaryDetails(Integer userId, Integer targetId) {
        DocumentaryDetailsVO documentaryDetails = new DocumentaryDetailsVO();
        UserDO user = userMapper.selectById(targetId);
        documentaryDetails.setId(user.getId());
        documentaryDetails.setNickname(user.getNickname());
        documentaryDetails.setAvatar(user.getAvatar());
        documentaryDetails.setFans(user.getFans());
        //发单数量 只查未中奖 已中奖,已派奖三种状态
        List<DocumentaryDO> documentaryList = documentaryMapper.selectList(new QueryWrapper<DocumentaryDO>().lambda().eq(DocumentaryDO::getUserId, targetId));
        //倒叙排序
        documentaryList = documentaryList.stream().sorted(Comparator.comparing(DocumentaryDO::getCreateTime)).collect(Collectors.toList());
        Integer issueCount = 0;
        //累计奖金
        BigDecimal bonus = new BigDecimal(0.00);
        //跟单列表
        List<DocumentaryInfoVO> documentaryInfoList = new ArrayList<>();
        for (DocumentaryDO documentaryDO : documentaryList) {
            //只查未中奖 已中奖,已派奖三种状态
            LotteryOrderDO lotteryOrder = lotteryOrderMapper.selectById(documentaryDO.getLotteryOrderId());
            //跟单列表
            if (!StrUtil.equals(lotteryOrder.getState(), LotteryOrderStateEnum.TO_BE_ISSUED.getKey()) && !StrUtil.equals(lotteryOrder.getState(), LotteryOrderStateEnum.REFUSE.getKey()) && !StrUtil.equals(lotteryOrder.getState(), LotteryOrderStateEnum.REFUND.getKey())) {
                DocumentaryInfoVO documentaryInfo = new DocumentaryInfoVO();
                //查询下注列表
                List<RacingBallDO> racingBallList = racingBallMapper.selectBatchIds(Convert.toList(Integer.class, lotteryOrder.getTargetIds()));
                //取出比赛的ids
                List<Integer> ids = new ArrayList<>();
                for (RacingBallDO racingBallDO : racingBallList) {
                    ids.add(racingBallDO.getTargetId());
                }
                //根据订单类型根据对应的比赛ids查询对应比赛的信息
                //截止时间
                Date endTime = null;
                //彩票名称
                String name = "";
                //logo
                String url = "";
                if (lotteryOrder.getType().equals(LotteryOrderTypeEnum.FOOTBALL.getKey())) {
                    List<FootballMatchDO> footballMatchList = footballMatchMapper.selectBatchIds(ids);
                    //按截止时间升序
                    footballMatchList = footballMatchList.stream().sorted(Comparator.comparing(FootballMatchDO::getDeadline)).collect(Collectors.toList());
                    endTime = footballMatchList.get(0).getDeadline();
                    BallGameDO ballGameDO = ballGameMapper.selectList(new QueryWrapper<BallGameDO>().lambda().eq(BallGameDO::getName, LotteryOrderTypeEnum.FOOTBALL.getValue())).get(0);
                    name = ballGameDO.getName();
                    url = ballGameDO.getUrl();
                } else if (lotteryOrder.getType().equals(LotteryOrderTypeEnum.BASKETBALL.getKey())) {
                    List<BasketballMatchDO> basketballMatchList = basketballMatchMapper.selectBatchIds(ids);
                    //按截止时间升序
                    basketballMatchList = basketballMatchList.stream().sorted(Comparator.comparing(BasketballMatchDO::getDeadline)).collect(Collectors.toList());
                    endTime = basketballMatchList.get(0).getDeadline();
                    BallGameDO ballGameDO = ballGameMapper.selectList(new QueryWrapper<BallGameDO>().lambda().eq(BallGameDO::getName, LotteryOrderTypeEnum.BASKETBALL.getValue())).get(0);
                    name = ballGameDO.getName();
                    url = ballGameDO.getUrl();
                }
                //TODO 跟单详情
                documentaryInfo.setDocumentaryId(documentaryDO.getId());
                documentaryInfo.setName(name);
                documentaryInfo.setUrl(url);
                documentaryInfo.setCreateTime(documentaryDO.getCreateTime());
                documentaryInfo.setPrice(lotteryOrder.getPrice());

                //判断时间是否在下单之内
                Date date = new Date();
                if (date.compareTo(endTime) < 0) {
                    //下单时间之内
                    documentaryInfo.setType("0");
                } else {
                    if (lotteryOrder.getState().equals(LotteryOrderStateEnum.TO_BE_AWARDED.getKey())) {
                        documentaryInfo.setType("1");
                    } else if (lotteryOrder.getState().equals(LotteryOrderStateEnum.FAIL_TO_WIN.getKey())) {
                        documentaryInfo.setType("2");
                    } else if (lotteryOrder.getState().equals(LotteryOrderStateEnum.WAITING_AWARD.getKey()) || lotteryOrder.getState().equals(LotteryOrderStateEnum.ALREADY_AWARD.getKey())) {
                        documentaryInfo.setType("3");
                        documentaryInfo.setWinPrice(lotteryOrder.getWinPrice());
                    }
                }
                //查询用户跟单信息
                List<DocumentaryUserDO> documentaryUserList = documentaryUserMapper.selectList(new QueryWrapper<DocumentaryUserDO>().lambda().eq(DocumentaryUserDO::getDocumentaryId, documentaryDO.getId()));
                if (CollUtil.isEmpty(documentaryUserList)) {
                    documentaryInfo.setQuantity(0);
                } else {
                    documentaryInfo.setQuantity(documentaryUserList.size());
                }
                documentaryInfoList.add(documentaryInfo);
            }
            if (lotteryOrder.getState().equals(LotteryOrderStateEnum.FAIL_TO_WIN.getKey()) || lotteryOrder.getState().equals(LotteryOrderStateEnum.WAITING_AWARD.getKey()) || lotteryOrder.getState().equals(LotteryOrderStateEnum.ALREADY_AWARD.getKey())) {
                issueCount++;
            }
            //统计累计发单中的奖金
            if (lotteryOrder.getState().equals(LotteryOrderStateEnum.WAITING_AWARD.getKey()) || lotteryOrder.getState().equals(LotteryOrderStateEnum.ALREADY_AWARD.getKey())) {
                bonus = bonus.add(lotteryOrder.getWinPrice());
            }
        }
        //根据创建时间倒叙
        documentaryInfoList = documentaryInfoList.stream().sorted(Comparator.comparing(DocumentaryInfoVO::getCreateTime).reversed()).collect(Collectors.toList());
        documentaryDetails.setDocumentaryInfoList(documentaryInfoList);
        documentaryDetails.setIssueCount(issueCount);
        documentaryDetails.setBonus(bonus);

        //统计七日盈利和命中情况
        //由于排序了所有取他最后发的那一条的时间
        String today = "";
        if (CollUtil.isNotEmpty(documentaryList)) {
            Date createTime = documentaryList.get(documentaryList.size() - 1).getCreateTime();
            today = DateUtil.format(createTime, "yyyy-MM-dd");
        } else {
            today = DateUtil.today();
        }
        //七日之前的日期
        String beforeToday = DateUtil.offsetDay(DateUtil.parse(today), -7).toDateStr();
        today = today + " 23:59:59";
        beforeToday = beforeToday + " 00:00:00";
        //中奖总金额
        BigDecimal totalWinPrice = new BigDecimal(0);
        //下单总金额
        BigDecimal totalPlacePrice = new BigDecimal(0);
        //跟单数量
        Integer count = 0;
        //中奖数量
        Integer winCount = 0;
        //近五场战绩
        List<Boolean> recordList = new ArrayList<>();
        //查询七日之内的跟单
        documentaryList = documentaryMapper.selectList(new QueryWrapper<DocumentaryDO>().lambda().eq(DocumentaryDO::getUserId, targetId).ge(DocumentaryDO::getCreateTime, DateUtil.parse(beforeToday)).le(DocumentaryDO::getCreateTime, DateUtil.parse(today)).orderByDesc(DocumentaryDO::getCreateTime));
        for (DocumentaryDO documentaryDO : documentaryList) {
            LotteryOrderDO lotteryOrder = lotteryOrderMapper.selectById(documentaryDO.getLotteryOrderId());
            if (lotteryOrder.getState().equals(LotteryOrderStateEnum.WAITING_AWARD.getKey()) || lotteryOrder.getState().equals(LotteryOrderStateEnum.ALREADY_AWARD.getKey())) {
                //中奖金额统计
                totalWinPrice = totalWinPrice.add(lotteryOrder.getWinPrice());
            }
            //下单金额统计
            totalPlacePrice = totalPlacePrice.add(lotteryOrder.getPrice());
            //统计7日命中状态
            if (lotteryOrder.getState().equals(LotteryOrderStateEnum.FAIL_TO_WIN.getKey()) || lotteryOrder.getState().equals(LotteryOrderStateEnum.WAITING_AWARD.getKey()) || lotteryOrder.getState().equals(LotteryOrderStateEnum.ALREADY_AWARD.getKey())) {
                //所有下单数量，只统计未中奖和已中奖，已派奖三种状态，未开奖，不统计
                count++;
                if (lotteryOrder.getState().equals(LotteryOrderStateEnum.WAITING_AWARD.getKey()) || lotteryOrder.getState().equals(LotteryOrderStateEnum.ALREADY_AWARD.getKey())) {
                    //如有中奖计数加1
                    winCount++;
                    // 统计近五场战绩
                    if (recordList.size() < 5) {
                        //true中奖
                        recordList.add(true);
                    }
                }
                if (lotteryOrder.getState().equals(LotteryOrderStateEnum.FAIL_TO_WIN.getKey())) {
                    // 统计近五场战绩
                    if (recordList.size() < 5) {
                        //true未中奖
                        recordList.add(false);
                    }
                }
            }
        }
        documentaryDetails.setRecordList(recordList);
        BigDecimal decimal = new BigDecimal(0);
        if (totalPlacePrice.compareTo(BigDecimal.ZERO) != 0) {
            decimal = totalWinPrice.divide(totalPlacePrice, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
        }
        //保留二位小数
        decimal = decimal.setScale(2, RoundingMode.HALF_UP);
        //判断是否小于0
        if (decimal.compareTo(BigDecimal.ZERO) == -1) {
            //小于0直接设置为0
            documentaryDetails.setProfit(new BigDecimal(0));
        } else {
            //不小于0设置计算出来的盈利率
            documentaryDetails.setProfit(decimal);
        }
        documentaryDetails.setHit(count + "中" + winCount);
        //查询是否关注
        FollowDO follow = followMapper.selectOne(new QueryWrapper<FollowDO>().lambda().eq(FollowDO::getUserId, userId).eq(FollowDO::getTargetId, targetId));
        //如果为空就是没有关注，或者关注了又取消了也要做相应的判断处理
        if (ObjectUtil.isNull(follow)) {
            documentaryDetails.setIsFollow(false);
        } else {
            if (StrUtil.equals(follow.getType(), FollowEnum.FOLLOW.getKey())) {
                documentaryDetails.setIsFollow(true);
            } else {
                documentaryDetails.setIsFollow(false);
            }
        }
        return documentaryDetails;
    }

    @Override
    @TenantIgnore
    public CommonListVO<DocumentaryByTypeVO> queryDocumentaryByType(String type, Integer userId) {
        CommonListVO<DocumentaryByTypeVO> commonList = new CommonListVO<>();
        List<DocumentaryByTypeVO> documentaryByTypeList = new ArrayList<>();
        //查询关注的用户类型
        List<Integer> userIds = new ArrayList<>();
        if (type.equals("2")) {
            List<FollowDO> followList = followMapper.selectList(new QueryWrapper<FollowDO>().lambda().eq(FollowDO::getUserId, userId).eq(FollowDO::getType, FollowEnum.FOLLOW.getKey()));
            //没有关注用户直接返回
            if (CollUtil.isEmpty(followList)) {
                commonList.setVoList(documentaryByTypeList);
                return commonList;
            }
            for (FollowDO followDO : followList) {
                userIds.add(followDO.getTargetId());
            }
        }
        //查询待开奖的订单信息
        List<LotteryOrderDO> orderList = lotteryOrderMapper.selectList(new QueryWrapper<LotteryOrderDO>().lambda().eq(LotteryOrderDO::getState, LotteryOrderStateEnum.TO_BE_AWARDED.getKey()).in(CollUtil.isNotEmpty(userIds), LotteryOrderDO::getUserId, userIds));
        for (LotteryOrderDO lotteryOrderDO : orderList) {
            //根据订单号和类型查询发单信息
            List<DocumentaryDO> documentaryList = documentaryMapper.selectList(new QueryWrapper<DocumentaryDO>().lambda().eq(DocumentaryDO::getLotteryOrderId, lotteryOrderDO.getId()));
            if (CollUtil.isNotEmpty(documentaryList)) {
                for (DocumentaryDO documentaryDO : documentaryList) {
                    DocumentaryByTypeVO documentary = new DocumentaryByTypeVO();
                    //查詢发单是否在比赛截止时间之内
                    LotteryOrderDO lotteryOrder = lotteryOrderMapper.selectById(documentaryDO.getLotteryOrderId());
                    List<RacingBallDO> racingBallList = racingBallMapper.selectBatchIds(Convert.toList(Integer.class, lotteryOrder.getTargetIds()));
                    //取出比赛的ids
                    List<Integer> ids = new ArrayList<>();
                    for (RacingBallDO racingBallDO : racingBallList) {
                        ids.add(racingBallDO.getTargetId());
                    }
                    //根据订单类型根据对应的比赛ids查询对应比赛的信息
                    //截止时间
                    Date endTime = null;
                    //彩票名称
                    String name = "";
                    //logo
                    String url = "";
                    if (lotteryOrder.getType().equals(LotteryOrderTypeEnum.FOOTBALL.getKey())) {
                        List<FootballMatchDO> footballMatchList = footballMatchMapper.selectBatchIds(ids);
                        //按截止时间升序
                        footballMatchList = footballMatchList.stream().sorted(Comparator.comparing(FootballMatchDO::getDeadline)).collect(Collectors.toList());
                        endTime = footballMatchList.get(0).getDeadline();

                    } else if (lotteryOrder.getType().equals(LotteryOrderTypeEnum.BASKETBALL.getKey())) {
                        List<BasketballMatchDO> basketballMatchList = basketballMatchMapper.selectBatchIds(ids);
                        //按截止时间升序
                        basketballMatchList = basketballMatchList.stream().sorted(Comparator.comparing(BasketballMatchDO::getDeadline)).collect(Collectors.toList());
                        endTime = basketballMatchList.get(0).getDeadline();

                    } else if (lotteryOrder.getType().equals(LotteryOrderTypeEnum.SINGLE.getKey())) {
                        List<BeiDanMatchDO> beiDanMatchDOS = beiDanMatchMapper.selectBatchIds(ids);
                        //按截止时间升序
                        beiDanMatchDOS = beiDanMatchDOS.stream().sorted(Comparator.comparing(BeiDanMatchDO::getDeadline)).collect(Collectors.toList());
                        endTime = beiDanMatchDOS.get(0).getDeadline();
                    } else if (lotteryOrder.getType().equals(LotteryOrderTypeEnum.VICTORY_DEFEAT.getKey()) || lotteryOrder.getType().equals(LotteryOrderTypeEnum.REN_JIU.getKey())) {
                        List<WinBurdenMatchDO> winBurdenMatchDOS = winBurdenMatchMapper.selectBatchIds(ids);
                        //按截止时间升序
                        winBurdenMatchDOS = winBurdenMatchDOS.stream().sorted(Comparator.comparing(WinBurdenMatchDO::getDeadline)).collect(Collectors.toList());
                        endTime = winBurdenMatchDOS.get(0).getDeadline();
                    } else {
                        //所有数字彩目前定在晚上21:00停售
                        //当前期号
                        //TODO 数字彩截止时间需要完善
                        List<PermutationAwardDO> permutationAwardDOS = permutationAwardMapper.selectList(new QueryWrapper<PermutationAwardDO>().lambda().eq(PermutationAwardDO::getType, lotteryOrder.getType()).orderByDesc(PermutationAwardDO::getCreateTime));
                        if (CollectionUtils.isEmpty(permutationAwardDOS)) {
                            endTime = DateUtil.parse(DateUtil.format(new Date(), "yyyy-MM-dd") + " 21:00:00", "yyyy-MM-dd HH:mm:ss");
                        } else {
                            PermutationAwardDO lastAwardDO = permutationAwardDOS.get(0);
                            if (lotteryOrder.getStageNumber() != null && lotteryOrder.getStageNumber() > lastAwardDO.getStageNumber()) {
                                endTime = DateUtil.parse(DateUtil.format(new Date(), "yyyy-MM-dd") + " 21:00:00", "yyyy-MM-dd HH:mm:ss");
                            } else {
                                endTime = DateUtils.addDays(new Date(), -1);
                            }
                        }
                    }
                    String names = LotteryOrderTypeEnum.valueOFS(lotteryOrder.getType()).getValue();
                    BallGameDO ballGameDO = ballGameMapper.selectList(new QueryWrapper<BallGameDO>().lambda().eq(BallGameDO::getName, names)).get(0);
                    name = ballGameDO.getName();
                    url = ballGameDO.getUrl();
                    //判断时间是否在下单之内
                    Date date = new Date();
                    if (date.compareTo(endTime) < 0) {
                        //发单的查询对应的用户信息
                        UserDO userDO = userMapper.selectById(documentaryDO.getUserId());
                        documentary.setId(userDO.getId());
                        documentary.setDocumentaryId(documentaryDO.getId());
                        documentary.setNickname(userDO.getNickname());
                        documentary.setAvatar(userDO.getAvatar());
                        documentary.setDescribe(documentaryDO.getDescribe());
                        if (lotteryOrder.getForecast() != null) {
                            documentary.setRemuneration(lotteryOrder.getForecast().divide(lotteryOrder.getPrice(), 2, RoundingMode.HALF_UP));
                        } else {
                            documentary.setRemuneration(BigDecimal.ZERO);
                        }
                        documentary.setEndTime(endTime.getTime() - date.getTime());
                        documentary.setName(name);
                        documentary.setUrl(url);
                        documentary.setPrice(lotteryOrder.getPrice());
                        //查询跟单信息
                        List<DocumentaryUserDO> documentaryUserList = documentaryUserMapper.selectList(new QueryWrapper<DocumentaryUserDO>().lambda().eq(DocumentaryUserDO::getDocumentaryId, documentaryDO.getId()));
                        if (CollUtil.isEmpty(documentaryUserList)) {
                            documentary.setQuantity(0);
                            documentary.setTotalPrice(new BigDecimal(0.00));
                        } else {
                            documentary.setQuantity(documentaryUserList.size());
                            BigDecimal totalPrice = new BigDecimal(0.00);
                            for (DocumentaryUserDO documentaryUser : documentaryUserList) {
                                LotteryOrderDO lotteryOrderData = lotteryOrderMapper.selectById(documentaryUser.getLotteryOrderId());
                                totalPrice = totalPrice.add(lotteryOrderData.getPrice());
                            }
                            documentary.setTotalPrice(totalPrice);
                        }
                        documentaryByTypeList.add(documentary);
                    }
                }
            }
        }
        //如果是人气和关注的根据人气排序，其它根据总跟单金额排序
        if (type.equals("0") || type.equals("2")) {
            documentaryByTypeList = documentaryByTypeList.stream().sorted(Comparator.comparing(DocumentaryByTypeVO::getQuantity).reversed()).collect(Collectors.toList());
        } else {
            documentaryByTypeList = documentaryByTypeList.stream().sorted(Comparator.comparing(DocumentaryByTypeVO::getTotalPrice).reversed()).collect(Collectors.toList());
        }
        commonList.setVoList(documentaryByTypeList);
        return commonList;
    }


    @Override
    @TenantIgnore
    public DocumentaryByIdVO queryDocumentaryById(Integer id, Integer userId, Integer targetId) {
        DocumentaryByIdVO documentaryById = new DocumentaryByIdVO();
        //根据跟单id查询跟单信息
        DocumentaryDO documentary = documentaryMapper.selectById(id);
        //根据用户目标id查询用户信息
        UserDO user = userMapper.selectById(targetId);
        documentaryById.setId(user.getId());
        //描述
        documentaryById.setDescribe(documentary.getDescribe());
        //昵称
        documentaryById.setNickname(user.getNickname());
        //头像
        documentaryById.setAvatar(user.getAvatar());
        //佣金
        documentaryById.setCommission(documentary.getCommission());
        //跟单共享状态
        documentaryById.setState(documentary.getState());
        //查询是否关注
        FollowDO follow = followMapper.selectOne(new QueryWrapper<FollowDO>().lambda().eq(FollowDO::getUserId, userId).eq(FollowDO::getTargetId, targetId));
        //如果为空就是没有关注，或者关注了又取消了也要做相应的判断处理
        if (ObjectUtil.isNull(follow)) {
            documentaryById.setIsFollow(false);
        } else {
            if (StrUtil.equals(follow.getType(), FollowEnum.FOLLOW.getKey())) {
                documentaryById.setIsFollow(true);
            } else {
                documentaryById.setIsFollow(false);
            }
        }
        //跟单的用户
        List<DocumentaryUserVO> userList = new ArrayList<>();
        List<DocumentaryUserDO> documentaryUserList = documentaryUserMapper.selectList(new QueryWrapper<DocumentaryUserDO>().lambda().eq(DocumentaryUserDO::getDocumentaryId, id));
        BigDecimal totalDocumentaryPrice = new BigDecimal(0.00);
        for (DocumentaryUserDO documentaryUserDO : documentaryUserList) {
            DocumentaryUserVO documentaryUser = new DocumentaryUserVO();
            UserDO userDO = userMapper.selectById(documentaryUserDO.getUserId());
            documentaryUser.setNickname(userDO.getNickname());
            documentaryUser.setAvatar(userDO.getAvatar());
            //跟单金额
            LotteryOrderDO lotteryOrder = lotteryOrderMapper.selectById(documentaryUserDO.getLotteryOrderId());
            documentaryUser.setPrice(lotteryOrder.getPrice());
            //累计跟单总金额
            totalDocumentaryPrice = totalDocumentaryPrice.add(lotteryOrder.getPrice());
            userList.add(documentaryUser);
        }
        //根据投注的金额进行倒叙排序
        userList = userList.stream().sorted(Comparator.comparing(DocumentaryUserVO::getPrice).reversed()).collect(Collectors.toList());
        //判断跟单人数是否大于10条数据，大于10取前10条
        if (userList.size() > 10) {
            userList = userList.subList(0, 10);
        }
        documentaryById.setDocumentaryUserList(userList);
        //自购金额
        LotteryOrderDO lotteryOrder = lotteryOrderMapper.selectById(documentary.getLotteryOrderId());
        documentaryById.setPrice(lotteryOrder.getPrice());
        //订单状态
        documentaryById.setOrderState(lotteryOrder.getState());
        //订单id
        documentaryById.setOrderId(lotteryOrder.getId());
        //跟单总金额
        documentaryById.setTotalDocumentaryPrice(totalDocumentaryPrice);

        //彩种ID
        documentaryById.setLotId(lotteryOrder.getType());
        //只有赛 事类型 的才有
        //截止时间
        Date endTime = null;
        String name = "";
        String url = "";
        if (Constant.isSport(lotteryOrder.getType())) {
            //查询下注信息
            List<RacingBallDO> racingBallList = racingBallMapper.selectBatchIds(Convert.toList(Integer.class, lotteryOrder.getTargetIds()));
            //注数
            documentaryById.setNotes(racingBallList.get(0).getNotes());
            //倍数
            documentaryById.setTimes(racingBallList.get(0).getTimes());
            //过关类型
            documentaryById.setPssTypeList(Convert.toList(Integer.class, racingBallList.get(0).getType()));
            //启投金额
            documentaryById.setRiseThrowPrice(new BigDecimal(racingBallList.get(0).getNotes() * 2));
            //下注的比赛信息
            List<BallInfoVO> ballInfoList = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            for (RacingBallDO racingBall : racingBallList) {
                BallInfoVO ballInfo = new BallInfoVO();
                if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.FOOTBALL.getKey())) {
                    FootballMatchDTO footballMatch = JSONUtil.toBean(racingBall.getContent(), FootballMatchDTO.class);
                    //根据id查询足球数据
                    FootballMatchDO footballMatchDO = footballMatchMapper.selectById(footballMatch.getId());
                    ids.add(footballMatchDO.getId());
                    ballInfo.setHomeTeam(footballMatch.getHomeTeam());
                    ballInfo.setVisitingTeam(footballMatch.getVisitingTeam());
                    ballInfo.setNumber(footballMatch.getNumber());
                    ballInfo.setAward(footballMatchDO.getAward());
                    //投注内容
                    ballInfo.setContent(racingBall.getContent());
                    //赛果
                    ballInfo.setHalfFullCourt(footballMatchDO.getHalfFullCourt());
                    ballInfo.setLetBall(footballMatch.getLetBall());
                    ballInfoList.add(ballInfo);
                } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.BASKETBALL.getKey())) {
                    BasketballMatchDTO basketballMatch = JSONUtil.toBean(racingBall.getContent(), BasketballMatchDTO.class);
                    //根据id查询足球数据
                    BasketballMatchDO basketballMatchDO = basketballMatchMapper.selectById(basketballMatch.getId());
                    ids.add(basketballMatchDO.getId());
                    ballInfo.setHomeTeam(basketballMatch.getHomeTeam());
                    ballInfo.setVisitingTeam(basketballMatch.getVisitingTeam());
                    ballInfo.setNumber(basketballMatch.getNumber());
                    ballInfo.setAward(basketballMatchDO.getAward());
                    //投注内容
                    ballInfo.setContent(racingBall.getContent());
                    //赛果
                    ballInfo.setHalfFullCourt(basketballMatchDO.getHalfFullCourt());
                    ballInfo.setLetBall(basketballMatch.getCedePoints());
                    ballInfoList.add(ballInfo);
                } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.SINGLE.getKey())) {
                    BeiDanMatchDTO beiDanMatchDTO = JSONUtil.toBean(racingBall.getContent(), BeiDanMatchDTO.class);
                    BeiDanMatchDO beiDanMatchDO = beiDanMatchMapper.selectById(beiDanMatchDTO.getId());
                    ids.add(beiDanMatchDO.getId());
                    ballInfo.setHomeTeam(beiDanMatchDTO.getHomeTeam());
                    ballInfo.setVisitingTeam(beiDanMatchDTO.getVisitingTeam());
                    ballInfo.setNumber(beiDanMatchDTO.getNumber());
                    ballInfo.setAward(beiDanMatchDO.getAward());
                    ballInfo.setContent(racingBall.getContent());
                    ballInfo.setHalfFullCourt(beiDanMatchDO.getHalfFullCourt());
                    ballInfo.setLetBall(beiDanMatchDTO.getLetBall());
                    ballInfoList.add(ballInfo);
                } else if (StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.REN_JIU.getKey()) ||
                        StrUtil.equals(lotteryOrder.getType(), LotteryOrderTypeEnum.VICTORY_DEFEAT.getKey())) {
                    WinBurdenMatchDTO winBurdenMatch = JSONUtil.toBean(racingBall.getContent(), WinBurdenMatchDTO.class);
                    WinBurdenMatchDO winBurdenMatchDO = winBurdenMatchMapper.selectById(winBurdenMatch.getId());
                    ids.add(winBurdenMatch.getId());
                    ballInfo.setHomeTeam(winBurdenMatch.getHomeTeam());
                    ballInfo.setVisitingTeam(winBurdenMatch.getVisitingTeam());
                    ballInfo.setNumber(winBurdenMatch.getNumber());
                    ballInfo.setAward(winBurdenMatchDO.getAward());
                    ballInfo.setContent(racingBall.getContent());
                    ballInfo.setHalfFullCourt("");
                    ballInfo.setLetBall("");
                    ballInfo.setIsGallbladder(winBurdenMatch.getIsGallbladder());
                    ballInfoList.add(ballInfo);
                }
                //TODO 下注跟单
            }
            //投注比赛内容
            documentaryById.setBallInfoList(ballInfoList);
            if (lotteryOrder.getType().equals(LotteryOrderTypeEnum.FOOTBALL.getKey())) {
                List<FootballMatchDO> footballMatchList = footballMatchMapper.selectBatchIds(ids);
                //按截止时间升序
                footballMatchList = footballMatchList.stream().sorted(Comparator.comparing(FootballMatchDO::getDeadline)).collect(Collectors.toList());
                endTime = footballMatchList.get(0).getDeadline();

            } else if (lotteryOrder.getType().equals(LotteryOrderTypeEnum.BASKETBALL.getKey())) {
                List<BasketballMatchDO> basketballMatchList = basketballMatchMapper.selectBatchIds(ids);
                //按截止时间升序
                basketballMatchList = basketballMatchList.stream().sorted(Comparator.comparing(BasketballMatchDO::getDeadline)).collect(Collectors.toList());
                endTime = basketballMatchList.get(0).getDeadline();
            } else if (lotteryOrder.getType().equals(LotteryOrderTypeEnum.SINGLE.getKey())) {
                List<BeiDanMatchDO> beiDanMatchDOS = beiDanMatchMapper.selectBatchIds(ids);
                beiDanMatchDOS = beiDanMatchDOS.stream().sorted(Comparator.comparing(BeiDanMatchDO::getDeadline)).collect(Collectors.toList());
                endTime = beiDanMatchDOS.get(0).getDeadline();

            } else if (lotteryOrder.getType().equals(LotteryOrderTypeEnum.VICTORY_DEFEAT.getKey())
                    || lotteryOrder.getType().equals(LotteryOrderTypeEnum.REN_JIU.getKey())) {
                List<WinBurdenMatchDO> winBurdenMatchDOS = winBurdenMatchMapper.selectBatchIds(ids);
                winBurdenMatchDOS = winBurdenMatchDOS.stream().sorted(Comparator.comparing(WinBurdenMatchDO::getDeadline)).collect(Collectors.toList());
                endTime = winBurdenMatchDOS.get(0).getDeadline();
            }

        } else {
            //数字彩
            documentaryById.setContent(lotteryOrder.getSchemeDetails());
            //查询下注信息
            List<PermutationDO> permutationDOS = permutationMapper.selectBatchIds(Convert.toList(Integer.class, lotteryOrder.getTargetIds()));
            //注数
            documentaryById.setNotes(permutationDOS.stream().mapToInt(PermutationDO::getNotes).sum());
            //倍数
            documentaryById.setTimes(permutationDOS.get(0).getTimes());
            //启投金额
            documentaryById.setRiseThrowPrice(new BigDecimal(documentaryById.getNotes() * 2));
            //数字彩投注内容
            documentaryById.setPermutationList(permutationDOS);
            //获取最后的截止时间
            List<PermutationAwardDO> permutationAwardDOS = permutationAwardMapper.selectList(new QueryWrapper<PermutationAwardDO>().lambda().eq(PermutationAwardDO::getType, lotteryOrder.getType()).orderByDesc(PermutationAwardDO::getCreateTime));
            if (CollectionUtils.isEmpty(permutationAwardDOS)) {
                endTime = DateUtil.parse(DateUtil.format(new Date(), "yyyy-MM-dd") + " 21:00:00", "yyyy-MM-dd HH:mm:ss");
            } else {
                PermutationAwardDO lastAwardDO = permutationAwardDOS.get(0);
                if (lotteryOrder.getStageNumber() != null && lotteryOrder.getStageNumber() > lastAwardDO.getStageNumber()) {
                    endTime = DateUtil.parse(DateUtil.format(new Date(), "yyyy-MM-dd") + " 21:00:00", "yyyy-MM-dd HH:mm:ss");
                } else {
                    endTime = DateUtils.addDays(new Date(), -1);
                }
            }

        }
        BallGameDO ballGameDO = ballGameMapper.selectList(new QueryWrapper<BallGameDO>().lambda().eq(BallGameDO::getName, LotteryOrderTypeEnum.valueOFS(lotteryOrder.getType()).getValue())).get(0);
        name = ballGameDO.getName();
        url = ballGameDO.getUrl();
        //截止时间
        documentaryById.setDeadline(endTime);

        //logo
        documentaryById.setUrl(url);
        //彩票名称
        documentaryById.setName(name);
        //判断时间是否在下单之内
        Date date = new Date();
        if (date.compareTo(endTime) < 0) {
            documentaryById.setEndTime(endTime.getTime() - date.getTime());
            documentaryById.setType("0");
        } else {
            documentaryById.setType("1");
        }
        List<DocumentaryDO> documentaryList = documentaryMapper.selectList(new QueryWrapper<DocumentaryDO>().lambda().eq(DocumentaryDO::getUserId, targetId));
        //倒叙排序
        documentaryList = documentaryList.stream().sorted(Comparator.comparing(DocumentaryDO::getCreateTime)).collect(Collectors.toList());
        //统计七日盈利和命中情况
        //由于排序了所有取他最后发的那一条的时间
        String today = "";
        if (CollUtil.isNotEmpty(documentaryList)) {
            Date createTime = documentaryList.get(documentaryList.size() - 1).getCreateTime();
            today = DateUtil.format(createTime, "yyyy-MM-dd");
        } else {
            today = DateUtil.today();
        }
        //七日之前的日期
        String beforeToday = DateUtil.offsetDay(DateUtil.parse(today), -7).toDateStr();
        today = today + " 23:59:59";
        beforeToday = beforeToday + " 00:00:00";
        //查询近七日之内的跟单
        documentaryList = documentaryMapper.selectList(new QueryWrapper<DocumentaryDO>().lambda().eq(DocumentaryDO::getUserId, targetId).ge(DocumentaryDO::getCreateTime, DateUtil.parse(beforeToday)).le(DocumentaryDO::getCreateTime, DateUtil.parse(today)));
        BigDecimal totalWinPrice = new BigDecimal(0.00);
        BigDecimal totalPlacePrice = new BigDecimal(0.00);
        Integer count = 0;
        Integer winCount = 0;
        for (DocumentaryDO documentaryDO : documentaryList) {
            LotteryOrderDO order = lotteryOrderMapper.selectById(documentaryDO.getLotteryOrderId());
            if (order.getState().equals(LotteryOrderStateEnum.WAITING_AWARD.getKey()) || order.getState().equals(LotteryOrderStateEnum.ALREADY_AWARD.getKey())) {
                //中奖金额统计
                totalWinPrice = totalWinPrice.add(order.getWinPrice());
            }
            //下单金额统计
            totalPlacePrice = totalPlacePrice.add(lotteryOrder.getPrice());
            //统计7日命中状态
            if (order.getState().equals(LotteryOrderStateEnum.FAIL_TO_WIN.getKey()) || order.getState().equals(LotteryOrderStateEnum.WAITING_AWARD.getKey()) || order.getState().equals(LotteryOrderStateEnum.ALREADY_AWARD.getKey())) {
                //所有下单数量，只统计未中奖和已中奖，已派奖三种状态，未开奖，不统计
                count++;
                if (order.getState().equals(LotteryOrderStateEnum.WAITING_AWARD.getKey()) || order.getState().equals(LotteryOrderStateEnum.ALREADY_AWARD.getKey())) {
                    //如有中奖计数加1
                    winCount++;
                }
            }
        }
        BigDecimal decimal = new BigDecimal(0);
        if (totalPlacePrice.compareTo(BigDecimal.ZERO) != 0) {
            decimal = totalWinPrice.divide(totalPlacePrice, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
        }
        //保留二位小数
        decimal = decimal.setScale(2, RoundingMode.HALF_UP);
        //判断是否小于0
        if (decimal.compareTo(BigDecimal.ZERO) == -1) {
            //小于0直接设置为0
            documentaryById.setProfit(new BigDecimal(0));
        } else {
            //不小于0设置计算出来的盈利率
            documentaryById.setProfit(decimal);
        }
        documentaryById.setHit(count + "中" + winCount);
        return documentaryById;
    }

    @Resource
    DocumentaryServiceHelper documentaryServiceHelper;

    @Resource
    BasketballHelper basketballHelper;

    @Resource
    FootballHelper footballHelper;


    /*
     wyong edit 23-11-14
     重写一下接口
     */
    @Override
    @TenantIgnore
    @Transactional(rollbackFor = Exception.class)
    public BaseVO createDocumentaryUser(CreateDocumentaryUserDTO createDocumentaryUser, Integer userId) {
        //重写接口
        DocumentaryDO documentary = documentaryMapper.selectById(createDocumentaryUser.getDocumentaryId());

        //查询发单用户的下注信息
        LotteryOrderDO order = lotteryOrderMapper.selectById(createDocumentaryUser.getOrderId());

        //竞猜类型 的订单 在racingball
        //数字类型 的在 permution
        //发单的下注列表
        //获取当前登录的用户信息
        UserDO user = userMapper.selectById(userId);
        if (order.getType().equals(LotteryOrderTypeEnum.FOOTBALL.getKey())) {
            List<RacingBallDO> racingBallList = racingBallMapper.selectBatchIds(Convert.toList(Integer.class, order.getTargetIds()));
            footballHelper.setRacingBallList(racingBallList);
            return footballHelper.templateDocumentary(createDocumentaryUser, documentary, user, order);
        } else if (order.getType().equals(LotteryOrderTypeEnum.BASKETBALL.getKey())) {
            List<RacingBallDO> racingBallList = racingBallMapper.selectBatchIds(Convert.toList(Integer.class, order.getTargetIds()));
            basketballHelper.setRacingBallList(racingBallList);
            return basketballHelper.templateDocumentary(createDocumentaryUser, documentary, user, order);
        } else if (Constant.isSport(order.getType())) {
            //其他竞猜北单，胜负彩，任九
            List<RacingBallDO> racingBallList = racingBallMapper.selectBatchIds(Convert.toList(Integer.class, order.getTargetIds()));
            documentaryServiceHelper.setRacingBallList(racingBallList);
            return documentaryServiceHelper.templateDocumentary(createDocumentaryUser, documentary, user, order);
        } else {
            List<PermutationDO> permutationDOS = permutationMapper.selectBatchIds(Convert.toList(Integer.class, order.getTargetIds()));
            documentaryServiceHelper.setPermutationDOS(permutationDOS);
            return documentaryServiceHelper.templateDocumentary(createDocumentaryUser, documentary, user, order);
        }
        /**
         //判断不能自己跟自己的单
         DocumentaryDO documentary = documentaryMapper.selectById(createDocumentaryUser.getDocumentaryId());
         if (ObjectUtil.equal(documentary.getUserId(), userId)) {
         return new BaseVO(false, ErrorCodeEnum.E080.getKey(), ErrorCodeEnum.E080.getValue());
         }
         RacingBallOrderVO racingBallOrder = new RacingBallOrderVO();
         //查询发单用户的下注信息
         LotteryOrderDO order = lotteryOrderMapper.selectById(createDocumentaryUser.getOrderId());
         //发单的下注列表
         List<RacingBallDO> racingBallList = racingBallMapper.selectBatchIds(Convert.toList(Integer.class, order.getTargetIds()));

         //获取当前登录的用户信息
         UserDO user = userMapper.selectById(userId);
         //总金额
         BigDecimal price = BigDecimal.valueOf(racingBallList.get(0).getNotes() * 2 * createDocumentaryUser.getMultiple());
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
         List<Integer> ids = new ArrayList<>();
         //添加跟单下注信息
         //选中的足球比赛列表
         List<BasketballMatchDTO> basketballMatchList = new ArrayList<>();
         //选中的蓝球比赛列表
         List<FootballMatchDTO> footballMatchList = new ArrayList<>();
         for (RacingBallDO racingBallDO : racingBallList) {
         RacingBallDO racingBall = new RacingBallDO();
         racingBall.setUserId(userId);
         racingBall.setCreateTime(new Date());
         racingBall.setUpdateTime(new Date());
         racingBall.setNotes(racingBallDO.getNotes());
         racingBall.setType(racingBallDO.getType());
         racingBall.setTimes(createDocumentaryUser.getMultiple());
         racingBall.setContent(racingBallDO.getContent());
         racingBall.setTargetId(racingBallDO.getTargetId());
         racingBall.setTenantId(user.getTenantId());
         racingBallMapper.insert(racingBall);
         ids.add(racingBall.getId());
         if (order.getType().equals(LotteryOrderTypeEnum.FOOTBALL.getKey())) {
         footballMatchList.add(JSONUtil.toBean(racingBallDO.getContent(), FootballMatchDTO.class));
         } else if (order.getType().equals(LotteryOrderTypeEnum.BASKETBALL.getKey())) {
         basketballMatchList.add(JSONUtil.toBean(racingBallDO.getContent(), BasketballMatchDTO.class));
         }
         }
         //添加订单信息
         LotteryOrderDO lotteryOrder = new LotteryOrderDO();
         lotteryOrder.setOrderId(OrderNumberGenerationUtil.getOrderId());
         lotteryOrder.setUserId(userId);
         lotteryOrder.setPrice(price);
         lotteryOrder.setTargetIds(StrUtil.join(",", ids));
         lotteryOrder.setType(order.getType());
         //根据类型计算预测奖金
         String type = "";
         if (order.getType().equals(LotteryOrderTypeEnum.FOOTBALL.getKey())) {
         BallCalculationDTO ballCalculation = new BallCalculationDTO();
         ballCalculation.setType(order.getType());
         ballCalculation.setNotes(racingBallList.get(0).getNotes());
         ballCalculation.setMultiple(createDocumentaryUser.getMultiple());
         ballCalculation.setPssTypeList(Convert.toList(Integer.class, racingBallList.get(0).getType()));
         ballCalculation.setFootballMatchList(footballMatchList);
         BallCalculationVO calculation = footballMatchService.calculation(ballCalculation);
         lotteryOrder.setForecast(calculation.getMaxPrice());
         type = PayOrderTypeEnum.FOOTBALL.getKey();
         } else if (order.getType().equals(LotteryOrderTypeEnum.BASKETBALL.getKey())) {
         BallCalculationDTO ballCalculation = new BallCalculationDTO();
         ballCalculation.setType(order.getType());
         ballCalculation.setNotes(racingBallList.get(0).getNotes());
         ballCalculation.setMultiple(createDocumentaryUser.getMultiple());
         ballCalculation.setPssTypeList(Convert.toList(Integer.class, racingBallList.get(0).getType()));
         ballCalculation.setBasketballMatchList(basketballMatchList);
         BallCalculationVO calculation = basketballMatchService.calculation(ballCalculation);
         lotteryOrder.setForecast(calculation.getMaxPrice());
         type = PayOrderTypeEnum.BASKETBALL.getKey();
         }
         lotteryOrder.setCreateTime(new Date());
         lotteryOrder.setUpdateTime(new Date());
         lotteryOrder.setTenantId(user.getTenantId());
         lotteryOrderMapper.insert(lotteryOrder);
         racingBallOrder.setId(lotteryOrder.getId());

         //跟单记录入库
         DocumentaryUserDO documentaryUser = new DocumentaryUserDO();
         documentaryUser.setUserId(userId);
         documentaryUser.setLotteryOrderId(lotteryOrder.getId());
         documentaryUser.setDocumentaryId(createDocumentaryUser.getDocumentaryId());
         documentaryUser.setCreateTime(new Date());
         documentaryUser.setUpdateTime(new Date());
         documentaryUserMapper.insert(documentaryUser);

         //添加钱包消费记录
         PayOrderDO payOrder = new PayOrderDO();
         payOrder.setOrderId(OrderNumberGenerationUtil.getOrderId());
         payOrder.setState(PayOrderStateEnum.PAID.getKey());
         payOrder.setCreateTime(new Date());
         payOrder.setUpdateTime(new Date());
         payOrder.setPayType(PayTypeEnum.APP.getKey());
         payOrder.setUserId(userId);
         payOrder.setPrice(price);
         payOrder.setTenantId(user.getTenantId());
         payOrder.setType(type);
         payOrderMapper.insert(payOrder);
         return racingBallOrder;
         */
    }
}