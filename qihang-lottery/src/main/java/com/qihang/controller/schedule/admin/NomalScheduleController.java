package com.qihang.controller.schedule.admin;

import com.qihang.common.vo.BaseVO;
import com.qihang.reptile.LotteryProcessor;
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
    private LotteryProcessor lotteryProcessor;

    @RequestMapping("run")
    public BaseVO runday(@RequestParam String type) {
        switch (type) {
            //runday
            case "1":
                log.info(" >>>> 手动触发 runday start ");
                lotteryProcessor.runDay();
                log.info(">>>> 手动触发 runday end ");
                break;
            case "2":
                log.info(" >>>> 手动触发 runOmit start ");
                lotteryProcessor.runOmit();
                log.info(">>>> 手动触发 runday end ");
                break;
            case "3":
                log.info(" >>>> 手动触发 runHour start ");
                lotteryProcessor.runHour();
                log.info(">>>> 手动触发 runHour end ");
                break;
            case "4":
                log.info(" >>>> 手动触发 run start ");
                lotteryProcessor.run();
                log.info(">>>> 手动触发 run end ");
                break;
            default:
                break;

        }
        return new BaseVO();
    }
}
