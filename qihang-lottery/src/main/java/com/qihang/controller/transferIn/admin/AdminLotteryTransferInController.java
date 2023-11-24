package com.qihang.controller.transferIn.admin;


import com.qihang.annotation.Log;
import com.qihang.common.vo.BaseVO;
import com.qihang.controller.transferIn.admin.dto.AddLotteryTransferInDTO;
import com.qihang.controller.transferIn.admin.vo.AdminShopTransferInVO;
import com.qihang.service.transfer.ILotteryTransferService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;

@RestController
@RequestMapping("/admin/lottery/transferin")
public class AdminLotteryTransferInController {

    @Resource
    ILotteryTransferService lotteryTransferService;

    @Log(title = "添加/修改彩种收单设置")
    @PostMapping("add")
    public BaseVO addLotteryTransfer(@Valid @RequestBody AddLotteryTransferInDTO dto) {
        return lotteryTransferService.addLotteryTransferIn(dto);
    }

    @Log(title = "修改彩种状态")
    @PostMapping("editState")
    public BaseVO editState(@RequestBody AdminShopTransferInVO vo) {
        return lotteryTransferService.editLotteryState(vo.getId(), vo.getStates());
    }

    @Log(title = "修改彩种返点")
    @PostMapping("editCommiss")
    public BaseVO editCommiss(@RequestParam Integer id, @RequestParam BigDecimal commiss) {
        return lotteryTransferService.editLotteryCommiss(id, commiss);
    }
}
