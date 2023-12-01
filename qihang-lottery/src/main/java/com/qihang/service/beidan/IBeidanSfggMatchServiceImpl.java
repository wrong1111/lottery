package com.qihang.service.beidan;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qihang.common.util.reward.BeiDanUtil;
import com.qihang.common.vo.CommonListVO;
import com.qihang.controller.beidan.vo.BeiDanMatchVO;
import com.qihang.controller.beidan.vo.BeiDanSfggVO;
import com.qihang.controller.beidan.vo.BeiDanVO;
import com.qihang.controller.beidan.vo.BeidanSfggMatchVO;
import com.qihang.controller.racingball.app.dto.BallCalculationDTO;
import com.qihang.controller.racingball.app.vo.BallCalculationVO;
import com.qihang.domain.beidan.BeiDanMatchDO;
import com.qihang.domain.beidan.BeiDanSFGGMatchDO;
import com.qihang.enumeration.ball.BettingStateEnum;
import com.qihang.mapper.beidan.BeiDanSfggMatchMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IBeidanSfggMatchServiceImpl extends ServiceImpl<BeiDanSfggMatchMapper, BeiDanSFGGMatchDO> implements IBeidanSfggMatchService {
    @Resource
    BeiDanSfggMatchMapper beiDanSfggMatchMapper;

    @Override
    public CommonListVO<BeiDanSfggVO> beiDanMatchList() {
        long c = System.currentTimeMillis();
        CommonListVO<BeiDanSfggVO> commonList = new CommonListVO<>();
        List<BeiDanSfggVO> beiDanList = new ArrayList<>();
        //小于当前时间 不展示
        List<BeiDanSFGGMatchDO> beiDanMatchDataList = beiDanSfggMatchMapper.selectList(new QueryWrapper<BeiDanSFGGMatchDO>().lambda().eq(BeiDanSFGGMatchDO::getState, BettingStateEnum.YES.getKey()).gt(BeiDanSFGGMatchDO::getDeadline, new Date()));
        Map<String, List<BeiDanSFGGMatchDO>> map = beiDanMatchDataList.stream().collect(Collectors.groupingBy(BeiDanSFGGMatchDO::getStartTime));
        //对map的key进行排序
        map = map.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        Integer id = 0;
        for (Map.Entry<String, List<BeiDanSFGGMatchDO>> entry : map.entrySet()) {
            BeiDanSfggVO beiDan = new BeiDanSfggVO();
            beiDan.setStartTime(entry.getKey());
            beiDan.setCount(entry.getValue().size());
            List<BeidanSfggMatchVO> beiDanMatchList = new ArrayList<>();
            //排序
            List<BeiDanSFGGMatchDO> list = entry.getValue();
            list = list.stream().sorted(Comparator.comparing(data -> Integer.valueOf(data.getNumber()))).collect(Collectors.toList());
            for (BeiDanSFGGMatchDO beiDanMatch : list) {
                BeidanSfggMatchVO beiDanMatchVO = new BeidanSfggMatchVO();
                BeanUtils.copyProperties(beiDanMatch, beiDanMatchVO);
                //默认选择项为0，方便前端好控制选择了几项
                beiDanMatchVO.setChoiceCount(0);
                List<Map<String, Object>> mapList = new ArrayList<>();
                Map<String, Object> mapObj = null;

                /*============================让球组成list<map>结构==============================*/
                //主胜
                mapObj = new HashMap<>(5);
                mapObj.put("id", ++id);
                mapObj.put("odds", beiDanMatch.getHostWinOdds());
                mapObj.put("active", false);
                mapObj.put("describe", "胜");
                mapObj.put("index", 0);
                mapList.add(mapObj);

                //客胜
                mapObj = new HashMap<>(5);
                mapObj.put("id", ++id);
                mapObj.put("odds", beiDanMatch.getVisitWinOdds());
                mapObj.put("active", false);
                mapObj.put("describe", "负");
                mapObj.put("index", 1);
                mapList.add(mapObj);
                //添加对对象中
                beiDanMatchVO.setLetOddsList(mapList);
                beiDanMatchList.add(beiDanMatchVO);
            }
            beiDan.setBeiDanMatchList(beiDanMatchList);
            beiDanList.add(beiDan);
        }
        log.info("=======>[北单胜负过关]=======cost {}", (System.currentTimeMillis() - c));
        commonList.setVoList(beiDanList);
        return commonList;
    }

    @Override
    public BallCalculationVO calculation(BallCalculationDTO ballCalculation) {
        return BeiDanUtil.calculationSfgg(ballCalculation.getBeiDanMatchList(), ballCalculation.getMultiple(), ballCalculation.getPssTypeList());
    }
}
