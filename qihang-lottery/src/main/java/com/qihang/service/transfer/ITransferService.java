package com.qihang.service.transfer;

import com.qihang.common.vo.BaseVO;
import com.qihang.controller.transferOut.app.dto.TransferDTO;

public interface ITransferService {
    public BaseVO validate(TransferDTO dto);


}
