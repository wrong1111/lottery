package com.qihang.service.order;


import com.baomidou.mybatisplus.extension.service.IService;
import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.controller.order.admin.lottery.dto.*;
import com.qihang.controller.order.admin.lottery.vo.LotteryOrderQueryVO;
import com.qihang.controller.order.app.lottery.dto.LotteryOrderDTO;
import com.qihang.controller.order.app.lottery.vo.LotteryOrderVO;
import com.qihang.controller.order.app.lottery.vo.YesterdayCentreOrderVO;
import com.qihang.domain.order.LotteryOrderDO;
import com.qihang.domain.order.LotteryTicketDO;

/**
 * @author bright
 * @since 2022-10-10
 */
public interface ILotteryOrderService extends IService<LotteryOrderDO> {

    /**
     * 根据参数查询购彩订单信息列表
     *
     * @param lotteryOrder
     * @param userId
     * @return
     */
    CommonListVO<LotteryOrderVO> getLotteryOrderPage(LotteryOrderDTO lotteryOrder, Integer userId);


    /**
     * 根据id查询订单信息
     *
     * @param id
     * @param userId
     * @return
     */
    LotteryOrderVO getLotteryOrderById(Integer id, Integer userId);

    /**
     * 后台购彩订单内表
     *
     * @param lotteryOrderQuery
     * @return
     */
    CommonListVO<LotteryOrderQueryVO> getAdminLotteryOrderPage(LotteryOrderQueryDTO lotteryOrderQuery);

    BaseVO sumAdminLotteryOrder(LotteryOrderQueryDTO lotteryOrderQuery);

    /**
     * 出票
     *
     * @param ticketing
     * @return
     */
    BaseVO ticketing(TicketingDTO ticketing);

    /**
     * 派奖
     *
     * @param award
     * @return
     */
    BaseVO award(AwardDTO award);

    /**
     * 退票
     *
     * @param id
     * @return
     */
    BaseVO retreat(Integer id);


    /**
     * 修改票据接口
     *
     * @param actualVoteDTO
     * @return
     */
    BaseVO actualVote(ActualVoteDTO actualVoteDTO);


    /**
     * 获取昨日下单中奖的订单提示信息
     *
     * @return
     */
    CommonListVO<YesterdayCentreOrderVO> centre();


    /**
     * 未出票发送邮件
     *
     * @return
     */
    void NoTicketIssuedSedEmail();


    /**
     * 清理流水
     *
     * @param orderFlowWater
     * @param user
     * @return
     */
    BaseVO clearFlow(OrderFlowWaterDTO orderFlowWater, String user);

    BaseVO retreatTicket(Integer ticketId);

    BaseVO editMultiTicket(Integer ticketId, Integer multi);

    CommonListVO<LotteryTicketDO> ticketByOrderId(String orderId);
}
