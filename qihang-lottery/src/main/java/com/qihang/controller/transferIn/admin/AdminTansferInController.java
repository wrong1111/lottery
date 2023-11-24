package com.qihang.controller.transferIn.admin;

import com.qihang.annotation.Log;
import com.qihang.annotation.TenantIgnore;
import com.qihang.common.vo.BaseVO;
import com.qihang.constant.TransferEnum;
import com.qihang.controller.transferIn.admin.dto.AdminShopTransferInDTO;
import com.qihang.service.transfer.IShopTransferService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@Api(tags = "后台 - 收单")
@RequestMapping("/admin/transferin")
public class AdminTansferInController {


    @Resource
    IShopTransferService shopTransferService;


    @GetMapping("list")
    @TenantIgnore
    public BaseVO list() {
        return shopTransferService.listShopTransfer(TransferEnum.TransferIn);
    }

    @Log(title = "修改收单设置")
    @TenantIgnore
    @RequestMapping("/edit/{id}")
    public BaseVO updateShopTransferIn(@Valid @RequestBody AdminShopTransferInDTO vo) {
        return shopTransferService.editShopTransfer(vo);
    }

}
