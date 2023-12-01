package com.qihang.controller.beidan;

import com.qihang.common.vo.CommonListVO;
import com.qihang.controller.beidan.vo.BeiDanSfggVO;
import com.qihang.controller.beidan.vo.BeiDanVO;
import com.qihang.controller.racingball.app.dto.BallCalculationDTO;
import com.qihang.controller.racingball.app.vo.BallCalculationVO;
import com.qihang.service.beidan.IBeidanSfggMatchService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/beidansfgg")
public class BeidanSfggMatchController {

    @Resource
    IBeidanSfggMatchService beidanSfggMatchService;

    @GetMapping("/list")
    @ApiOperation("北单胜负过关比赛列表接口")
    public CommonListVO<BeiDanSfggVO> list() {
        return beidanSfggMatchService.beiDanMatchList();
    }

    @PostMapping("/calculation")
    @ApiOperation("计算 组 注 预测金额接口")
    public BallCalculationVO calculation(@RequestBody BallCalculationDTO ballCalculation) {
        return beidanSfggMatchService.calculation(ballCalculation);
    }
}
