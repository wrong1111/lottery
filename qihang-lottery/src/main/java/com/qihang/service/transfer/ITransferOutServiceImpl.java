package com.qihang.service.transfer;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qihang.annotation.TenantIgnore;
import com.qihang.common.vo.BaseDataVO;
import com.qihang.common.vo.BaseVO;
import com.qihang.controller.shop.app.vo.ShopVO;
import com.qihang.controller.transferOut.app.vo.TransferLotteryVO;
import com.qihang.domain.shop.ShopDO;
import com.qihang.domain.transfer.LotteryTransferDO;
import com.qihang.domain.transfer.ShopTransferDO;
import com.qihang.domain.user.UserDO;
import com.qihang.mapper.shop.ShopMapper;
import com.qihang.mapper.transfer.LotteryTransferMapper;
import com.qihang.mapper.user.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
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
}
