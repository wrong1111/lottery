package com.qihang.controller.statistics;

import com.qihang.controller.statistics.vo.StatisticsVO;
import com.qihang.service.statistics.IStatisticsService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author: bright
 * @description:
 * @time: 2022-11-06 13:47
 */
@RestController
@RequestMapping("/admin/statistics")
public class StatisticsController {

    @Resource
    private IStatisticsService statisticsService;

    @GetMapping("/get")
    @ApiOperation("统计接口")
    public StatisticsVO calculation() {
        return statisticsService.calculation();
    }
}
