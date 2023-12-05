package com.qihang.controller.statistics;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.qihang.common.vo.BaseDataVO;
import com.qihang.common.vo.BaseVO;
import com.qihang.config.BigdecimalValueFilter;
import com.qihang.controller.statistics.dto.QueryDTO;
import com.qihang.controller.statistics.vo.ReportVO;
import com.qihang.controller.statistics.vo.StatisticsVO;
import com.qihang.service.statistics.IStatisticsService;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

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

    @PostMapping("/day")
    @ApiOperation("day统计接口")
    public BaseVO dayGet(@RequestBody QueryDTO queryDTO) {
        String start = queryDTO.getStart();
        String end = queryDTO.getEnd();
        ReportVO reportVO = ReportVO.builder().changeCounts(0).changeMoney(BigDecimal.ZERO).allMoney(BigDecimal.ZERO).drawCounts(0)
                .drawMoney(BigDecimal.ZERO).orderCounts(0).orderMoney(BigDecimal.ZERO).receiveCounts(0)
                .receiveMoney(BigDecimal.ZERO).rechargeCounts(0).rechargeMoney(BigDecimal.ZERO).revokePrice(BigDecimal.ZERO)
                .awardCounts(0).awardMoney(BigDecimal.ZERO).users(0).build();
        Map<String, Object> bmap = JSONUtil.toBean(JSONUtil.toJsonStr(reportVO), Map.class);
        if (StringUtils.isBlank(queryDTO.getStart()) || StringUtils.isBlank(queryDTO.getEnd())) {
//            Date startDate = DateUtils.addDays(DateUtil.parse(DateUtil.format(DateUtil.date(), "yyyy-MM-dd")), -1);
//            start = DateUtil.formatDate(startDate);
//            end = DateUtil.formatDate(DateUtils.addDays(new Date(), -1));
            return BaseDataVO.builder().success(true).data(bmap).build();
        }
        Map<String, Object> map = statisticsService.daGet(start, end);

        bmap.putAll(map);
        return BaseDataVO.builder().success(true).data(bmap).build();
    }

    @PostMapping("/month")
    @ApiOperation("month统计接口")
    public BaseVO monthGet(@RequestBody QueryDTO queryDTO) {
        ReportVO reportVO = ReportVO.builder().changeCounts(0).changeMoney(BigDecimal.ZERO).allMoney(BigDecimal.ZERO).drawCounts(0)
                .drawMoney(BigDecimal.ZERO).orderCounts(0).orderMoney(BigDecimal.ZERO).receiveCounts(0)
                .receiveMoney(BigDecimal.ZERO).rechargeCounts(0).rechargeMoney(BigDecimal.ZERO).revokePrice(BigDecimal.ZERO)
                .awardCounts(0).awardMoney(BigDecimal.ZERO).users(0).build();
        Map<String, Object> bmap = JSONUtil.toBean(JSONUtil.toJsonStr(reportVO), Map.class);
        if (StringUtils.isBlank(queryDTO.getStart())) {
            return BaseDataVO.builder().success(true).data(bmap).build();
        }
        //String start = DateUtil.format(new Date(), "yyyy-MM");
        String start = queryDTO.getStart();
        start = start + "-01";
        String end = DateUtil.formatDate(DateUtils.addDays(DateUtils.addMonths(DateUtil.parse(start), 1), -1));

        bmap.putAll(statisticsService.daGet(start, end));
        return BaseDataVO.builder().success(true).data(bmap).build();
    }

}
