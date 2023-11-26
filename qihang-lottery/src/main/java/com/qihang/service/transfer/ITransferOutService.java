package com.qihang.service.transfer;

import com.qihang.common.vo.BaseVO;
import com.qihang.domain.order.LotteryOrderDO;
import com.qihang.domain.permutation.PermutationDO;
import com.qihang.domain.racingball.RacingBallDO;

import java.util.List;

/**
 * @Author: wyong
 * @Date: 2023/11/25 025 19:35
 * @Description:
 **/
public interface ITransferOutService {

    /***
     * 上游开放的彩种以及返点信息，截止信息
     * @author wyong
     * @date 2023/11/25 025
     * @param key
     * @return com.qihang.common.vo.BaseVO
     */
    public BaseVO listLottery(String key);

    public BaseVO getAccountMoney(String key);

    public BaseVO createOrder(String data, String key);

    public BaseVO createSportOrder(LotteryOrderDO lotteryOrderDO, List<RacingBallDO> racingBallDOList, String key);

    public BaseVO createDigitOrder(LotteryOrderDO lotteryOrderDO, List<PermutationDO> permutationDOS, String key);
}
