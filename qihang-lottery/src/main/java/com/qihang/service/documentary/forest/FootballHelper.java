package com.qihang.service.documentary.forest;

import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONUtil;
import com.qihang.controller.football.dto.FootballMatchDTO;
import com.qihang.controller.racingball.app.dto.BallCalculationDTO;
import com.qihang.controller.racingball.app.vo.BallCalculationVO;
import com.qihang.service.documentary.DocumentaryServiceHelper;
import com.qihang.service.football.IFootballMatchService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class FootballHelper extends DocumentaryServiceHelper {

    @Resource
    private IFootballMatchService footballMatchService;

    @Override
    public BigDecimal calculationForest(List<String> contentList, String type, int notes, int multiple, String passway) {
        BallCalculationDTO ballCalculation = new BallCalculationDTO();
        ballCalculation.setType(type);
        ballCalculation.setNotes(notes);
        ballCalculation.setMultiple(multiple);
        ballCalculation.setPssTypeList(Convert.toList(Integer.class, passway));
        List<FootballMatchDTO> footballMatchList = new ArrayList<>();
        for (String s : contentList) {
            footballMatchList.add(JSONUtil.toBean(s, FootballMatchDTO.class));
        }
        ballCalculation.setFootballMatchList(footballMatchList);
        BallCalculationVO calculation = footballMatchService.calculation(ballCalculation);
        return calculation.getMaxPrice();
    }
}
