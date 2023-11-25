package com.qihang.controller.transferIn.admin;

import cn.hutool.crypto.digest.MD5;
import com.qihang.annotation.Log;
import com.qihang.annotation.TenantIgnore;
import com.qihang.common.vo.BaseVO;
import com.qihang.constant.TransferEnum;
import com.qihang.controller.transferIn.admin.dto.AdminPlatDTO;
import com.qihang.controller.transferIn.admin.dto.AdminShopTransferInDTO;
import com.qihang.service.transfer.IShopTransferService;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

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
        return shopTransferService.listLotterTransfer(TransferEnum.TransferIn);
    }

    @GetMapping("/plat")
    @TenantIgnore
    public BaseVO listShopTransfer() {
        return shopTransferService.listShopTransfer(TransferEnum.TransferIn);
    }

    @Log(title = "修改收单设置")
    @TenantIgnore
    @RequestMapping("/plat/edit")
    public BaseVO updateShopTransferIn(@Valid @RequestBody AdminShopTransferInDTO vo) {
        return shopTransferService.editShopTransfer(vo);
    }

    @Log(title = "修改下游商家开通状态")
    @TenantIgnore
    @PostMapping("/plat/editState/{id}")
    public BaseVO platEditState(@RequestParam String state, @PathVariable Integer id) {
        AdminShopTransferInDTO vo = new AdminShopTransferInDTO();
        vo.setId(id);
        vo.setInterfaceState(state);
        return shopTransferService.editShopTransfer(vo);
    }

    @Log(title = "重置秘钥")
    @TenantIgnore
    @PostMapping("/plat/reset/{id}")
    public BaseVO resetSecurty(@RequestParam(defaultValue = "") String security, @PathVariable Integer id) {
        AdminShopTransferInDTO vo = new AdminShopTransferInDTO();
        vo.setId(id);
        if (StringUtils.isBlank(security)) {
            vo.setTransferSecurty(RandomStringUtils.randomAlphanumeric(32));
        } else {
            vo.setTransferSecurty(security);
        }
        return shopTransferService.editShopTransfer(vo);
    }
}
