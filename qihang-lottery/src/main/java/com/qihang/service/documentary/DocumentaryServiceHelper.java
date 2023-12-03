package com.qihang.service.documentary;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.qihang.common.util.order.OrderNumberGenerationUtil;
import com.qihang.common.vo.BaseVO;
import com.qihang.controller.documentary.app.dto.CreateDocumentaryUserDTO;
import com.qihang.controller.racingball.app.vo.RacingBallOrderVO;
import com.qihang.domain.documentary.DocumentaryDO;
import com.qihang.domain.documentary.DocumentaryUserDO;
import com.qihang.domain.order.LotteryOrderDO;
import com.qihang.domain.order.PayOrderDO;
import com.qihang.domain.permutation.PermutationDO;
import com.qihang.domain.racingball.RacingBallDO;
import com.qihang.domain.user.UserDO;
import com.qihang.enumeration.error.ErrorCodeEnum;
import com.qihang.enumeration.order.pay.PayOrderStateEnum;
import com.qihang.enumeration.order.pay.PayTypeEnum;
import com.qihang.mapper.documentary.DocumentaryUserMapper;
import com.qihang.mapper.order.LotteryOrderMapper;
import com.qihang.mapper.order.PayOrderMapper;
import com.qihang.mapper.permutation.PermutationMapper;
import com.qihang.mapper.racingball.RacingBallMapper;
import com.qihang.mapper.user.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class DocumentaryServiceHelper {

    DocumentaryDO documentaryDO = null;
    UserDO userDo = null;
    LotteryOrderDO lotteryOrderDO = null;
    List<RacingBallDO> racingBallList = null;

    List<PermutationDO> permutationList = null;
    CreateDocumentaryUserDTO createDocumentaryUserDTO = null;

    @Resource
    UserMapper userMapper;
    @Resource
    RacingBallMapper racingBallMapper;
    @Resource
    LotteryOrderMapper lotteryOrderMapper;
    @Resource
    DocumentaryUserMapper documentaryUserMapper;
    @Resource
    PayOrderMapper payOrderMapper;

    @Resource
    PermutationMapper permutationMapper;

    List<RacingBallDO> racingBallDOS = new ArrayList<>();

    List<PermutationDO> permutationDOS = new ArrayList<>();

    //注数
    int notes;

    //彩种
    String type;

    //玩法
    String play;


    public BaseVO templateDocumentary(CreateDocumentaryUserDTO createDocumentaryUserDTO, DocumentaryDO documentaryDO, UserDO userDO,
                                      LotteryOrderDO lotteryOrderDO) {
        this.documentaryDO = documentaryDO;
        this.userDo = userDO;
        this.lotteryOrderDO = lotteryOrderDO;
        this.createDocumentaryUserDTO = createDocumentaryUserDTO;
        racingBallDOS = new ArrayList<>();
        permutationDOS = new ArrayList<>();
        //彩种ID
        this.type = lotteryOrderDO.getType();
        //查询发单用户的下注信息
        //判断自己不能跟自己的单
        if (ObjectUtil.equal(documentaryDO.getUserId(), userDO.getId())) {
            return new BaseVO(false, ErrorCodeEnum.E080.getKey(), ErrorCodeEnum.E080.getValue());
        }
        //判断余额是否足够
        if (!descieUserMoneyPay()) {
            return new BaseVO(false, ErrorCodeEnum.E0763.getKey(), ErrorCodeEnum.E0763.getValue());
        }
        //总金额
        BigDecimal price = BigDecimal.valueOf(notes * 2 * createDocumentaryUserDTO.getMultiple());
        //扣除用户余额
        subUserMoney();
        //复制订单
        LotteryOrderDO order = copyOrder();
        //入库
        RacingBallOrderVO racingBallOrder = create(order, price);
        return racingBallOrder;
    }

    private void subUserMoney() {
        BigDecimal price = BigDecimal.valueOf(notes * 2 * createDocumentaryUserDTO.getMultiple());
        if (price.compareTo(userDo.getGold()) == 1) {
            //直接扣除彩金
            BigDecimal remainingPrice = price.subtract(userDo.getGold());
            //彩金直接设置为0
            userDo.setGold(new BigDecimal(0));
            //剩下的扣除奖金余额
            userDo.setPrice(userDo.getPrice().subtract(remainingPrice));
            userMapper.updateById(userDo);
        } else {
            //直接扣除彩金
            userDo.setGold(userDo.getGold().subtract(price));
            userMapper.updateById(userDo);
        }
    }

    private boolean descieUserMoneyPay() {
        //总金额
        BigDecimal price = BigDecimal.valueOf(notes * 2 * createDocumentaryUserDTO.getMultiple());
        if (price.compareTo(userDo.getGold()) == 1) {
            if (price.compareTo(userDo.getGold().add(userDo.getPrice())) == 1) {
                return false;
            }
        }
        return true;
    }

    private LotteryOrderDO copyOrder() {
        BigDecimal price = BigDecimal.valueOf(notes * 2 * createDocumentaryUserDTO.getMultiple());
        //添加跟单下注信息
        List<String> contentList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(racingBallList)) {
            for (RacingBallDO racingBallDO : racingBallList) {
                RacingBallDO racingBall = new RacingBallDO();
                racingBall.setUserId(userDo.getId());
                racingBall.setCreateTime(new Date());
                racingBall.setUpdateTime(new Date());
                racingBall.setNotes(racingBallDO.getNotes());
                racingBall.setType(racingBallDO.getType());
                racingBall.setTimes(createDocumentaryUserDTO.getMultiple());
                racingBall.setContent(racingBallDO.getContent());
                racingBall.setTargetId(racingBallDO.getTargetId());
                racingBall.setTenantId(userDo.getTenantId());
                racingBall.setGameNo(racingBallDO.getGameNo());
                racingBallDOS.add(racingBall);
                contentList.add(racingBallDO.getContent());
            }
        }
        if (!CollectionUtils.isEmpty(permutationList)) {
            for (PermutationDO permutationDO : permutationList) {
                PermutationDO permutation = new PermutationDO();
                BeanUtils.copyProperties(permutationDO, permutation);
                permutation.setId(null);
                permutation.setUserId(userDo.getId());
                permutation.setCreateTime(new Date());
                permutation.setUpdateTime(new Date());
                permutation.setTimes(createDocumentaryUserDTO.getMultiple());
                permutation.setTenantId(userDo.getTenantId());
                permutationDOS.add(permutation);
            }
        }
        //添加订单信息
        LotteryOrderDO lotteryOrder = new LotteryOrderDO();
        lotteryOrder.setOrderId(OrderNumberGenerationUtil.getOrderId());
        lotteryOrder.setUserId(userDo.getId());
        lotteryOrder.setPrice(price);
        // ids.add(racingBall.getId());入库时候再赋值
        //lotteryOrder.setTargetIds(StrUtil.join(",", ids));
        lotteryOrder.setType(lotteryOrderDO.getType());

        //根据类型计算预测奖金
        BigDecimal forestMoney = calculationForest(contentList, lotteryOrderDO.getType(), notes, createDocumentaryUserDTO.getMultiple(), play);
        lotteryOrder.setForecast(forestMoney);
        lotteryOrder.setCreateTime(new Date());
        lotteryOrder.setUpdateTime(new Date());
        lotteryOrder.setTenantId(userDo.getTenantId());
        //数字型 数据入库
        lotteryOrder.setSchemeDetails(lotteryOrderDO.getSchemeDetails());
        return lotteryOrder;

    }

    public void setRacingBallList(List<RacingBallDO> racingBallList) {
        this.racingBallList = racingBallList;
        //赛事的注数统一了。
        this.notes = racingBallList.get(0).getNotes();
        this.play = racingBallList.get(0).getType();
    }

    public void setPermutationDOS(List<PermutationDO> permutationList) {
        this.permutationList = permutationList;
        this.notes = permutationList.stream().mapToInt(PermutationDO::getNotes).sum();
    }

    public BigDecimal calculationForest(List<String> contentList, String type, int notes, int multiple, String passway) {
        return BigDecimal.ZERO;
    }

    private RacingBallOrderVO create(LotteryOrderDO lotteryOrder, BigDecimal price) {
        List<Integer> ids = new ArrayList<>();
        if (!CollectionUtils.isEmpty(racingBallDOS)) {
            for (RacingBallDO racingBallDO : racingBallDOS) {
                racingBallMapper.insert(racingBallDO);
                ids.add(racingBallDO.getId());
            }
        }
        if (!CollectionUtils.isEmpty(permutationDOS)) {
            for (PermutationDO permutationDO : permutationDOS) {
                permutationMapper.insert(permutationDO);
                ids.add(permutationDO.getId());
            }
        }
        lotteryOrder.setTargetIds(StrUtil.join(",", ids));
        lotteryOrderMapper.insert(lotteryOrder);

        //跟单记录入库
        DocumentaryUserDO documentaryUser = new DocumentaryUserDO();
        documentaryUser.setUserId(userDo.getId());
        documentaryUser.setLotteryOrderId(lotteryOrder.getId());
        documentaryUser.setDocumentaryId(createDocumentaryUserDTO.getDocumentaryId());
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
        payOrder.setUserId(userDo.getId());
        payOrder.setPrice(price);
        payOrder.setTenantId(userDo.getTenantId());
        payOrder.setType(lotteryOrderDO.getType());
        payOrderMapper.insert(payOrder);
        RacingBallOrderVO racingBallOrderVO = new RacingBallOrderVO();
        racingBallOrderVO.setId(lotteryOrder.getId());
        return racingBallOrderVO;
    }
}
