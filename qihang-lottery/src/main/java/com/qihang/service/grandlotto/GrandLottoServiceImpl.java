package com.qihang.service.grandlotto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qihang.common.util.reward.GrandLottoUtil;
import com.qihang.controller.grandlotto.dto.GrandLottoDTO;
import com.qihang.controller.grandlotto.dto.GrandLottoObjDTO;
import com.qihang.controller.grandlotto.vo.GrandLottoVO;
import com.qihang.controller.permutation.app.vo.PermutationVO;
import com.qihang.domain.permutation.PermutationAwardDO;
import com.qihang.enumeration.order.lottery.LotteryOrderTypeEnum;
import com.qihang.mapper.permutation.PermutationAwardMapper;
import com.qihang.mapper.permutation.PermutationMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author: bright
 * @description:
 * @time: 2023-04-05 11:35
 */
@Service
public class GrandLottoServiceImpl implements IGrandLottoService {

    @Resource
    private PermutationAwardMapper permutationAwardMapper;

    /*
      默认 大乐透 8
      新增 双色球 24
      七乐彩 22
     */
    @Override
    public GrandLottoVO calculation(GrandLottoDTO grandLotto) {
        GrandLottoVO grandLottoVO = new GrandLottoVO();
        if ("8".equals(grandLotto.getType())) {
            String type = grandLotto.getType();
            PermutationAwardDO permutationAward = permutationAwardMapper.selectOne(new QueryWrapper<PermutationAwardDO>().lambda().eq(PermutationAwardDO::getType, type).orderByDesc(PermutationAwardDO::getCreateTime).last("limit 1"));

            //获取对应的下单号码
            List<String> list = GrandLottoUtil.calculation(grandLotto.getRedList(), grandLotto.getBlueList());

            grandLottoVO.setNotes(list.size());
            List<PermutationVO> permutationList = new ArrayList<>();
            for (String s : list) {
                PermutationVO permutationVO = new PermutationVO();
                permutationVO.setContent(s);
                permutationVO.setMode(LotteryOrderTypeEnum.GRAND_LOTTO.getValue());
                permutationVO.setStageNumber(permutationAward.getStageNumber() + 1);
                permutationList.add(permutationVO);
            }
            grandLottoVO.setPermutationList(permutationList);
            return grandLottoVO;
        } else if ("24".equals(grandLotto.getType())) {
            PermutationAwardDO permutationAward = permutationAwardMapper.selectOne(new QueryWrapper<PermutationAwardDO>().lambda().eq(PermutationAwardDO::getType, LotteryOrderTypeEnum.FCSSQ.getKey()).orderByDesc(PermutationAwardDO::getCreateTime).last("limit 1"));
            //获取对应的下单号码
            List<String> list = GrandLottoUtil.calculationSsq(grandLotto.getRedList(), grandLotto.getBlueList());

            grandLottoVO.setNotes(list.size());
            List<PermutationVO> permutationList = new ArrayList<>();
            for (String s : list) {
                PermutationVO permutationVO = new PermutationVO();
                permutationVO.setContent(s);
                permutationVO.setMode(LotteryOrderTypeEnum.FCSSQ.getValue());
                permutationVO.setStageNumber(permutationAward.getStageNumber() + 1);
                permutationList.add(permutationVO);
            }
            grandLottoVO.setPermutationList(permutationList);
            return grandLottoVO;
        } else if ("22".equals(grandLotto.getType())) {
            PermutationAwardDO permutationAward = permutationAwardMapper.selectOne(new QueryWrapper<PermutationAwardDO>().lambda().eq(PermutationAwardDO::getType, LotteryOrderTypeEnum.FCSSQ.getKey()).orderByDesc(PermutationAwardDO::getCreateTime).last("limit 1"));
            //获取对应的下单号码
            List<String> list = GrandLottoUtil.calculationQLC(grandLotto.getRedList());
            grandLottoVO.setNotes(list.size());
            List<PermutationVO> permutationList = new ArrayList<>();
            for (String s : list) {
                PermutationVO permutationVO = new PermutationVO();
                permutationVO.setContent(s);
                permutationVO.setMode(LotteryOrderTypeEnum.FCQLC.getValue());
                permutationVO.setStageNumber(permutationAward.getStageNumber() + 1);
                permutationList.add(permutationVO);
            }
            grandLottoVO.setPermutationList(permutationList);
            return grandLottoVO;
        } else if ("23".equals(grandLotto.getType())) {
            PermutationAwardDO permutationAward = permutationAwardMapper.selectOne(new QueryWrapper<PermutationAwardDO>().lambda().eq(PermutationAwardDO::getType, LotteryOrderTypeEnum.FCSSQ.getKey()).orderByDesc(PermutationAwardDO::getCreateTime).last("limit 1"));
            //获取对应的下单号码
            List<String> list = GrandLottoUtil.calculationKL8(grandLotto.getRedList(), grandLotto.getType());
            grandLottoVO.setNotes(list.size());
            List<PermutationVO> permutationList = new ArrayList<>();
            for (String s : list) {
                PermutationVO permutationVO = new PermutationVO();
                permutationVO.setContent(s);
                permutationVO.setMode(LotteryOrderTypeEnum.FCQLC.getValue());
                permutationVO.setStageNumber(permutationAward.getStageNumber() + 1);
                permutationList.add(permutationVO);
            }
            grandLottoVO.setPermutationList(permutationList);
            return grandLottoVO;
        }
        return grandLottoVO;
    }
}
