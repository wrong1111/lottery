package com.qihang.controller.shop.admin;


import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.controller.shop.admin.dto.AddShopDTO;
import com.qihang.controller.shop.admin.dto.AdminShopDTO;
import com.qihang.controller.shop.admin.dto.ShopRechargeDTO;
import com.qihang.controller.shop.admin.vo.AdminShopVO;
import com.qihang.service.shop.IShopService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;

/**
 * @author bright
 * @since 2022-11-13
 */
@RestController
@RequestMapping("/admin/shop")
public class AdminShopController {
    @Resource
    private IShopService shopService;

    @Resource
    private ServletRequest request;

    @PostMapping("/list")
    @ApiOperation("店铺列表接口")
    public CommonListVO<AdminShopVO> shopList(@RequestBody AdminShopDTO adminShop) {
        return shopService.adminShopList(adminShop);
    }

    @PutMapping("/update/line/{id}/{type}")
    @ApiOperation("福彩上下架接口")
    public BaseVO updateLine(@PathVariable("id") Integer id, @PathVariable("type") String type) {
        return shopService.updateLine(id, type);
    }

    @PostMapping("/add")
    @ApiOperation("添加店铺接口")
    public BaseVO add(@RequestBody AddShopDTO addShop) {
        return shopService.add(addShop, Integer.valueOf(request.getAttribute("User-ID").toString()));
    }

    @PostMapping("/recharge")
    @ApiOperation("店铺充值")
    public BaseVO recharge(@RequestBody ShopRechargeDTO shopRecharge) {
        return shopService.recharge(shopRecharge);
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除店铺")
    public BaseVO delete(@PathVariable("id") Integer id) {
        return shopService.delete(id);
    }
}
