package com.qihang.service.order;

import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.controller.order.admin.pay.dto.PayOrderQueryDTO;
import com.qihang.controller.order.admin.pay.vo.PayOrderQueryVO;
import com.qihang.controller.order.app.pay.dto.PayCreateOrderDTO;
import com.qihang.controller.order.app.pay.dto.PayOrderDTO;
import com.qihang.controller.order.app.pay.vo.PayOrderVO;
import com.qihang.domain.order.PayOrderDO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author bright
 * @since 2022-10-13
 */
public interface IPayOrderService extends IService<PayOrderDO> {
    /**
     * 分页获取当前用户的支付订单
     *
     * @param payOrderDTO
     * @param userId
     * @return
     */
    CommonListVO<PayOrderVO> getPayOrderPage(PayOrderDTO payOrderDTO, Integer userId);

    /**
     * 创建订单
     *
     * @param payCreateOrder
     * @param userId
     * @return
     * @throws AlipayApiException
     */
    BaseVO create(PayCreateOrderDTO payCreateOrder, Integer userId) throws AlipayApiException;


    /**
     * 支付宝回调
     *
     * @param request
     * @return
     * @throws AlipayApiException
     */
    String callback(HttpServletRequest request) throws AlipayApiException;


    /**
     * 分页获取所有的支付订单
     *
     * @param payOrderQuery
     * @return
     */
    CommonListVO<PayOrderQueryVO> getAdminPayOrderPage(PayOrderQueryDTO payOrderQuery);
}
