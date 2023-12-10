package com.qihang.controller.order.admin.lottery;


import com.qihang.annotation.Log;
import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.controller.order.admin.lottery.dto.*;
import com.qihang.controller.order.admin.lottery.vo.LotteryOrderQueryVO;
import com.qihang.domain.order.LotteryTicketDO;
import com.qihang.service.order.ILotteryOrderService;
import com.qihang.service.transfer.IChangeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.checkerframework.framework.qual.HasQualifierParameter;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * @author bright
 * @since 2022-10-10
 */
@RestController
@RequestMapping("/admin/lottery/order")
@Api(tags = "admin 彩票订单接口集合")
public class AdminLotteryOrderController {

    @Resource
    private ILotteryOrderService lotteryOrderService;

    @Resource
    private HttpServletRequest request;

    @Resource
    IChangeService changeService;


    @PostMapping("/list")
    @ApiOperation("购彩订单记录接口")
    public CommonListVO<LotteryOrderQueryVO> getLotteryOrderPage(@RequestBody @Valid LotteryOrderQueryDTO lotteryOrderQuery) {
        return lotteryOrderService.getAdminLotteryOrderPage(lotteryOrderQuery);
    }

    @PostMapping("/sum")
    @ApiOperation("统计购彩订单记录接口")
    public BaseVO sumLotteryOrder(@RequestBody @Valid LotteryOrderQueryDTO lotteryOrderQuery) {
        return lotteryOrderService.sumAdminLotteryOrder(lotteryOrderQuery);
    }

    @Log(title = "出票审核")
    @PutMapping("/ticketing")
    @ApiOperation("出票审核接口")
    public BaseVO ticketing(@RequestBody @Valid TicketingDTO ticketing) {
        return lotteryOrderService.ticketing(ticketing);
    }


    @Log(title = "派奖")
    @PutMapping("/award")
    @ApiOperation("派奖接口")
    public BaseVO award(@RequestBody @Valid AwardDTO award) {
        return lotteryOrderService.award(award);
    }


    @Log(title = "退票")
    @PutMapping("/retreat/{id}")
    @ApiOperation("退票接口")
    public BaseVO retreat(@PathVariable("id") Integer id) {
        return lotteryOrderService.retreat(id);
    }


    @Log(title = "修改票据")
    @PutMapping("/actual")
    @ApiOperation("修改票据接口")
    public BaseVO actualVote(@RequestBody @Valid ActualVoteDTO actualVoteDTO) {
        return lotteryOrderService.actualVote(actualVoteDTO);
    }

    @Log(title = "清理流水")
    @PostMapping("/clear")
    @ApiOperation("清理流水接口")
    public BaseVO clearFlow(@RequestBody @Valid OrderFlowWaterDTO orderFlowWater) {
        return lotteryOrderService.clearFlow(orderFlowWater, request.getHeader("x-user"));
    }


    @Log(title = "转单")
    @PostMapping("/change/{id}")
    @ApiOperation("转单 接口")
    public BaseVO change(@PathVariable("id") Integer id) {
        // return lotteryOrderService.clearFlow(orderFlowWater, request.getHeader("x-user"));
        return changeService.send(id, false);
    }

    @Log(title = "同步转单出票状态")
    @PostMapping("/changeState")
    @ApiOperation("同步转单出票状态 接口")
    public BaseVO changeState(@RequestBody LotteryOrderQueryVO orderQueryVO) {
        return changeService.chageState(orderQueryVO.getId());
    }

    @Log(title = "退某张票")
    @PostMapping("/retreatTicket/{ticketId}")
    @ApiOperation("退某张票 接口")
    public BaseVO retreatTicket(@PathVariable("ticketId") Integer ticketId) {
        return lotteryOrderService.retreatTicket(ticketId);
    }

    @Log(title = "调整倍数")
    @PostMapping("/editTicket/{ticketId}/{multi}")
    @ApiOperation("退某张票 接口")
    public BaseVO editMultiTicket(@PathVariable("multi") Integer multi, @PathVariable("ticketId") Integer ticketId) {
        return lotteryOrderService.editMultiTicket(ticketId, multi);
    }

    @GetMapping("/ticket/{orderId}")
    @ApiOperation("票据列表 接口")
    public CommonListVO<LotteryTicketDO> ticketByOrderId(@PathVariable String orderId) {
        return lotteryOrderService.ticketByOrderId(orderId);
    }

    @Log(title = "开奖")
    @PostMapping("/award/{id}")
    @ApiOperation("单个开奖 接口")
    public BaseVO award(@PathVariable("id") Integer id) {
        return lotteryOrderService.openAward(id);
    }

    @Log(title = "开奖")
    @PostMapping("/award/batch")
    @ApiOperation("开奖 接口")
    public BaseVO awardBatch() {
        return lotteryOrderService.openAward(null);
    }

}
