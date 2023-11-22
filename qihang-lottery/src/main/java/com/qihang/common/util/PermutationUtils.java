package com.qihang.common.util;

import cn.hutool.core.date.DateUtil;
import com.qihang.domain.permutation.PermutationAwardDO;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;

public class PermutationUtils {


    /**
     * 生成下一期 截止时间
     * 必须截止时间 小于当前时间 才会生成。否则返回当前期 数据
     *
     * @param curIssue
     * @return
     */
    public static PermutationAwardDO next(PermutationAwardDO curIssue) {
        PermutationAwardDO next = new PermutationAwardDO();
        next.setStageNumber(curIssue.getStageNumber() + 1);
        next.setType(curIssue.getType());
        next.setCreateTime(new Date());
        next.setUpdateTime(new Date());
        Date now = new Date();
        if (curIssue.getDeadTime() != null && now.before(curIssue.getDeadTime())) {
            return null;
        }
        String nowString = DateUtil.format(now, "yyyy-MM-dd");
        int weekDay = DateUtil.dayOfWeek(new Date()) - 1;
        switch (curIssue.getType()) {
            case "3":// "排列3" 每日 开奖 21：25 截止 21：00
            case "4"://排列5  每日 开奖 21：25 截止 21：00
            case "21"://福彩3D 每日开奖 21:15 截止21：00
            case "23"://快乐8 每日开奖 21:30 截止21：00
                next.setDeadTime(DateUtil.parse(nowString + " 21:00:00", "yyyy-MM-dd HH:mm:ss"));
                break;
            case "5"://七星彩每周二、五、日开奖 dayOfweek 周日0
                int days = 0;
                if (weekDay == 0) {
                    days = 2;
                } else if (2 > weekDay) {
                    days = 2 - weekDay;
                } else if (5 > weekDay) {
                    days = 5 - weekDay;
                } else {
                    //今天 是周五，下次一定是周日 ，
                    days = 2;
                }
                next.setDeadTime(DateUtils.addDays(curIssue.getDeadTime(), days));
                break;
            case "8"://大乐透 每周一、三、六开奖
                days = 0;
                if (weekDay == 0) {
                    days = 1;
                } else if (3 > weekDay) {
                    days = 3 - weekDay;
                } else if (6 > weekDay) {
                    days = 6 - weekDay;
                } else {
                    days = 2;
                }
                next.setDeadTime(DateUtils.addDays(curIssue.getDeadTime(), days));
                break;

            case "22"://七乐彩 每周一 三 五开奖 21：15 开奖，截止21：00
                days = 0;
                if (1 > weekDay) {
                    days = 1;
                } else if (3 > weekDay) {
                    days = 3 - weekDay;
                } else if (5 > weekDay) {
                    days = 5 - weekDay;
                } else {
                    days = 8 - weekDay;
                }
                next.setDeadTime(DateUtils.addDays(curIssue.getDeadTime(), days));
                break;
            case "24"://双色球 每周二 四 日开奖 21:15 截止21：00
                days = 0;
                if (2 > weekDay) {
                    days = 2 - weekDay;
                } else if (4 > weekDay) {
                    days = 4 - weekDay;
                } else if (7 > weekDay) {
                    days = 7 - weekDay;
                }
                next.setDeadTime(DateUtils.addDays(curIssue.getDeadTime(), days));
                break;
        }
        return next;
    }

    public static void main(String[] args) {
        System.out.println(DateUtil.dayOfWeek(new Date()));
        Date now = DateUtil.parse("2023-11-26 21:00:00", "yyyy-MM-dd HH:mm:ss");
        System.out.println(DateUtil.dayOfWeek(now));
        Date now2 = DateUtil.parse("2023-11-25 21:00:00", "yyyy-MM-dd HH:mm:ss");
        System.out.println(DateUtil.dayOfWeek(now2));
    }
}
