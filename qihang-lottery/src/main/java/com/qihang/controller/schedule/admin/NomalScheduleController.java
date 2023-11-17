package com.qihang.controller.schedule.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qihang.common.vo.BaseDataVO;
import com.qihang.common.vo.BaseVO;
import com.qihang.domain.permutation.PermutationAwardDO;
import com.qihang.reptile.SpiderRunner;
import com.qihang.service.basketball.IBasketballMatchService;
import com.qihang.service.beidan.IBeiDanMatchService;
import com.qihang.service.football.IFootballMatchService;
import com.qihang.service.permutation.IPermutationAwardService;
import com.qihang.service.permutation.PermutationServiceImpl;
import com.qihang.service.winburden.IWinBurdenMatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping("/sch")
@RestController
@Slf4j
public class NomalScheduleController {

    @Resource
    private SpiderRunner spiderRunner;

    @Resource
    IFootballMatchService footballMatchService;


    @Resource
    IBasketballMatchService basketballMatchService;
    @Resource
    IBeiDanMatchService beiDanMatchService;

    @Resource
    IWinBurdenMatchService winBurdenMatchService;

    @Resource
    PermutationServiceImpl permutationService;

    @Resource
    IPermutationAwardService permutationAwardService;

    @RequestMapping("run")
    public BaseVO runday(@RequestParam String type, @RequestParam(value = "", required = false) String issueNo,
                         @RequestParam(value = "", required = false) Integer lotId) {
        long s = System.currentTimeMillis();
        switch (type) {
            //runday
            case "1":
                log.info(" >>>> 手动触发 runday start ");
                spiderRunner.runDay();
                log.info(">>>> 手动触发 runday end ");
                break;
            case "2":
                log.info(" >>>> 手动触发 runOmit start ");
                spiderRunner.runOmit();
                log.info(">>>> 手动触发 runday end ");
                break;
            case "3":
                log.info(" >>>> 手动触发 runHour start ");
                spiderRunner.runHour();
                log.info(">>>> 手动触发 runHour end ");
                break;
            case "4":
                log.info(" >>>> 手动触发 run start ");
                spiderRunner.run();
                log.info(">>>> 手动触发 run end ");
                break;

            case "10":
                log.info("竞猜足球开奖");
                footballMatchService.award();
                log.info("竞猜足球开奖 End");
                break;
            case "11":
                log.info("竞猜篮球开奖");
                basketballMatchService.award();
                log.info("竞猜篮球开奖 End");
                break;
            case "12":
                log.info("北单开奖");
                beiDanMatchService.award();
                log.info("北单开奖 End");
                break;
            case "13":
                log.info("胜负彩开奖");
                winBurdenMatchService.victoryDefeatAward();
                log.info("胜负彩开奖 End");
                break;
            case "14":
                log.info("任九开奖");
                winBurdenMatchService.renJiuAward();
                log.info("任九开奖 End");
                break;
            case "15":
                log.info("排列开奖->期号:{},type:{}", issueNo, lotId);
                //查询当期开奖信息
                PermutationAwardDO awardDO = permutationAwardService.getOne(new QueryWrapper<PermutationAwardDO>().lambda().eq(PermutationAwardDO::getType, lotId).eq(PermutationAwardDO::getStageNumber, issueNo).orderByDesc(PermutationAwardDO::getCreateTime));
                if (null != awardDO) {
                    log.info("排列3开奖->期号:{},type:{},info:{}", issueNo, lotId, awardDO);
                    permutationService.calculation(awardDO);
                }
                log.info("排列开奖 End");
                break;
            default:
                break;

        }
        BaseDataVO vo = new BaseDataVO();
        vo.setData("cost:" + (System.currentTimeMillis() - s));
        return vo;
    }
}
