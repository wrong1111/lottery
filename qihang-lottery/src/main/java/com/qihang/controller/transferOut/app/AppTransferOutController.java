package com.qihang.controller.transferOut.app;


import com.qihang.common.vo.BaseVO;
import com.qihang.controller.transferOut.app.dto.TransferDTO;
import com.qihang.service.transfer.ITransferOutService;
import com.qihang.service.transfer.ITransferService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/app/transfer")
public class AppTransferOutController {
    @Resource
    ITransferOutService transferOutService;

    @Resource
    ITransferService transferService;

    @PostMapping("/info/{key}")
    public BaseVO info(@PathVariable String key) {
        return transferOutService.listLottery(key);
    }

    /**
     * * getTransferInfo 获取转单信息（上游提供的返点。彩种，店铺名称，店铺ID,店铺LOGO，店铺联系人）
     * * createOrder 创建订单
     * * ping 测试接口
     * getAccountMoney
     *
     * @param dto
     * @return
     */
    @PostMapping("create")
    public BaseVO list(@Valid @RequestBody TransferDTO dto) {
        BaseVO baseVO = transferService.validate(dto);
        if (!baseVO.getSuccess()) {
            return baseVO;
        }
        switch (dto.getAction()) {
            case "getTransferInfo":
                return transferOutService.listLottery(dto.getKey());
            case "getAccountMoney":
                return transferOutService.getAccountMoney(dto.getKey());
            case "createOrder":
                return transferOutService.createOrder(dto.getData(), dto.getKey());
            case "changeState":
                return transferOutService.getChangeState(dto.getData(), dto.getKey());
            case "ping":
                baseVO.setSuccess(true);
                baseVO.setErrorMsg("ok");
                break;
            default:
                baseVO.setSuccess(false);
                baseVO.setErrorMsg("不支持此请求");
                break;
        }
        return baseVO;
    }
}
