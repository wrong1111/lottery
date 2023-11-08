package com.qihang.controller.shop.app;


import com.qihang.common.vo.CommonListVO;
import com.qihang.controller.shop.app.dto.ShopDTO;
import com.qihang.controller.shop.app.vo.ShopVO;
import com.qihang.service.shop.IShopService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author bright
 * @since 2022-11-13
 */
@RestController
@RequestMapping("/app/shop")
public class AppShopController {
    @Resource
    private IShopService shopService;

    @PostMapping("/list")
    @ApiOperation("店铺列表接口")
    public CommonListVO<ShopVO> shopList(@RequestBody ShopDTO shop) {
        return shopService.shopList(shop);
    }
}
