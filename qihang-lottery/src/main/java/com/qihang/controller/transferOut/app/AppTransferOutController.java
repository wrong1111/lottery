package com.qihang.controller.transferOut.app;


import com.qihang.common.vo.BaseDataVO;
import com.qihang.common.vo.BaseVO;
import com.qihang.controller.transferOut.app.dto.TransferDTO;
import com.qihang.service.transfer.IShopTransferService;
import com.qihang.service.transfer.ITransferService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/app/transfer")
public class AppTransferOutController {
    @Resource
    IShopTransferService shopTransferService;

    @Resource
    ITransferService transferService;

    /**
     * * getTransferInfo 获取转单信息（上游提供的返点。彩种，店铺名称，店铺ID,店铺LOGO，店铺联系人）
     * * createOrder 创建订单
     * * ping 测试接口
     *
     * @param dto
     * @return
     */
    @GetMapping("create")
    public BaseVO list(@Valid @RequestBody TransferDTO dto) {
        BaseVO baseVO = transferService.validate(dto);
        if (!baseVO.getSuccess()) {
            return baseVO;
        }
        switch (dto.getAction()) {
            case "getTransferInfo":
                BaseDataVO baseDataVO = BaseDataVO.builder().data("{}").build();
                return baseDataVO;
            case "createOrder":
                baseVO.setSuccess(false);
                baseVO.setErrorMsg("暂未实现");
                break;
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
