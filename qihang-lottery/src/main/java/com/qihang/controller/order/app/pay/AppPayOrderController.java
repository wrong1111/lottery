package com.qihang.controller.order.app.pay;


import com.alipay.api.AlipayApiException;
import com.qihang.common.util.log.LogUtil;
import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.controller.order.app.pay.dto.PayCreateOrderDTO;
import com.qihang.controller.order.app.pay.dto.PayOrderDTO;
import com.qihang.controller.order.app.pay.vo.PayOrderVO;
import com.qihang.service.order.IPayOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * @author bright
 * @since 2022-10-10
 */
@RestController
@RequestMapping("/app/pay/order")
@Api(tags = "app 支付订单接口集合")
public class AppPayOrderController {

    @Resource
    private IPayOrderService payOrderService;

    @Resource
    private ServletRequest request;

    @Resource
    private LogUtil logUtil;

    @PostMapping("/list")
    @ApiOperation("支付订单记录接口")
    public CommonListVO<PayOrderVO> getPayOrderPage(@RequestBody @Valid PayOrderDTO payOrder) {
        return payOrderService.getPayOrderPage(payOrder, Integer.valueOf(request.getAttribute("User-ID").toString()));
    }

    @PostMapping("/create")
    @ApiOperation("创建订单接口")
    public BaseVO create(@RequestBody @Valid PayCreateOrderDTO payCreateOrder) throws AlipayApiException {
        logUtil.record("发起充值,充值金额为【" + payCreateOrder.getPrice() + "】,订单状态未支付");
        return payOrderService.create(payCreateOrder, Integer.valueOf(request.getAttribute("User-ID").toString()));
    }

    @PostMapping("/callback")
    @ApiOperation("支付宝回调接口")
    public String callback(HttpServletRequest request) throws AlipayApiException {
        return payOrderService.callback(request);
    }
}
