package com.qihang.service.permutation;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.controller.permutation.app.dto.PlaceOrderDTO;
import com.qihang.controller.permutation.app.vo.PermutationRecordVO;
import com.qihang.domain.order.LotteryOrderDO;
import com.qihang.domain.permutation.PermutationAwardDO;
import com.qihang.domain.permutation.PermutationDO;

import java.util.List;

/**
 * @author bright
 * @since 2022-10-10
 */
public interface IPermutationService extends IService<PermutationDO> {

    /**
     * 排序 下单接口
     *
     * @param placeList
     * @param userId
     * @param userId
     * @param type
     * @return
     */
    BaseVO placeOrder(List<PlaceOrderDTO> placeList, Integer userId, String type, String issueNo);


    /**
     * 出奖记录
     *
     * @param type
     * @return
     */
    CommonListVO<PermutationRecordVO> record(String type);


    /**
     * 计算用户中奖结果
     * 数字彩兑奖入口
     *
     * @param permutationAward
     * @return
     */
    BaseVO calculation(PermutationAwardDO permutationAward);

    /**
     * 计算用户中奖结果
     * 数字彩兑奖入口
     *
     * @param type
     * @return
     */
    BaseVO calculation(String type);

    BaseVO calculation(LotteryOrderDO orderDO);

    /**
     * 计算用户中奖结果
     * 数字彩兑奖入口
     *
     * @param type
     * @return
     */
    public BaseVO calculationBySchemeDetail(String type);

    /**
     * 计算用户中奖结果
     * 数字彩兑奖入口
     *
     * @param orders
     * @return
     */
    public BaseVO calculationBySchemeDetail(LotteryOrderDO orders, PermutationAwardDO permutationAwardDO);
}
