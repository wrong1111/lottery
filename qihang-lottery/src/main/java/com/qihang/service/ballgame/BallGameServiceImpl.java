package com.qihang.service.ballgame;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qihang.annotation.TenantIgnore;
import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.constant.TransferEnum;
import com.qihang.controller.ballgame.admin.vo.AdminBallGameVO;
import com.qihang.controller.ballgame.app.vo.BallGameVO;
import com.qihang.domain.ballgame.BallGameDO;
import com.qihang.domain.transfer.LotteryTransferDO;
import com.qihang.mapper.ballgame.BallGameMapper;
import com.qihang.mapper.transfer.LotteryTransferMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author bright
 * @since 2022-10-03
 */
@Service
public class BallGameServiceImpl implements IBallGameService {
    @Resource
    private BallGameMapper ballGameMapper;

    @Resource
    LotteryTransferMapper lotteryTransferMapper;

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

    @TenantIgnore
    @Override
    public CommonListVO<BallGameVO> listNoOpenTransfer() {
        List<BallGameDO> gameDOS = ballGameMapper.selectList(new QueryWrapper<BallGameDO>().lambda().eq(BallGameDO::getTenantId, 1));
        List<BallGameVO> gameVOS = BeanUtil.copyToList(gameDOS, BallGameVO.class);
        List<LotteryTransferDO> transferDOS = lotteryTransferMapper.selectList(new QueryWrapper<LotteryTransferDO>().lambda().eq(LotteryTransferDO::getTransferFlag, TransferEnum.TransferIn.code));
        Map<Integer, Integer> transferMap = transferDOS.stream().collect(Collectors.toMap(LotteryTransferDO::getLotteryType, LotteryTransferDO::getLotteryType, (a, b) -> a));
        gameVOS.stream().forEach(p -> {
            if (transferMap.get(p.getLotid()) != null) {
                p.setSelected(true);
            }
        });
        CommonListVO<BallGameVO> gameVOCommonListVO = new CommonListVO();
        gameVOCommonListVO.setVoList(gameVOS);
        gameVOCommonListVO.setTotal((long) gameVOS.size());
        return gameVOCommonListVO;
    }
}
