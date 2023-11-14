package com.qihang.service.documentary.forest;

import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONUtil;
import com.qihang.controller.basketball.dto.BasketballMatchDTO;
import com.qihang.controller.racingball.app.dto.BallCalculationDTO;
import com.qihang.controller.racingball.app.vo.BallCalculationVO;
import com.qihang.service.basketball.IBasketballMatchService;
import com.qihang.service.documentary.DocumentaryServiceHelper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class BasketballHelper extends DocumentaryServiceHelper {


    @Resource
    private IBasketballMatchService basketballMatchService;

    @Override
    public BigDecimal calculationForest(List<String> contentList, String type, int notes, int multiple, String passway) {
        BallCalculationDTO ballCalculation = new BallCalculationDTO();
        ballCalculation.setType(type);
        ballCalculation.setNotes(notes);
        ballCalculation.setMultiple(multiple);
        ballCalculation.setPssTypeList(Convert.toList(Integer.class, passway));
        List<BasketballMatchDTO> footballMatchList = new ArrayList<>();
        for (String s : contentList) {
            footballMatchList.add(JSONUtil.toBean(s, BasketballMatchDTO.class));
        }
        ballCalculation.setBasketballMatchList(footballMatchList);
        BallCalculationVO calculation = basketballMatchService.calculation(ballCalculation);
        return calculation.getMaxPrice();
    }
}
