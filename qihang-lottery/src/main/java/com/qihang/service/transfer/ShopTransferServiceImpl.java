package com.qihang.service.transfer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qihang.annotation.TenantIgnore;
import com.qihang.common.vo.BaseDataVO;
import com.qihang.common.vo.BaseVO;
import com.qihang.constant.TransferEnum;
import com.qihang.controller.transferIn.admin.dto.AdminShopTransferInDTO;
import com.qihang.controller.transferIn.admin.vo.AdminShopTransferInVO;
import com.qihang.domain.ballgame.BallGameDO;
import com.qihang.domain.transfer.LotteryTransferDO;
import com.qihang.domain.transfer.ShopTransferDO;
import com.qihang.mapper.ballgame.BallGameMapper;
import com.qihang.mapper.transfer.ShopTransferMapper;
import com.qihang.service.ballgame.IBallGameService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ShopTransferServiceImpl extends ServiceImpl<ShopTransferMapper, ShopTransferDO> implements IShopTransferService {
    @Resource
    private ShopTransferMapper shopTransferMapper;

    @Resource
    ILotteryTransferService lotteryTransferService;

    @Resource
    private BallGameMapper ballGameMapper;

    @TenantIgnore
    @Override
    public BaseVO editShopTransfer(AdminShopTransferInDTO vo) {
        ShopTransferDO transferDO = shopTransferMapper.selectById(vo.getId());
        if (ObjectUtil.isNotNull(transferDO)) {
            boolean update = false;
            if (StringUtils.isNotBlank(vo.getTransferInterface())) {
                transferDO.setTransferInterface(vo.getTransferInterface());
                update = true;
            }
            if (StringUtils.isNotBlank(vo.getTransferSecurty())) {
                transferDO.setTransferSecurty(vo.getTransferSecurty());
                update = true;
            }
            if (StringUtils.isNotBlank(vo.getShopConcatName())) {
                transferDO.setShopConcatName(vo.getShopConcatName());
                update = true;
            }
            if (StringUtils.isNotBlank(vo.getShopConcatPhone())) {
                transferDO.setShopConcatPhone(vo.getShopConcatPhone());
                update = true;
            }
            if (update) {
                shopTransferMapper.updateById(transferDO);
            }
        }
        return new BaseVO();
    }

    @TenantIgnore
    @Override
    public BaseVO listShopTransfer(TransferEnum transferEnum) {
        ShopTransferDO shopTransferDO = getOne(new QueryWrapper<ShopTransferDO>().lambda().eq(ShopTransferDO::getTransferType, TransferEnum.TransferIn.code));
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("transfer", "0");
        if (null != shopTransferDO) {
            dataMap.put("transfer", "1");
            dataMap.put("shop", shopTransferDO);
            List<LotteryTransferDO> lotteryTransferDOList = lotteryTransferService.list(new QueryWrapper<LotteryTransferDO>().lambda().eq(LotteryTransferDO::getTransferFlag, TransferEnum.TransferIn.code).orderByDesc(LotteryTransferDO::getCreateTime));
            if (CollectionUtils.isEmpty(lotteryTransferDOList)) {
                dataMap.put("lotterys", Collections.EMPTY_LIST);
            } else {
                List<AdminShopTransferInVO> list = BeanUtil.copyToList(lotteryTransferDOList, AdminShopTransferInVO.class);
                List<BallGameDO> ballGameDOS = ballGameMapper.selectList(new QueryWrapper<BallGameDO>().lambda().eq(BallGameDO::getTenantId, 1));
                Map<Integer, BallGameDO> ballGameDOMap = ballGameDOS.stream().collect(Collectors.toMap(BallGameDO::getLotid, a -> a, (b, c) -> b));
                for (AdminShopTransferInVO transfer : list) {
                    BallGameDO gameDO = ballGameDOMap.get(transfer.getLotteryType());
                    if (null != gameDO) {
                        transfer.setIcon(gameDO.getUrl());
                        transfer.setLotteryName(gameDO.getName());
                    }
                }
                dataMap.put("lotterys", list);
            }
        }
        return BaseDataVO.builder().data(dataMap).build();
    }
}
