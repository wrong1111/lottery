package com.qihang.service.transfer;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qihang.common.vo.BaseVO;
import com.qihang.constant.TransferEnum;
import com.qihang.controller.transferIn.admin.dto.AdminShopTransferInDTO;
import com.qihang.domain.transfer.ShopTransferDO;

public interface IShopTransferService extends IService<ShopTransferDO> {

    public BaseVO editShopTransfer(AdminShopTransferInDTO vo);

    public BaseVO listShopTransfer(TransferEnum transferEnum);
}
