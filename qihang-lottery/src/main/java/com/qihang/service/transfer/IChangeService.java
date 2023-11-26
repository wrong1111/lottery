package com.qihang.service.transfer;

import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.controller.transferIn.admin.dto.LotteryAutoStateDTO;
import com.qihang.controller.transferIn.admin.dto.LotteryOutDTO;
import com.qihang.controller.transferIn.admin.vo.ShopOutVO;

/**
 * @Author: wyong
 * @Date: 2023/11/25 025 21:05
 * @Description:
 **/
public interface IChangeService {

    public BaseVO info(String url);

    public BaseVO list(LotteryOutDTO dto);

    public CommonListVO<ShopOutVO> listShop();

    public BaseVO editAutoState(LotteryAutoStateDTO autoStateDTO);
}
