package com.qihang.reptile;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.qihang.common.util.reward.LotteryAlgorithmUtil;
import com.qihang.constant.CrawlingAddressConstant;
import com.qihang.domain.basketball.BasketballMatchDO;
import com.qihang.domain.beidan.BeiDanMatchDO;
import com.qihang.domain.beidan.BeiDanSFGGMatchDO;
import com.qihang.domain.football.FootballMatchDO;
import com.qihang.domain.omit.OmitDO;
import com.qihang.domain.permutation.PermutationAwardDO;
import com.qihang.domain.winburden.WinBurdenMatchDO;
import com.qihang.enumeration.order.lottery.LotteryOrderTypeEnum;
import com.qihang.service.racingball.RacingBallServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author: bright
 * @description:
 * @time: 2022-10-03 10:28
 */

@Slf4j
@Component
public class LotteryProcessor implements PageProcessor {

    private Site site = Site.me().setRetryTimes(1).setSleepTime(300);

    @Override
    public void process(Page page) {
        Html html = page.getHtml();
        if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL1)) {

            List<FootballMatchDO> footballMatchList = new ArrayList<>();
            try {
                List<Selectable> nodes = html.css(".bet-date-wrap").nodes();
                for (int i = 1; i <= nodes.size(); i++) {
                    Selectable selectableDate = html.xpath("//*[@id='relativeContainer']//*[@class='bet-date-wrap'][" + (i + 1) + "]");
                    Selectable selectableTable = html.xpath("//*[@id='relativeContainer']//*[@class='bet-tb-dg'][" + i + "]");
                    List<Selectable> tr = selectableTable.css("tr").nodes();
                    int index = 0;
                    for (int j = 0; j < tr.size(); j++) {
                        String match = tr.get(j).css(".bet-tb-tr .td-evt a", "text").toString();
                        String number = tr.get(j).css(".bet-tb-tr .td-no a", "text").toString();
                        if (StrUtil.isBlank(match) && StrUtil.isBlank(number)) {
                            continue;
                        }
                        index += 2;
                        FootballMatchDO footballMatch = new FootballMatchDO();
                        footballMatch.setStartTime(selectableDate.css(".bet-date", "text").get());
                        footballMatch.setNumber(number);
                        footballMatch.setMatch(match);
                        String color = tr.get(j).xpath("//*[@class='td-evt']/a/@style").toString();
                        footballMatch.setColor(color.substring(color.indexOf("#"), color.length() - 1));
                        footballMatch.setOpenTime(tr.get(j).css(".td-endtime", "text").toString());
                        footballMatch.setHomeTeam(tr.get(j).css(".td-team .team-l i", "text").toString() + tr.get(j).css(".td-team .team-l a", "text").toString());
                        footballMatch.setVisitingTeam(tr.get(j).css(".td-team .team-r i", "text").toString() + tr.get(j).css(".td-team .team-r a", "text").toString());
                        footballMatch.setLetBall(tr.get(j).css(".td-rang .itm-rangA2", "text").toString());
                        footballMatch.setNotLetOdds(StrUtil.join(",", tr.get(j).css(".itm-rangB1 .betbtn span:first-child", "text").all()).replaceAll(",↑", "").replaceAll(",↓", ""));
                        footballMatch.setLetOdds(StrUtil.join(",", tr.get(j).css(".itm-rangB2 .betbtn span:first-child", "text").all()).replaceAll(",↑", "").replaceAll(",↓", ""));
                        footballMatch.setIsSingle((tr.get(j).css(".td-rang .itm-rangA1 .ico-dg", "text").toString()) != null || (tr.get(j).css(".td-rang .itm-rangA2 .ico-dg", "text").toString()) != null ? "1" : "0");
                        footballMatch.setHalfWholeOdds(StrUtil.join(",", html.xpath("/html/body/div[6]/div/div[3]/table[" + i + "]/tbody/tr[" + index + "]/td/div/table[1]/tbody/tr/td/p/i/text()").all()).replaceAll(",↑", "").replaceAll(",↓", ""));
                        footballMatch.setScoreOdds(StrUtil.join(",", html.xpath("/html/body/div[6]/div/div[3]/table[" + i + "]/tbody/tr[" + index + "]/td/div/table[2]/tbody/tr/td/p/i/text()").all()).replaceAll(",↑", "").replaceAll(",↓", ""));
                        footballMatch.setGoalOdds(StrUtil.join(",", html.xpath("/html/body/div[6]/div/div[3]/table[" + i + "]/tbody/tr[" + index + "]/td/div/table[3]/tbody/tr/td/p/i/text()").all()).replaceAll(",↑", "").replaceAll(",↓", ""));
                        footballMatch.setDeadline(LotteryAlgorithmUtil.calculationDeadline(footballMatch.getOpenTime(), footballMatch.getStartTime()));
                        footballMatch.setCreateTime(new Date());
                        footballMatch.setUpdateTime(new Date());
                        footballMatch.setIssueNo(RacingBallServiceImpl.getDeadline(footballMatch.getDeadline()));
                        footballMatch.setGameNo(RacingBallServiceImpl.getMatchGameNo(footballMatch.getNumber(), footballMatch.getDeadline()));
                        footballMatchList.add(footballMatch);
                    }
                }
            } catch (Exception e) {
                log.error(" url {} ，异常 {}", page.getUrl().toString(), e);
            }
            log.info(" 足彩比赛 >>>> {} ，result:{}", page.getUrl().toString(), JSON.toJSONString(footballMatchList));
            page.putField("footballGoalList", footballMatchList);
        } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL2) || ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL16) || ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL17) || ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL20) || ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL_FC3D) || ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL_QLC) || ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL_SSQ) || ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL_KL8)) {
            log.info(page.getUrl() + " >> {}", html);
            //排列 开奖结果爬取
            PermutationAwardDO permutationAward = new PermutationAwardDO();
            try {
                permutationAward.setStageNumber(Integer.valueOf(page.getHtml().css(".td_title01 .cfont2 strong", "text").get()));
                List<String> rewardList = page.getHtml().css(".ball_box01 .ball_orange", "text").all();
                if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL2)) {
                    permutationAward.setType(LotteryOrderTypeEnum.ARRAY.getKey());
                } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL16)) {
                    permutationAward.setType(LotteryOrderTypeEnum.ARRANGE.getKey());
                } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL17)) {
                    permutationAward.setType(LotteryOrderTypeEnum.SEVEN_STAR.getKey());
                    String r = html.xpath("/html/body/div[6]/div[3]/div[2]/div[1]/div[2]/table[2]/tbody/tr[3]/td[3]/text()").toString().trim().replaceAll(",", "") + "," + html.xpath("/html/body/div[6]/div[3]/div[2]/div[1]/div[2]/table[2]/tbody/tr[4]/td[3]/text()").toString().trim().replaceAll(",", "") + "," + html.xpath("/html/body/div[6]/div[3]/div[2]/div[1]/div[2]/table[2]/tbody/tr[5]/td[3]/text()").toString().trim().replaceAll(",", "") + "," + html.xpath("/html/body/div[6]/div[3]/div[2]/div[1]/div[2]/table[2]/tbody/tr[6]/td[3]/text()").toString().trim().replaceAll(",", "") + "," + html.xpath("/html/body/div[6]/div[3]/div[2]/div[1]/div[2]/table[2]/tbody/tr[7]/td[3]/text()").toString().trim().replaceAll(",", "") + "," + html.xpath("/html/body/div[6]/div[3]/div[2]/div[1]/div[2]/table[2]/tbody/tr[8]/td[3]/text()").toString().trim().replaceAll(",", "") + "," + html.xpath("/html/body/div[6]/div[3]/div[2]/div[1]/div[2]/table[2]/tbody/tr[9]/td[3]/text()").toString().trim().replaceAll(",", "");
                    permutationAward.setMoneyAward(r);
                } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL20)) {
                    permutationAward.setType(LotteryOrderTypeEnum.GRAND_LOTTO.getKey());
                    rewardList = page.getHtml().css(".ball_box01 li", "text").all();
                    permutationAward.setMoneyAward(html.xpath("/html/body/div[6]/div[3]/div[2]/div[1]/div[2]/table[2]/tbody/tr[3]/td[4]/text()").toString().replaceAll(",", "") + "," + html.xpath("/html/body/div[6]/div[3]/div[2]/div[1]/div[2]/table[2]/tbody/tr[5]/td[4]/text()").toString().replaceAll(",", ""));
                } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL_FC3D)) {
                    permutationAward.setType(LotteryOrderTypeEnum.FC3D.getKey());
                } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL_QLC)) {
                    permutationAward.setType(LotteryOrderTypeEnum.FCQLC.getKey());
                    //开奖号码
                    rewardList = page.getHtml().css(".ball_box01 li", "text").all();
                    String r = html.xpath("/html/body/div[6]/div[3]/div[2]/div[1]/div[2]/table[2]/tbody/tr[3]/td[3]/text()").toString().trim().replaceAll(",", "") + "," + html.xpath("/html/body/div[6]/div[3]/div[2]/div[1]/div[2]/table[2]/tbody/tr[4]/td[3]/text()").toString().trim().replaceAll(",", "") + "," + html.xpath("/html/body/div[6]/div[3]/div[2]/div[1]/div[2]/table[2]/tbody/tr[5]/td[3]/text()").toString().trim().replaceAll(",", "") + "," + html.xpath("/html/body/div[6]/div[3]/div[2]/div[1]/div[2]/table[2]/tbody/tr[6]/td[3]/text()").toString().trim().replaceAll(",", "") + "," + html.xpath("/html/body/div[6]/div[3]/div[2]/div[1]/div[2]/table[2]/tbody/tr[7]/td[3]/text()").toString().trim().replaceAll(",", "") + "," + html.xpath("/html/body/div[6]/div[3]/div[2]/div[1]/div[2]/table[2]/tbody/tr[8]/td[3]/text()").toString().trim().replaceAll(",", "") + "," + html.xpath("/html/body/div[6]/div[3]/div[2]/div[1]/div[2]/table[2]/tbody/tr[9]/td[3]/text()").toString().trim().replaceAll(",", "");
                    permutationAward.setMoneyAward(r);
                } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL_SSQ)) {
                    permutationAward.setType(LotteryOrderTypeEnum.FCSSQ.getKey());
                    //开奖号码
                    rewardList = page.getHtml().css(".ball_box01 li", "text").all();
                } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL_KL8)) {
                    permutationAward.setType(LotteryOrderTypeEnum.FCKL8.getKey());
                    //开奖号码
                    rewardList = page.getHtml().css(".ball_box01 li", "text").all();
                }
                permutationAward.setReward(StrUtil.join(",", rewardList));
            } catch (Exception e) {
                log.error(" url {},error :{}", page.getUrl().toString(), e);

            }
            log.info(" 开奖>>>>>>>{} ,result:{} ", page.getUrl().toString(), JSON.toJSONString(permutationAward));
            page.putField("permutation", permutationAward);
        } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL3)) {
            //抓取竞彩网的队伍分析数据
            List<FootballMatchDO> footballMatchList = new ArrayList<>();
            try {
                List<Selectable> nodes = html.css(".mainArea .match_item").nodes();
                for (int i = 0; i < nodes.size(); i++) {
                    FootballMatchDO footballMatch = new FootballMatchDO();
                    footballMatch.setNumber(nodes.get(i).css(".list_main  .matchNo", "text").toString());
//                footballMatch.setHomeTeam(nodes.get(i).css(".list_main  .analysis home", "text").toString());
//                footballMatch.setVisitingTeam(nodes.get(i).css(".list_main  .analysis guest", "text").toString());
                    String clk = html.xpath("//*[@id=\"divPage_Main\"]/div[2]/div[" + (i + 1) + "]/div[1]/div[2]/div[1]/@onclick").toString();
                    footballMatch.setAnalysis("http://wap.310win.com" + clk.substring(clk.indexOf("('") + 2, clk.indexOf("')")));
                    footballMatchList.add(footballMatch);
                }
            } catch (Exception e) {
                log.error(" url {},error :{} ", page.getUrl().toString(), e);
            }
            log.info(" 足彩对局分析 >>>>>>>{} ,result:{} ", page.getUrl().toString(), JSON.toJSONString(footballMatchList));
            page.putField("footballGoalList", footballMatchList);
        } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL4)) {
            List<BasketballMatchDO> basketballMatchList = new ArrayList<>();
            try {
                List<Selectable> nodes = html.css(".bet-date-wrap").nodes();
                for (int i = 1; i <= nodes.size(); i++) {
                    Selectable selectableDate = html.xpath("//*[@id='relativeContainer']//*[@class='bet-date-wrap'][" + (i + 1) + "]");
                    Selectable selectableTable = html.xpath("//*[@id='relativeContainer']//*[@class='bet-tb-dg'][" + i + "]");
                    List<Selectable> tr = selectableTable.css("tr").nodes();
                    int index = 0;
                    for (int j = 0; j < tr.size(); j++) {
                        String match = tr.get(j).css(".bet-tb-tr .td-evt a", "text").toString();
                        String number = tr.get(j).css(".bet-tb-tr .td-no a", "text").toString();
                        if (StrUtil.isBlank(match) && StrUtil.isBlank(number)) {
                            continue;
                        }
                        index += 2;
                        BasketballMatchDO basketballMatch = new BasketballMatchDO();
                        basketballMatch.setStartTime(selectableDate.css(".bet-date", "text").get());
                        basketballMatch.setNumber(number);
                        basketballMatch.setMatch(match);
                        String color = tr.get(j).xpath("//*[@class='td-evt']/a/@style").toString();
                        basketballMatch.setColor(color.substring(color.indexOf("#"), color.length() - 1));
                        basketballMatch.setOpenTime(tr.get(j).css(".td-endtime span", "text").toString());
                        basketballMatch.setVisitingTeam(tr.get(j).css(".td-team .team-l i", "text").toString() + tr.get(j).css(".td-team .team-l a", "text").toString());
                        basketballMatch.setHomeTeam(tr.get(j).css(".td-team .team-r i", "text").toString() + tr.get(j).css(".td-team .team-r a", "text").toString());
                        basketballMatch.setWinNegativeOdds(StrUtil.join(",", tr.get(j).css(".betbtn-row-sf .betbtn span", "text").all()).replaceAll(",↑", "").replaceAll(",↓", ""));
                        basketballMatch.setCedePointsOdds(StrUtil.join(",", tr.get(j).css(".betbtn-row-rfsf .betbtn span", "text").all()).replaceAll(",↑", "").replaceAll(",↓", ""));
                        basketballMatch.setCedePoints(StrUtil.join(",", tr.get(j).css(".betbtn-row-rfsf .betmsg span", "text").all()).replaceAll(",↑", "").replaceAll(",↓", ""));
                        basketballMatch.setSizeOdds(StrUtil.join(",", tr.get(j).css(".betbtn-row-dxf p span", "text").all()).replaceAll(",↑", "").replaceAll(",↓", ""));
                        basketballMatch.setDifferenceOdds(StrUtil.join(",", html.xpath("/html/body/div[6]/div/div[2]/table[" + i + "]/tbody/tr[" + index + "]/td/div/table/tbody/tr/td/p/i/text()").all()).replaceAll(",↑", "").replaceAll(",↓", ""));
                        basketballMatch.setDeadline(LotteryAlgorithmUtil.calculationDeadline(basketballMatch.getOpenTime(), basketballMatch.getStartTime()));
                        basketballMatch.setCreateTime(new Date());
                        basketballMatch.setUpdateTime(new Date());
                        basketballMatch.setIssueNo(RacingBallServiceImpl.getDeadline(basketballMatch.getDeadline()));
                        basketballMatch.setGameNo(RacingBallServiceImpl.getMatchGameNo(basketballMatch.getNumber(), basketballMatch.getDeadline()));
                        basketballMatchList.add(basketballMatch);
                    }
                }
            } catch (Exception e) {
                log.error(" url {},error :{} ", page.getUrl().toString(), e);
            }
            log.info(" 篮彩比赛 >>>>>>>{} ,result:{} ", page.getUrl().toString(), JSON.toJSONString(basketballMatchList));
            page.putField("basketballMatchList", basketballMatchList);
        } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL5)) {
            //抓取竞彩网的队伍分析数据
            List<BasketballMatchDO> basketballMatchList = new ArrayList<>();
            try {
                List<Selectable> nodes = html.css(".mainArea .match_item").nodes();
                for (int i = 0; i < nodes.size(); i++) {
                    BasketballMatchDO basketballMatch = new BasketballMatchDO();
                    basketballMatch.setNumber(nodes.get(i).css(".list_main  .matchNo", "text").toString());
//                footballMatch.setHomeTeam(nodes.get(i).css(".list_main  .analysis home", "text").toString());
//                footballMatch.setVisitingTeam(nodes.get(i).css(".list_main  .analysis guest", "text").toString());
                    String clk = html.xpath("//*[@id=\"divPage_Main\"]/div[2]/div[" + (i + 1) + "]/div[1]/div[2]/div[1]/@onclick").toString();
                    basketballMatch.setAnalysis("http://wap.310win.com" + clk.substring(clk.indexOf("('") + 2, clk.indexOf("')")));
                    basketballMatchList.add(basketballMatch);
                }
            } catch (Exception e) {
                log.error(" url {},error :{} ", page.getUrl().toString(), e);
            }
            log.info(" 篮球对局分析 >>>>>>>{} ,result:{} ", page.getUrl().toString(), JSON.toJSONString(basketballMatchList));
            page.putField("basketballMatchList", basketballMatchList);
        } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL6)) {
            List<FootballMatchDO> footballMatchList = new ArrayList<>();
            try {
                List<Selectable> nodes = html.css("table.ld_table > tbody>tr").nodes();
                for (int i = 1; i < nodes.size(); i++) {
                    FootballMatchDO footballMatch = new FootballMatchDO();
                    footballMatch.setNumber(nodes.get(i).css("td:nth-child(1)", "text").toString().trim());
                    footballMatch.setOpenTime(nodes.get(i).css("td:nth-child(3)", "text").toString().trim());
                    //footballMatch.setHomeTeam((nodes.get(i).css(".text_r a", "text").toString()).replace(" ", ""));
                    //footballMatch.setVisitingTeam((nodes.get(i).css(".text_l a", "text").toString()).replace(" ", ""));
                    String str = nodes.get(i).css("td:nth-child(7)", "text").toString();
                    String result = nodes.get(i).css("td:nth-child(18)", "text").toString();
                    result = result.substring(0, 1) + "-" + result.substring(1, 2);
                    footballMatch.setAward(nodes.get(i).css("td:nth-child(12)", "text").toString() + "," + nodes.get(i).css("td:nth-child(9)", "text").toString() + "," + nodes.get(i).css("td:nth-child(15)", "text").toString() + "," + result + "," + str.substring(str.lastIndexOf(")") + 1).trim());
                    footballMatch.setHalfFullCourt(StrUtil.join(",", str.split(" ")));
                    footballMatchList.add(footballMatch);
                }
            } catch (Exception e) {
                log.error(" url {},error :{} ", page.getUrl().toString(), e);
            }
            log.info(" 足球开奖 >>>>>>>{} ,result:{} ", page.getUrl().toString(), JSON.toJSONString(footballMatchList));
            page.putField("footballGoalList", footballMatchList);
        } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL7)) {
            //篮球开奖
            List<BasketballMatchDO> basketballMatchList = new ArrayList<>();
            try {
                List<Selectable> nodes = html.css(".lea_list table.ld_table > tbody>tr").nodes();
                for (int i = 1; i < nodes.size(); i++) {
                    BasketballMatchDO basketballMatch = new BasketballMatchDO();
                    basketballMatch.setNumber(nodes.get(i).css("td:nth-child(1)", "text").toString().trim());
                    basketballMatch.setOpenTime(nodes.get(i).css("td:nth-child(3)", "text").toString().trim());
                    basketballMatch.setHomeTeam((nodes.get(i).css(".text_l a", "text").toString()).replace(" ", ""));
                    basketballMatch.setVisitingTeam((nodes.get(i).css(".text_r a", "text").toString()).replace(" ", ""));

                    String str = nodes.get(i).css("td:nth-child(12)", "text").toString();
                    String score = nodes.get(i).css("td:nth-child(7)", "text").toString();
                    String result = "";
                    if (StrUtil.isNotBlank(score) && !score.equals("-")) {
                        int[] scoreArr = StrUtil.splitToInt(score, ":");
                        //
                        //500 客队在前，主队在后 让分为正，为客让分，负数为主让分。
                        if (scoreArr[0] - Double.valueOf(str) > scoreArr[1]) {
                            result = "主负";
                        } else {
                            result = "主胜";
                        }
                    }
                    //没出结果直接跳过
                    if (score.contains("-")) {
                        continue;
                    }
                    basketballMatch.setAward(nodes.get(i).css("td:nth-child(8)", "text").toString() + "," + result + "," + nodes.get(i).css("td:nth-child(15)", "text").toString() + "," + nodes.get(i).css("td:nth-child(19)", "text").toString());
                    basketballMatch.setHalfFullCourt(score);
                    basketballMatchList.add(basketballMatch);
                }
            } catch (Exception e) {
                log.error(" url {},error :{} ", page.getUrl().toString(), e);
            }
            log.info(" 篮球开奖 >>>>>>>{} ,result:{} ", page.getUrl().toString(), JSON.toJSONString(basketballMatchList));
            page.putField("basketballMatchList", basketballMatchList);
        } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL8)) {
            //ID=expect_select
            String issueNo = html.css("#expect_select option", "text").toString().split(" ")[0];
            List<BeiDanMatchDO> beiDanMatchList = new ArrayList<>();
            try {
                List<Selectable> nodes = html.css("#vsTable #vs_table tbody").nodes();
                String startTime = "";
                for (int i = 0; i < nodes.size(); i++) {
                    if (i % 2 == 0) {
                        startTime = nodes.get(i).css(".dc_hs strong", "text").toString();
                        continue;
                    }
                    List<Selectable> selectables = nodes.get(i).css("tr").nodes();
                    for (int j = 0; j < selectables.size(); j++) {
                        //过滤第一个标题
                        if (selectables.get(j).css("tr", "style").toString().equals("display:none")) continue;
                        BeiDanMatchDO beiDanMatch = new BeiDanMatchDO();
                        beiDanMatch.setStartTime(startTime);
                        beiDanMatch.setNumber(selectables.get(j).css("tr td:nth-child(1) .chnum", "text").toString());
                        String match = selectables.get(j).css("tr .league a", "text").toString();
                        if (null == match) {
                            match = selectables.get(j).css("tr .league span", "text").toString();
                        }
                        beiDanMatch.setMatch(match);
                        String color = selectables.get(j).xpath("//*[@class='vs_lines']/td[2]/@style").toString();
                        beiDanMatch.setColor(color.substring(color.indexOf("#"), color.indexOf(";")));
                        String openTime = selectables.get(j).css("tr td:nth-child(3) .eng", "text").toString();
                        String homeTeam = selectables.get(j).css("tr td:nth-child(4) a", "text").toString();
                        if (null == homeTeam) {
                            homeTeam = selectables.get(j).css("tr td:nth-child(4)", "text").toString();
                        }
                        beiDanMatch.setHomeTeam(homeTeam.replace(" ", ""));
                        beiDanMatch.setLetBall(selectables.get(j).css("tr td:nth-child(5) strong strong", "text").toString());
                        String visitingTeam = selectables.get(j).css("tr td:nth-child(6) a", "text").toString();
                        if (null == visitingTeam) {
                            visitingTeam = selectables.get(j).css("tr td:nth-child(6)", "text").toString();
                        }
                        beiDanMatch.setVisitingTeam(visitingTeam.replace(" ", ""));
                        String deadline = beiDanMatch.getStartTime().substring(0, beiDanMatch.getStartTime().indexOf(" ")) + " " + openTime + ":00";
                        DateTime date = DateUtil.parse(deadline);
                        int hour = DateUtil.hour(date, true);
                        if (hour >= 0 && hour <= 5) {
                            date = DateUtil.offsetDay(date, 1);
                        }
                        beiDanMatch.setDeadline(date);
                        beiDanMatch.setLetOdds(StrUtil.join(",", selectables.get(j).css(".sp_value.eng", "text").all()).replaceAll(",↑", "").replaceAll(",↓", ""));
                        beiDanMatch.setIssueNo(issueNo);
                        //北单 保留四位数
                        beiDanMatch.setGameNo(issueNo + RacingBallServiceImpl.fillZero(beiDanMatch.getNumber(), 4));
                        beiDanMatchList.add(beiDanMatch);
                    }
                }
            } catch (Exception e) {
                log.error(" url {},error :{} ", page.getUrl().toString(), e);
            }
            log.info(" 北京单场 >>>>>>>{} ,result:{} ", page.getUrl().toString(), JSON.toJSONString(beiDanMatchList));
            page.putField("beiDanMatchList", beiDanMatchList);
        } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL9) || ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL10) || ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL12)) {
            List<BeiDanMatchDO> beiDanMatchList = new ArrayList<>();
            try {
                String issueNo = html.css("#expect_select option", "text").toString().split(" ")[0];
                List<Selectable> nodes = html.css("#vsTable #vs_table tbody").nodes();
                String url = page.getUrl().toString();
                for (int i = 0; i < nodes.size(); i++) {
                    //过滤第一个标题
                    if (i % 2 == 0) {
                        continue;
                    }
                    List<Selectable> selectables = nodes.get(i).css("tr").nodes();
                    for (int j = 0; j < selectables.size(); j++) {
                        //如果是隐藏的直接就跳过
                        if (selectables.get(j).css("tr", "style").toString().equals("display:none")) continue;
                        BeiDanMatchDO beiDanMatch = new BeiDanMatchDO();
                        beiDanMatch.setNumber(selectables.get(j).css("tr td:nth-child(1) .chnum", "text").toString());
                        String match = selectables.get(j).css("tr .league a", "text").toString();
                        if (null == match) {
                            match = selectables.get(j).css("tr .league span", "text").toString();
                        }
                        beiDanMatch.setMatch(match);
                        String homeTeam = selectables.get(j).css("tr td:nth-child(4) a", "text").toString();
                        if (null == homeTeam) {
                            homeTeam = selectables.get(j).css("tr td:nth-child(4)", "text").toString();
                        }
                        beiDanMatch.setHomeTeam(homeTeam.replace(" ", ""));
                        String visitingTeam = selectables.get(j).css("tr td:nth-child(5) a", "text").toString();
                        if (null == visitingTeam) {
                            visitingTeam = selectables.get(j).css("tr td:nth-child(5)", "text").toString();
                        }
                        beiDanMatch.setVisitingTeam(visitingTeam.replace(" ", ""));
                        if (url.equals(CrawlingAddressConstant.URL9)) {
                            beiDanMatch.setGoalOdds(StrUtil.join(",", selectables.get(j).css(".sp_value.eng", "text").all()).replaceAll(" ", "").replaceAll(",↑", "").replaceAll(",↓", ""));
                        } else if (url.equals(CrawlingAddressConstant.URL10)) {
                            beiDanMatch.setOddEvenOdds(StrUtil.join(",", selectables.get(j).css(".sp_value.eng", "text").all()).replaceAll(",↑", "").replaceAll(",↓", ""));
                        } else if (url.equals(CrawlingAddressConstant.URL12)) {
                            beiDanMatch.setHalfWholeOdds(StrUtil.join(",", selectables.get(j).css(".sp_value.eng", "text").all()).replaceAll(" ", "").replaceAll(",↑", "").replaceAll(",↓", ""));
                        }
                        beiDanMatch.setCreateTime(new Date());
                        beiDanMatch.setUpdateTime(new Date());
                        beiDanMatch.setIssueNo(issueNo);
                        beiDanMatchList.add(beiDanMatch);
                    }
                }
            } catch (Exception e) {
                log.error(" url {},error :{} ", page.getUrl().toString(), e);
            }
            log.info(" 北京单场 >>>>>>>{} ,result:{} ", page.getUrl().toString(), JSON.toJSONString(beiDanMatchList));
            page.putField("beiDanMatchList", beiDanMatchList);
        } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL11)) {
            List<BeiDanMatchDO> beiDanMatchList = new ArrayList<>();
            try {
                List<Selectable> nodes = html.css("#vsTable #vs_table>tbody").nodes();
                for (int i = 0; i < nodes.size(); i++) {
                    //过滤第一个标题
                    if (i % 2 == 0) {
                        continue;
                    }
                    List<Selectable> selectables = nodes.get(i).css("tr").nodes();
                    for (int j = 0; j < selectables.size(); j++) {
                        //如果是隐藏的直接就跳过
                        if (selectables.get(j).css("tr", "style").toString().equals("display:none")) continue;
                        //过滤其它的tr标签
                        if (selectables.get(j).css(".vs_lines").toString() == null) continue;
                        BeiDanMatchDO beiDanMatch = new BeiDanMatchDO();
                        beiDanMatch.setNumber(selectables.get(j).css("tr td:nth-child(1) .chnum", "text").toString());
                        String match = selectables.get(j).css("tr .league a", "text").toString();
                        if (null == match) {
                            match = selectables.get(j).css("tr .league span", "text").toString();
                        }
                        beiDanMatch.setMatch(match);
                        String homeTeam = selectables.get(j).css("tr td:nth-child(4) a", "text").toString();
                        if (null == homeTeam) {
                            homeTeam = selectables.get(j).css("tr td:nth-child(4)", "text").toString();
                        }
                        beiDanMatch.setHomeTeam(homeTeam.replace(" ", ""));
                        String visitingTeam = selectables.get(j).css("tr td:nth-child(6) a", "text").toString();
                        if (null == visitingTeam) {
                            visitingTeam = selectables.get(j).css("tr td:nth-child(6)", "text").toString();
                        }
                        beiDanMatch.setVisitingTeam(visitingTeam.replace(" ", ""));
                        beiDanMatch.setScoreOdds(StrUtil.join(",", selectables.get(j + 1).css(".hide_table .sp_value", "text").all()).replaceAll(",↑", "").replaceAll(",↓", ""));
                        beiDanMatch.setCreateTime(new Date());
                        beiDanMatch.setUpdateTime(new Date());
                        beiDanMatchList.add(beiDanMatch);
                    }
                }
            } catch (Exception e) {
                log.error(" url {},error :{} ", page.getUrl().toString(), e);
            }
            log.info(" 北京单场 比分 >>>>>>>{} ,result:{} ", page.getUrl().toString(), JSON.toJSONString(beiDanMatchList));
            page.putField("beiDanMatchList", beiDanMatchList);
        } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL13)) {
            //北单开奖
            List<BeiDanMatchDO> beiDanMatchList = new ArrayList<>();
            try {
                List<Selectable> nodes = html.css("table.ld_table > tbody>tr").nodes();
                for (int i = 1; i < nodes.size(); i++) {
                    BeiDanMatchDO beiDanMatch = new BeiDanMatchDO();
                    beiDanMatch.setNumber(nodes.get(i).css("td:nth-child(1)", "text").toString().trim());
                    beiDanMatch.setMatch(nodes.get(i).css("td:nth-child(2) a", "text").toString().trim());
                    beiDanMatch.setHomeTeam((nodes.get(i).css(".text_r a", "text").toString()).replace(" ", ""));
                    beiDanMatch.setVisitingTeam((nodes.get(i).css(".text_l a", "text").toString()).replace(" ", ""));
                    //比分
                    String str = nodes.get(i).css("td:nth-child(7)", "text").toString();
                    String winData = nodes.get(i).css("td:nth-child(9)", "text").toString();
                    //查到为空的就不需要继续解析了直接跳出
                    if (StrUtil.isBlank(winData) || nodes.get(i).css("td:nth-child(10) span", "text").toString().equals("-")) {
                        continue;
                    }
                    if (winData.equals("3")) {
                        winData = "胜";
                    } else if (winData.equals("1")) {
                        winData = "平";
                    } else if (winData.equals("0")) {
                        winData = "负";
                    }
                    beiDanMatch.setAward(winData + "," + nodes.get(i).css("td:nth-child(12)", "text").toString().trim() + "," + nodes.get(i).css("td:nth-child(15)", "text").toString().trim() + "," + nodes.get(i).css("td:nth-child(18)", "text").toString().trim() + "," + nodes.get(i).css("td:nth-child(21)", "text").toString().trim());
                    beiDanMatch.setHalfFullCourt(StrUtil.join(",", str.split(" ")));
                    beiDanMatch.setBonusOdds(nodes.get(i).css("td:nth-child(10) span", "text").toString() + "," + nodes.get(i).css("td:nth-child(13) span", "text") + "," + nodes.get(i).css("td:nth-child(16) span", "text") + "," + nodes.get(i).css("td:nth-child(19) span", "text") + "," + nodes.get(i).css("td:nth-child(22) span", "text"));
                    beiDanMatchList.add(beiDanMatch);
                }
            } catch (Exception e) {
                log.error(" url {},error :{} ", page.getUrl().toString(), e);
            }
            log.info(" 北单开奖 >>>>>>>{} ,result:{} ", page.getUrl().toString(), JSON.toJSONString(beiDanMatchList));
            page.putField("beiDanMatchList", beiDanMatchList);
        } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL14)) {
            List<BasketballMatchDO> basketballMatchList = new ArrayList<>();
            try {
                List<Selectable> nodes = html.css(".bet-date-wrap").nodes();
                for (int i = 1; i <= nodes.size(); i++) {
                    Selectable selectableDate = html.xpath("//*[@id='relativeContainer']//*[@class='bet-date-wrap'][" + (i + 1) + "]");
                    Selectable selectableTable = html.xpath("//*[@id='relativeContainer']//*[@class='bet-tb-dg'][" + i + "]");
                    List<Selectable> tr = selectableTable.css("tr").nodes();
                    for (int j = 0; j < tr.size(); j++) {
                        String dan = tr.get(j).css(".td-team .ico-dg", "text").toString();
                        if (ObjectUtil.isNull(dan)) {
                            continue;
                        }
                        String match = tr.get(j).css(".bet-tb-tr .td-evt a", "text").toString();
                        String number = tr.get(j).css(".bet-tb-tr .td-no a", "text").toString();
                        if (StrUtil.isBlank(match) && StrUtil.isBlank(number)) {
                            continue;
                        }
                        BasketballMatchDO basketballMatch = new BasketballMatchDO();
                        basketballMatch.setStartTime(selectableDate.css(".bet-date", "text").get());
                        basketballMatch.setNumber(number);
                        basketballMatch.setMatch(match);
                        basketballMatch.setOpenTime(tr.get(j).css(".td-endtime", "text").toString());
                        basketballMatch.setVisitingTeam(tr.get(j).css(".td-team .team-l i", "text").toString() + tr.get(j).css(".td-team .team-l a", "text").toString());
                        basketballMatch.setHomeTeam(tr.get(j).css(".td-team .team-r i", "text").toString() + tr.get(j).css(".td-team .team-r a", "text").toString());
                        basketballMatchList.add(basketballMatch);
                    }
                }
            } catch (Exception e) {
                log.error(" url {},error :{} ", page.getUrl().toString(), e);
            }
            log.info(" 篮球单关查询 >>>>>>>{} ,result:{} ", page.getUrl().toString(), JSON.toJSONString(basketballMatchList));
            page.putField("basketballMatchList", basketballMatchList);
        } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL15)) {
            //抓取竞彩网的队伍分析数据
            List<BeiDanMatchDO> beiDanMatchList = new ArrayList<>();
            try {
                List<Selectable> nodes = html.css(".mainArea .match_item").nodes();
                for (int i = 0; i < nodes.size(); i++) {
                    BeiDanMatchDO beiDanMatch = new BeiDanMatchDO();
                    String number = nodes.get(i).css(".list_main  .matchNo", "text").toString();
                    beiDanMatch.setNumber(number.substring(1, number.length() - 1));
//                beiDanMatch.setHomeTeam(nodes.get(i).css(".list_main  .analysis home", "text").toString());
//                beiDanMatch.setVisitingTeam(nodes.get(i).css(".list_main  .analysis guest", "text").toString());
                    String clk = html.xpath("//*[@id=\"divPage_Main\"]/div[2]/div[" + (i + 1) + "]/div[1]/div[2]/div[1]/@onclick").toString();
                    beiDanMatch.setAnalysis("http://wap.310win.com" + clk.substring(clk.indexOf("('") + 2, clk.indexOf("')")));
                    beiDanMatchList.add(beiDanMatch);
                }
            } catch (Exception e) {
                log.error(" url {},error :{} ", page.getUrl().toString(), e);
                beiDanMatchList = new ArrayList<>();
            }
            log.info(" 北单分析 >>>>>>>{} ,result:{} ", page.getUrl().toString(), JSON.toJSONString(beiDanMatchList));
            page.putField("beiDanMatchList", beiDanMatchList);
        } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL18)) {
            //胜负彩比赛
            List<WinBurdenMatchDO> winBurdenMatchList = new ArrayList<>();
            try {
                String deadlineTime = html.css(".zcfilter-l .zcfilter-endtime", "text").toString();
                if (StringUtils.isBlank(deadlineTime)) {
                    deadlineTime = html.css(".zcfilter-l span", "text").toString();
                }
                if (StringUtils.isBlank(deadlineTime)) {
                    page.putField("winBurdenMatchList", winBurdenMatchList);
                    return;
                }
                deadlineTime = deadlineTime.replaceAll("官方售彩已截止", "").trim();
                deadlineTime = deadlineTime.substring(deadlineTime.indexOf("：") + 1);
                deadlineTime = DateUtil.year(DateUtil.date()) + "-" + deadlineTime + ":00";
                String issueNo = html.xpath("/html/body/div[6]/div/div[2]/div[1]/div/ul/li[1]/@data-expect").toString();
                List<Selectable> nodes = html.css(".bet-tb-dg .bet-tb-tr").nodes();
                for (int i = 0; i < nodes.size(); i++) {
                    WinBurdenMatchDO winBurdenMatch = new WinBurdenMatchDO();
                    winBurdenMatch.setNumber(nodes.get(i).css(".td-no", "text").toString());
                    winBurdenMatch.setMatch(nodes.get(i).css(".td-evt a", "text").toString());
                    String color = nodes.get(i).xpath("//*[@class='td-evt']/a/@style").toString();
                    winBurdenMatch.setColor(color.substring(color.indexOf("#"), color.length() - 1));
                    winBurdenMatch.setOpenTime(nodes.get(i).css(".td-endtime", "text").toString());
                    winBurdenMatch.setHomeTeam(nodes.get(i).css(".team-l i", "text").toString() + nodes.get(i).css(".td-team .team-l a", "text").toString().replaceAll(" ", ""));
                    winBurdenMatch.setVisitingTeam(nodes.get(i).css(".team-r i", "text").toString() + nodes.get(i).css(".td-team .team-r a", "text").toString().replaceAll(" ", ""));
                    winBurdenMatch.setNotLetOdds(StrUtil.join(",", nodes.get(i).css(".td-pei span", "text").all()).replaceAll(",↑", "").replaceAll(",↓", ""));
                    winBurdenMatch.setDeadline(DateUtil.parse(deadlineTime));
                    winBurdenMatch.setIssueNo(issueNo);
                    winBurdenMatch.setCreateTime(new Date());
                    winBurdenMatch.setUpdateTime(new Date());
                    winBurdenMatch.setGameNo(issueNo + RacingBallServiceImpl.fillZero(winBurdenMatch.getNumber(), 3));
                    winBurdenMatchList.add(winBurdenMatch);
                }
            } catch (Exception e) {
                log.error(" url {},error :{} ", page.getUrl().toString(), e);
            }
            log.info(" 胜负彩比赛 >>>>>>>{} ,result:{} ", page.getUrl().toString(), JSON.toJSONString(winBurdenMatchList));
            page.putField("winBurdenMatchList", winBurdenMatchList);
        } else if (page.getUrl().toString().startsWith(CrawlingAddressConstant.URL18_01)) {
            List<WinBurdenMatchDO> winBurdenMatchList = new ArrayList<>();
            try {
                String issueNo = page.getUrl().toString().split("=")[1];
                //胜负彩比赛 下一期
                String deadlineTime = html.css(".zcfilter-endtime", "text").toString();
                if (StringUtils.isBlank(deadlineTime)) {
                    deadlineTime = html.css(".zcfilter-l span", "text").toString();
                }
                if (StringUtils.isBlank(deadlineTime)) {
                    page.putField("winBurdenMatchList", winBurdenMatchList);
                    return;
                }
                deadlineTime = deadlineTime.replaceAll("官方售彩已截止", "").trim();
                deadlineTime = deadlineTime.substring(deadlineTime.indexOf("：") + 1);
                deadlineTime = DateUtil.year(DateUtil.date()) + "-" + deadlineTime + ":00";
                //String issueNo = html.xpath("/html/body/div[6]/div/div[2]/div[1]/div/ul/li[1]/@data-expect").toString();
                List<Selectable> nodes = html.css(".bet-tb-dg .bet-tb-tr").nodes();
                for (int i = 0; i < nodes.size(); i++) {
                    WinBurdenMatchDO winBurdenMatch = new WinBurdenMatchDO();
                    winBurdenMatch.setNumber(nodes.get(i).css(".td-no", "text").toString());
                    winBurdenMatch.setMatch(nodes.get(i).css(".td-evt a", "text").toString());
                    String color = nodes.get(i).xpath("//*[@class='td-evt']/a/@style").toString();
                    winBurdenMatch.setColor(color.substring(color.indexOf("#"), color.length() - 1));
                    winBurdenMatch.setOpenTime(nodes.get(i).css(".td-endtime", "text").toString());
                    winBurdenMatch.setHomeTeam(nodes.get(i).css(".team-l i", "text").toString() + nodes.get(i).css(".td-team .team-l a", "text").toString().replaceAll(" ", ""));
                    winBurdenMatch.setVisitingTeam(nodes.get(i).css(".team-r i", "text").toString() + nodes.get(i).css(".td-team .team-r a", "text").toString().replaceAll(" ", ""));
                    winBurdenMatch.setNotLetOdds(StrUtil.join(",", nodes.get(i).css(".td-pei span", "text").all()).replaceAll(",↑", "").replaceAll(",↓", ""));
                    winBurdenMatch.setDeadline(DateUtil.parse(deadlineTime));
                    winBurdenMatch.setIssueNo(issueNo);
                    winBurdenMatch.setCreateTime(new Date());
                    winBurdenMatch.setUpdateTime(new Date());
                    winBurdenMatch.setGameNo(issueNo + RacingBallServiceImpl.fillZero(winBurdenMatch.getNumber(), 3));
                    winBurdenMatchList.add(winBurdenMatch);
                }
            } catch (Exception e) {
                log.error(" url {},error :{} ", page.getUrl().toString(), e);
            }
            log.info(" 胜负彩比赛 >>>>>>>{} ,result:{} ", page.getUrl().toString(), JSON.toJSONString(winBurdenMatchList));
            page.putField("winBurdenMatchList", winBurdenMatchList);
        } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL19)) {
            //胜负彩开奖
            List<WinBurdenMatchDO> winBurdenMatchList = new ArrayList<>();
            try {
                String issueNo = html.css(".cfont2 strong", "text").toString();
                List<Selectable> nodes = html.css(".div_shupai").nodes();
                int idx = 1;
                for (Selectable node : nodes) {
                    WinBurdenMatchDO winBurdenMatch = new WinBurdenMatchDO();
                    winBurdenMatch.setIssueNo(issueNo);
                    winBurdenMatch.setHomeTeam(node.css(".div_shupai", "text").toString().replaceAll(" ", ""));
                    String str = html.xpath("/html/body/div[6]/div[3]/div[2]/div[1]/div[2]/table[1]/tbody/tr[3]/td[" + idx++ + "]/span/text()").toString();
                    if ("3".equals(str)) {
                        winBurdenMatch.setAward("胜");
                    } else if ("1".equals(str)) {
                        winBurdenMatch.setAward("平");
                    } else if ("0".equals(str)) {
                        winBurdenMatch.setAward("负");
                    } else {
                        winBurdenMatch.setAward("-");
                    }
                    winBurdenMatch.setMoneyAward(html.xpath("/html/body/div[6]/div[3]/div[2]/div[1]/div[2]/table[2]/tbody/tr[3]/td[3]/text()").toString().replaceAll(",", "") + "," + html.xpath("/html/body/div[6]/div[3]/div[2]/div[1]/div[2]/table[2]/tbody/tr[4]/td[3]/text()").toString().replaceAll(",", "") + "," + html.xpath("/html/body/div[6]/div[3]/div[2]/div[1]/div[2]/table[2]/tbody/tr[5]/td[3]/text()").toString().replaceAll(",", ""));
                    winBurdenMatchList.add(winBurdenMatch);
                }
            } catch (Exception e) {
                log.error(" url {},error :{} ", page.getUrl().toString(), e);
            }
            log.info(" 胜负彩开奖 >>>>>>>{} ,result:{} ", page.getUrl().toString(), JSON.toJSONString(winBurdenMatchList));
            page.putField("winBurdenMatchList", winBurdenMatchList);
        } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL21) || ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL22) || ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL23) || ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL24)) {
            List<OmitDO> omitList = new ArrayList<>();
            try {
                OmitDO omit = new OmitDO();
                List<String> list = new ArrayList<>();
                List<Selectable> nodes;
                if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL21)) {
                    omit.setType(LotteryOrderTypeEnum.ARRAY.getKey());
                    nodes = html.css(".c-tab .tab_ide  #zx_bzxh .plw-main .plw-num .plw-m li em.num-yl").nodes();
                    list = getNodeText(nodes);
                } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL22)) {
                    omit.setType(LotteryOrderTypeEnum.ARRANGE.getKey());
                    nodes = html.css("#basicbet .plw-main .plw-num .plw-m li em.num-yl").nodes();
                    list = getNodeText(nodes);
                } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL23)) {
                    omit.setType(LotteryOrderTypeEnum.SEVEN_STAR.getKey());
                    nodes = html.css(".c-tab .plw-main .plw-num .plw-m li em.num-yl").nodes();
                    list = getNodeText(nodes);
                } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL24)) {
                    omit.setType(LotteryOrderTypeEnum.GRAND_LOTTO.getKey());
                    nodes = html.css("#pttz .c-select .c-s-l .c-num li em.num-yl").nodes();
                    list.addAll(getNodeText(nodes));
                    nodes = html.css("#pttz .c-select .c-s-r .c-num li em.num-yl").nodes();
                    list.addAll(getNodeText(nodes));
                }
                omit.setRecord(StrUtil.join(",", list));
                omitList.add(omit);
            } catch (Exception e) {
                log.error(" url {},error :{} ", page.getUrl().toString(), e);
                omitList = new ArrayList<>();
            }
            log.info(" 排列号码遗漏 >>>>>>>{} ,result:{} ", page.getUrl().toString(), JSON.toJSONString(omitList));
            page.putField("omitList", omitList);
        } else if (ObjectUtil.equal(page.getUrl().toString(), CrawlingAddressConstant.URL_BD_SFGG)) {
            List<BeiDanSFGGMatchDO> beiDanSfggMatchList = new ArrayList<>();
            //北单胜负过关。
            //期号

            //*[@id="bet_content"]
            // List<Selectable> divNodes = html.css("//*[@id='bet_content']/div").nodes();
            List<Selectable> tableNodes = html.css(".bet_table").nodes();
            int idx = 0;
            for (Selectable node : tableNodes) {

                //获取当天的赛事列表
                List<Selectable> trNodes = node.xpath("tr").nodes();

                for (Selectable trNode : trNodes) {

                    List<Selectable> tdNodes = trNode.xpath("td").nodes();
                    BeiDanSFGGMatchDO match = new BeiDanSFGGMatchDO();
                    //让球
                    String issueNo = trNode.xpath("//tr/@pdate").get();
                    String mid = trNode.xpath("//tr/@mid").get();
                    String gdate = trNode.xpath("//tr/@gdate").get();
                    try {
                        String letBall = trNode.xpath("//tr/@rq").get();
                        String matchType = trNode.xpath("//tr/@matchtype").get();
                        String unionMatch = trNode.xpath("//tr/@lg").get().replaceAll(matchType + "-", "");//足球-亚冠联赛
                        String endTime = trNode.xpath("//tr/@pendtime").get();//开赛时间加10分钟 2023-11-28 17:50

                        String bg = tdNodes.get(2).xpath("//span/@style").get().replaceAll("background:", "").trim();
                        //主队
                        List<Selectable> teamNodes = tdNodes.get(4).xpath("//span[@class='odds_item']").nodes();
                        String homeWinOdds = teamNodes.get(0).xpath("//span/@data-sp-ori").get();
                        String homeTeam = teamNodes.get(0).xpath("//span[@class='gray']/text()").get() + teamNodes.get(0).xpath("//span[@class='item_left']/text()").get();

                        String homeOdds = teamNodes.get(0).xpath("//span[@class='js-sp']/text()").get();

                        String visitWinOdds = teamNodes.get(1).xpath("//span/@data-sp-ori").get();
                        String visitTeam = teamNodes.get(1).xpath("//span/text()").get() + teamNodes.get(1).xpath("//span[@class='gray']/text()").get();
                        //visitTeam 需要处理下 1.57  佩特罗鲁 [6]
                        String visitOdds = teamNodes.get(1).xpath("//span[@class='js-sp']/text()").get();

                        String score = tdNodes.get(5).xpath("//a/text()").get();
                        match.setState("0");
                        boolean isScore = (StringUtils.isNotBlank(score) && score.matches("^\\d+:\\d+$")) ? true : false;
                        if (StringUtils.isNotBlank(score) && (isScore) && (StringUtils.isNotBlank(homeWinOdds) || StringUtils.isNotBlank(visitWinOdds))) {
                            String boundsOdds = homeWinOdds + "," + visitOdds;
                            match.setBonusOdds(boundsOdds);
                            //已经开奖
                            match.setState("1");
                            if (StringUtils.isNotBlank(homeWinOdds)) {
                                match.setAward("胜");
                            } else if (StringUtils.isNotBlank(visitWinOdds)) {
                                match.setAward("负");
                            }
                        }
                        if (isScore) {
                            match.setHalfFullCourt(score);
                        }
                        match.setHostWinOdds(homeOdds);
                        match.setVisitWinOdds(visitOdds);
                        match.setStartTime(gdate);
                        try {
                            match.setDeadline(DateUtils.addMinutes(DateUtils.parseDate(endTime, "yyyy-MM-dd HH:mm"), 10));
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }

                        match.setLetBall(letBall);
                        match.setColor(bg);
                        match.setUnionMatch(unionMatch);
                        match.setCreateTime(new Date());
                        match.setIssueNo(issueNo);
                        match.setGameNo(issueNo + RacingBallServiceImpl.fillZero(mid, 4));
                        match.setMatch(matchType);
                        match.setHomeTeam(homeTeam.trim());
                        match.setNumber(mid);
                        match.setVisitingTeam(visitTeam.trim());
                        beiDanSfggMatchList.add(match);
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error(" 北单胜负过关对阵>>>>>{} {}", issueNo, mid);
                    }
                }
            }
            log.info(" 北单胜负过关对阵 >>>>>>>{} ,result:{} ", page.getUrl().toString(), JSON.toJSONString(beiDanSfggMatchList));
            page.putField("sfggList", beiDanSfggMatchList);
        }
    }

    private List<String> getNodeText(List<Selectable> nodes) {
        List<String> list = new ArrayList<>();
        for (Selectable node : nodes) {
            String str = node.css(".num-yl", "text").toString();
            if (str.equals("")) {
                str = node.css(".num-yl span", "text").toString();
            }
            list.add(str);
        }
        return list;
    }

    @Override
    public Site getSite() {
        return site;
    }


}
