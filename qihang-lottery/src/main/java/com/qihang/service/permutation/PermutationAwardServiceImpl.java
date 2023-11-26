package com.qihang.service.permutation;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qihang.common.util.PermutationUtils;
import com.qihang.controller.permutation.app.vo.IssueNoVO;
import com.qihang.domain.permutation.PermutationAwardDO;
import com.qihang.enumeration.order.lottery.LotteryOrderTypeEnum;
import com.qihang.mapper.permutation.PermutationAwardMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author bright
 * @since 2022-10-10
 */
@Slf4j
@Service
public class PermutationAwardServiceImpl extends ServiceImpl<PermutationAwardMapper, PermutationAwardDO> implements IPermutationAwardService {

    @Resource
    private PermutationAwardMapper permutationAwardMapper;

    @Override
    public IssueNoVO getIssueNo(String type) {
        IssueNoVO issueNo = new IssueNoVO();
        //查询最新的一期数据，
        PermutationAwardDO permutationAward = permutationAwardMapper.selectOne(new QueryWrapper<PermutationAwardDO>().lambda().eq(PermutationAwardDO::getType, type).gt(PermutationAwardDO::getDeadTime, new Date()).orderByAsc(PermutationAwardDO::getDeadTime).last("limit 1"));
        if (null == permutationAward) {
            //最后一期的旧期 号
            permutationAward = permutationAwardMapper.selectOne(new QueryWrapper<PermutationAwardDO>().lambda().eq(PermutationAwardDO::getType, type).gt(PermutationAwardDO::getDeadTime, DateUtils.addMonths(new Date(), -1)).orderByDesc(PermutationAwardDO::getId).last("limit 1"));
            if (null == permutationAward) {
                //重新生成新的
                log.error("=================================================");
                log.error("======彩种[{}]  没有基础数据，请先执行爬虫任务=========", LotteryOrderTypeEnum.valueOFS(type).getValue());
                log.error("=================================================");
                throw new RuntimeException("没有期号基础数据，请先执行爬虫任务");
            }
            //生成新期号
            permutationAward = PermutationUtils.next(permutationAward);
            if (null != permutationAward) {
                permutationAwardMapper.insert(permutationAward);
            }
        }
        BeanUtils.copyProperties(permutationAward, issueNo);
        return issueNo;
    }

    @Override
    public IssueNoVO getLastIssueNo(String type) {
        PermutationAwardDO permutationAward = permutationAwardMapper.selectOne(new QueryWrapper<PermutationAwardDO>().lambda().eq(PermutationAwardDO::getType, type).gt(PermutationAwardDO::getDeadTime, new Date()).orderByAsc(PermutationAwardDO::getDeadTime).last("limit 1"));
        if (null == permutationAward) {
            //最后一期的旧期 号
            permutationAward = permutationAwardMapper.selectOne(new QueryWrapper<PermutationAwardDO>().lambda().eq(PermutationAwardDO::getType, type).gt(PermutationAwardDO::getDeadTime, DateUtils.addMonths(new Date(), -1)).orderByDesc(PermutationAwardDO::getId).last("limit 1"));
            if (null == permutationAward) {
                return null;
            }
        }
        IssueNoVO issueNo = new IssueNoVO();
        BeanUtils.copyProperties(permutationAward, issueNo);
        return issueNo;
    }


}
