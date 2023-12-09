package com.qihang.service.beidan;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.controller.beidan.vo.BeiDanSfggVO;
import com.qihang.controller.racingball.app.dto.BallCalculationDTO;
import com.qihang.controller.racingball.app.vo.BallCalculationVO;
import com.qihang.domain.beidan.BeiDanSFGGMatchDO;

public interface IBeidanSfggMatchService extends IService<BeiDanSFGGMatchDO> {

    /**
     * 北单比赛列表
     *
     * @return
     */
    CommonListVO<BeiDanSfggVO> beiDanMatchList();


    /**
     * 计算 组 注 预测金额
     *
     * @param ballCalculation
     * @return
     */
    BallCalculationVO calculation(BallCalculationDTO ballCalculation);

    BaseVO award();
}
