package com.qihang.service.ballgame;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.controller.ballgame.admin.vo.AdminBallGameVO;
import com.qihang.controller.ballgame.app.vo.BallGameVO;
import com.qihang.domain.ballgame.BallGameDO;
import com.qihang.mapper.ballgame.BallGameMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author bright
 * @since 2022-10-03
 */
@Service
public class BallGameServiceImpl implements IBallGameService {
    @Resource
    private BallGameMapper ballGameMapper;

    @Override
    public CommonListVO<BallGameVO> list() {
        CommonListVO commonList = new CommonListVO();
        List<BallGameDO> ballGameList = ballGameMapper.selectList(new QueryWrapper<BallGameDO>().lambda().eq(BallGameDO::getLine, "0"));
        List<BallGameVO> list = BeanUtil.copyToList(ballGameList, BallGameVO.class);
        commonList.setVoList(list);
        return commonList;
    }

    @Override
    public CommonListVO<AdminBallGameVO> adminList() {
        CommonListVO<AdminBallGameVO> commonList = new CommonListVO<>();
        List<BallGameDO> gameList = ballGameMapper.selectList(null);
        List<AdminBallGameVO> list = BeanUtil.copyToList(gameList, AdminBallGameVO.class);
        commonList.setVoList(list);
        return commonList;
    }

    @Override
    public BaseVO updateLine(Integer id, String type) {
        BallGameDO ballGame = new BallGameDO();
        ballGame.setId(id);
        ballGame.setLine(type);
        ballGameMapper.updateById(ballGame);
        return new BaseVO();
    }
}
