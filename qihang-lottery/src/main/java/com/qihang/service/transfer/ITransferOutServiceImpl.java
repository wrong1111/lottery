package com.qihang.service.transfer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qihang.annotation.TenantIgnore;
import com.qihang.common.util.SpringContextUtils;
import com.qihang.common.vo.BaseDataVO;
import com.qihang.common.vo.BaseVO;
import com.qihang.constant.TransferEnum;
import com.qihang.controller.order.admin.lottery.vo.SportSchemeDetailsListVO;
import com.qihang.controller.shop.app.vo.ShopVO;
import com.qihang.controller.transferOut.app.dto.ChangeOrderDTO;
import com.qihang.controller.transferOut.app.vo.TransferLotteryVO;
import com.qihang.domain.order.LotteryOrderDO;
import com.qihang.domain.permutation.PermutationDO;
import com.qihang.domain.racingball.RacingBallDO;
import com.qihang.domain.shop.ShopDO;
import com.qihang.domain.transfer.LotteryTransferDO;
import com.qihang.domain.transfer.ShopTransferDO;
import com.qihang.domain.user.UserDO;
import com.qihang.mapper.order.LotteryOrderMapper;
import com.qihang.mapper.shop.ShopMapper;
import com.qihang.mapper.transfer.LotteryTransferMapper;
import com.qihang.mapper.user.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    @TenantIgnore
    @Override
    public BaseVO createSportOrder(LotteryOrderDO lotteryOrderDO, List<RacingBallDO> racingBallDOList, String key) {
        Integer lotteryId = Integer.valueOf(lotteryOrderDO.getType());
        //判断此账户开通接单 与否
        ShopTransferDO shopTransferDO = TransferServiceImpl.SHOP_TRANSFER_MAP.get(key);
        if (null == shopTransferDO) {
            return BaseVO.builder().success(false).errorMsg("未开通，请联系商家").build();
        }
        if (!"0".equals(shopTransferDO.getInterfaceState())) {
            return BaseVO.builder().success(false).errorMsg("暂停收单，请联系商家").build();
        }
        UserDO userDO = userMapper.selectOne(new QueryWrapper<UserDO>().lambda().eq(UserDO::getUid, shopTransferDO.getUid()));
        if (null == userDO) {
            return BaseVO.builder().success(false).errorMsg("账号异常，请联系商家").build();
        }
        //判断此彩种是否收单
        LotteryTransferDO lotteryTransferDOS = lotteryTransferMapper.selectOne(new QueryWrapper<LotteryTransferDO>().lambda().eq(LotteryTransferDO::getLotteryType, lotteryId).eq(LotteryTransferDO::getTransferFlag, TransferEnum.TransferIn.code));
        if (null == lotteryTransferDOS || lotteryTransferDOS.getStates() != 0) {
            return BaseVO.builder().success(false).errorMsg("彩种暂停收单，请联系商家").build();
        }
        //判断此账户对应的余额是否足够支付
        BigDecimal money = BigDecimal.ZERO;
        List<SportSchemeDetailsListVO> listVOList = JSONUtil.toList(lotteryOrderDO.getSchemeDetails(), SportSchemeDetailsListVO.class);
        money = listVOList.stream().map(sportSchemeDetailsListVO -> BigDecimal.valueOf(Integer.valueOf(sportSchemeDetailsListVO.getNotes()))).reduce(BigDecimal::add).get().multiply(new BigDecimal(2));
        if (money.compareTo(lotteryOrderDO.getPrice()) != 0) {
            return BaseVO.builder().success(false).errorMsg("投注金额【" + lotteryOrderDO.getPrice().toPlainString() + "】与实际不符【" + money.toPlainString() + "】").build();
        }
        //判断此单是否已经下过单
        String orderNo = key + lotteryOrderDO.getOrderId();
        lotteryOrderDO.setOrderId(orderNo);
        if (lotteryOrderMapper.selectCount(new QueryWrapper<LotteryOrderDO>().lambda().eq(LotteryOrderDO::getOrderId, orderNo)) > 0) {
            return BaseVO.builder().success(false).errorMsg("此单已经下过单，请勿重复下单").build();
        }
        //判断此单是否已经超过最后下单时间
        //修改订单发起人为 账户对应的会员账户uid
        //调整订单 内容的对阵ID。
        //入库，写记录
        return null;
    }

    @TenantIgnore
    @Override
    public BaseVO createDigitOrder(LotteryOrderDO lotteryOrderDO, List<PermutationDO> permutationDOS, String key) {
        return null;
    }


    private boolean isSports(Integer lotteryId) {
        return (lotteryId == 0 || lotteryId == 2 || lotteryId == 1 ||
                lotteryId == 6 || lotteryId == 7) ? true : false;

    }
}
