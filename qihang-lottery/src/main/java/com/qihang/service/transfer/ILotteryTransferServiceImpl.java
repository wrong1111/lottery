package com.qihang.service.transfer;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qihang.annotation.TenantIgnore;
import com.qihang.common.vo.BaseVO;
import com.qihang.constant.TransferEnum;
import com.qihang.controller.transferIn.admin.dto.AddLotteryTransferInDTO;
import com.qihang.domain.transfer.LotteryTransferDO;
import com.qihang.mapper.transfer.LotteryTransferMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;


@Slf4j
@Service
public class ILotteryTransferServiceImpl extends ServiceImpl<LotteryTransferMapper, LotteryTransferDO> implements ILotteryTransferService {

    @Resource
    LotteryTransferMapper lotteryTransferMapper;

    @TenantIgnore
    public BaseVO addLotteryTransferIn(AddLotteryTransferInDTO dto) {
        LotteryTransferDO lotteryTransferDO = getOne(new QueryWrapper<LotteryTransferDO>().lambda().eq(LotteryTransferDO::getLotteryType, dto.getLotteryType()).eq(LotteryTransferDO::getTransferFlag, TransferEnum.TransferIn.code));
        if (ObjectUtil.isNull(lotteryTransferDO)) {
            lotteryTransferDO = new LotteryTransferDO();
            lotteryTransferDO.setTransferFlag(TransferEnum.TransferIn.code);
            lotteryTransferDO.setTransferBeforeTime(dto.getBeforeTime());
            lotteryTransferDO.setStates(dto.getState());
            lotteryTransferDO.setCreateTime(new Date());
            lotteryTransferDO.setShopId(dto.getShopId());
            lotteryTransferDO.setLotteryType(dto.getLotteryType());
            lotteryTransferDO.setCommiss(dto.getCommiss());
            lotteryTransferMapper.insert(lotteryTransferDO);
        } else {
            lotteryTransferDO.setTransferBeforeTime(dto.getBeforeTime());
            lotteryTransferDO.setStates(dto.getState());
            lotteryTransferDO.setUpdateTime(new Date());
            lotteryTransferDO.setLotteryType(dto.getLotteryType());
            lotteryTransferDO.setCommiss(dto.getCommiss());
            lotteryTransferMapper.updateById(lotteryTransferDO);
        }
        return new BaseVO();
    }

    @TenantIgnore
    @Override
    public BaseVO editLotteryState(Integer id, Integer state) {
        LotteryTransferDO lotteryTransferDO = lotteryTransferMapper.selectById(id);
        lotteryTransferDO.setStates(state);
        lotteryTransferMapper.updateById(lotteryTransferDO);
        return new BaseVO();
    }

    @TenantIgnore
    @Override
    public BaseVO editLotteryCommiss(Integer id, BigDecimal commiss) {
        LotteryTransferDO lotteryTransferDO = lotteryTransferMapper.selectById(id);
        lotteryTransferDO.setCommiss(commiss);
        lotteryTransferMapper.updateById(lotteryTransferDO);
        return new BaseVO();
    }
}
