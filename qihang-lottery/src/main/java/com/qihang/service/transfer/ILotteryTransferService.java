package com.qihang.service.transfer;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qihang.common.vo.BaseVO;
import com.qihang.controller.transferIn.admin.dto.AddLotteryTransferInDTO;
import com.qihang.domain.transfer.LotteryTransferDO;

import java.math.BigDecimal;

public interface ILotteryTransferService extends IService<LotteryTransferDO> {

    public BaseVO addLotteryTransferIn(AddLotteryTransferInDTO dto);

    public BaseVO editLotteryState(Integer id, Integer state);

    public BaseVO editLotteryCommiss(Integer id, BigDecimal commiss);

}
