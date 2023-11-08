package com.qihang.service.ballgame;

import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.controller.ballgame.admin.vo.AdminBallGameVO;
import com.qihang.controller.ballgame.app.vo.BallGameVO;

/**
 * @author bright
 * @since 2022-10-03
 */
public interface IBallGameService {
    /**
     * 福彩列表
     *
     * @return
     */
    CommonListVO<BallGameVO> list();

    /**
     * 后台数据
     *
     * @return
     */
    CommonListVO<AdminBallGameVO> adminList();


    /**
     * 上下架
     *
     * @param id
     * @param type
     * @return
     */
    BaseVO updateLine(Integer id, String type);
}
