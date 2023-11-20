package com.qihang.common.util.reward;


import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.qihang.domain.permutation.PermutationAwardDO;
import com.qihang.enumeration.order.lottery.LotteryOrderTypeEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Stream;

public class DigitBallAwardUtils {

    private static ThreadFactory factory = ThreadFactoryBuilder.create().setDaemon(true).setNamePrefix("兑奖程序-").build();
    public static ExecutorService threadPool = new ThreadPoolExecutor(4, 10, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10000), factory, new ThreadPoolExecutor.CallerRunsPolicy());

    public static void award(List<DigitBall> balls, PermutationAwardDO award) {
        if (LotteryOrderTypeEnum.FCKL8.getKey().equals(award.getType())) {
            DigitBallAwardUtils.kl8(balls, award);
        } else if (LotteryOrderTypeEnum.FCQLC.getKey().equals(award.getType())) {
            DigitBallAwardUtils.qlc(balls, award);
        } else if (LotteryOrderTypeEnum.FCSSQ.getKey().equals(award.getType())) {
            DigitBallAwardUtils.ssq(balls, award);
        } else if (LotteryOrderTypeEnum.FC3D.getKey().equals(award.getType())) {
            DigitBallAwardUtils.fc3d(balls, award);
        } else if (LotteryOrderTypeEnum.ARRAY.getKey().equals(award.getType())) {
            DigitBallAwardUtils.pl3(balls, award);
        } else if (LotteryOrderTypeEnum.ARRANGE.getKey().equals(award.getType())) {
            DigitBallAwardUtils.pl5(balls, award);
        } else if (LotteryOrderTypeEnum.GRAND_LOTTO.getKey().equals(award.getType())) {
            DigitBallAwardUtils.dlt(balls, award);
        } else if (LotteryOrderTypeEnum.SEVEN_STAR.getKey().equals(award.getType())) {
            DigitBallAwardUtils.qxc(balls, award);
        }
    }

    private static List<DigitBall> kl8(List<DigitBall> bets, PermutationAwardDO award) {
        String[] awardNumber = StringUtils.splitByWholeSeparatorPreserveAllTokens(award.getReward(), ",");
        int ge = 0;
        for (DigitBall ball : bets) {
            //默认不中
            ball.setAwardContent("");
            ball.setAward(false);
            ball.setMoney("");

            String awardContent = "";
            List<String> ballsList = new ArrayList<>(Arrays.asList(StringUtils.splitByWholeSeparatorPreserveAllTokens(ball.getContent(), ",")));
            for (int i = 0; i < awardNumber.length; i++) {
                if (ballsList.contains(awardNumber[i])) {
                    ge++;
                    awardContent += awardNumber[i] + ",";
                }
            }
            String key = award.getType() + "-选" + ball.getMode() + "中" + ge;
            Double money = GrandLottoUtil.awardLevelMap.get(key);
            String level = "选" + ball.getMode() + "中" + ge;
            switch (ball.getMode()) {
                case "10":
                    //选10
                    switch (ge) {
                        case 10:
                        case 9:
                        case 8:
                        case 7:
                        case 6:
                        case 5:
                            ball.setLevel(level);
                            ball.setAward(true);
                            ball.setAwardContent(awardContent);
                            ball.setMoney(money.toString());
                            break;
                        case 0:
                            ball.setLevel("选" + ball.mode + "不中");
                            ball.setAward(true);
                            ball.setAwardContent(ball.getContent());
                            ball.setMoney(money.toString());
                            break;

                    }
                    break;
                case "9":
                    switch (ge) {
                        case 9:
                        case 8:
                        case 7:
                        case 6:
                        case 5:
                        case 4:
                            ball.setLevel(level);
                            ball.setAward(true);
                            ball.setAwardContent(awardContent);
                            ball.setMoney(money.toString());
                            break;
                        case 0:
                            ball.setLevel("选" + ball.mode + "不中");
                            ball.setAward(true);
                            ball.setAwardContent(ball.getContent());
                            ball.setMoney(money.toString());
                            break;
                    }
                    break;

                case "8":
                    switch (ge) {
                        case 8:
                        case 7:
                        case 6:
                        case 5:
                        case 4:
                            ball.setLevel(level);
                            ball.setAward(true);
                            ball.setAwardContent(awardContent);
                            ball.setMoney(money.toString());
                            break;
                        case 0:
                            ball.setLevel("选" + ball.mode + "不中");
                            ball.setAward(true);
                            ball.setAwardContent(ball.getContent());
                            ball.setMoney(money.toString());
                            break;
                    }
                    break;
                case "7":
                    switch (ge) {
                        case 7:
                        case 6:
                        case 5:
                        case 4:
                            ball.setLevel(level);
                            ball.setAward(true);
                            ball.setAwardContent(awardContent);
                            ball.setMoney(money.toString());
                            break;
                        case 0:
                            ball.setLevel("选" + ball.mode + "不中");
                            ball.setAward(true);
                            ball.setAwardContent(ball.getContent());
                            ball.setMoney(money.toString());
                            break;
                    }
                    break;
                case "6":
                    switch (ge) {
                        case 6:
                        case 5:
                        case 4:
                            ball.setLevel(level);
                            ball.setAward(true);
                            ball.setAwardContent(awardContent);
                            ball.setMoney(money.toString());
                            break;
                    }
                    break;
                case "5":
                    switch (ge) {
                        case 5:
                        case 4:
                        case 3:
                            ball.setLevel(level);
                            ball.setAward(true);
                            ball.setAwardContent(awardContent);
                            ball.setMoney(money.toString());
                            break;
                    }
                    break;
                case "4":
                    switch (ge) {
                        case 4:
                        case 3:
                        case 2:
                            ball.setLevel(level);
                            ball.setAward(true);
                            ball.setAwardContent(awardContent);
                            ball.setMoney(money.toString());
                            break;
                    }
                    break;
                case "3":
                    switch (ge) {
                        case 3:
                        case 2:
                            ball.setLevel(level);
                            ball.setAward(true);
                            ball.setAwardContent(awardContent);
                            ball.setMoney(money.toString());
                            break;
                    }
                    break;
                case "2":
                    switch (ge) {
                        case 2:
                            ball.setLevel(level);
                            ball.setAward(true);
                            ball.setAwardContent(awardContent);
                            ball.setMoney(money.toString());
                            break;
                    }
                    break;
                case "1":
                    switch (ge) {
                        case 1:
                            ball.setLevel(level);
                            ball.setAward(true);
                            ball.setAwardContent(awardContent);
                            ball.setMoney(money.toString());
                            break;
                    }
                    break;
            }
        }
        return bets;
    }

    private static List<DigitBall> qlc(List<DigitBall> bets, PermutationAwardDO award) {
        String[] awardNubmer = StringUtils.splitByWholeSeparatorPreserveAllTokens(award.getReward(), ",");//7+1
        String[] awardMoney = StringUtils.splitByWholeSeparatorPreserveAllTokens(award.getMoneyAward(), ",");
        String blueNumber = awardNubmer[7];

        int awardCount = 0;
        for (DigitBall bet : bets) {
            String awardContent = "";
            bet.setAward(false);
            bet.setMoney("");
            bet.setAwardContent("");
            bet.setLevel("");
            List<String> betNumbers = new ArrayList<>(Arrays.asList(StringUtils.splitByWholeSeparatorPreserveAllTokens(bet.getContent(), ",")));
            boolean blueAward = betNumbers.contains(blueNumber);
            //一等
            for (int i = 0; i < 7; i++) {
                if (betNumbers.contains(awardNubmer[i])) {
                    awardCount++;
                    awardContent += awardNubmer[i] + ",";
                }
            }
            if (awardCount == 7) {
                bet.setLevel("一等奖");
                bet.setAward(true);
                bet.setMoney(awardMoney[0]);
                bet.setAwardContent(awardContent);
            } else if (awardCount == 6 && blueAward) {
                bet.setLevel("二等奖");
                bet.setAward(true);
                bet.setMoney(awardMoney[0]);
                bet.setAwardContent(awardContent);
            } else if (awardCount == 6) {
                bet.setLevel("三等奖");
                bet.setAward(true);
                bet.setMoney(awardMoney[0]);
                bet.setAwardContent(awardContent);
            } else if (awardCount == 5 && blueAward) {
                bet.setLevel("四等奖");
                bet.setAward(true);
                bet.setMoney("200");
                bet.setAwardContent(awardContent);
            } else if (awardCount == 5) {
                bet.setLevel("五等奖");
                bet.setAward(true);
                bet.setMoney("50");
                bet.setAwardContent(awardContent);
            } else if (awardCount == 4 && blueAward) {
                bet.setLevel("六等奖");
                bet.setAward(true);
                bet.setMoney("10");
                bet.setAwardContent(awardContent);
            } else if (awardCount == 4) {
                bet.setLevel("七等奖");
                bet.setAward(true);
                bet.setMoney("5");
                bet.setAwardContent(awardContent);
            }
        }
        return bets;
    }

    private static List<DigitBall> ssq(List<DigitBall> bets, PermutationAwardDO award) {
        String[] awardNubmer = StringUtils.splitByWholeSeparatorPreserveAllTokens(award.getReward(), ",");//6+1
        String[] awardMoney = StringUtils.splitByWholeSeparatorPreserveAllTokens(award.getMoneyAward(), ",");
        String blueNumber = awardNubmer[6];

        int awardCount = 0;
        for (DigitBall bet : bets) {
            String awardContent = "";
            bet.setAward(false);
            bet.setMoney("");
            bet.setAwardContent("");
            bet.setLevel("");
            String[] betNum = StringUtils.splitByWholeSeparatorPreserveAllTokens(bet.getContent(), ",");
            String betBlue = betNum[6];
            //排除最后蓝球
            List<String> betNumbers = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                betNumbers.add(betNum[i]);
            }
            boolean blueAward = betBlue.equals(blueNumber);
            //一等
            for (int i = 0; i < 6; i++) {
                if (betNumbers.contains(awardNubmer[i])) {
                    awardCount++;
                    awardContent += awardNubmer[i] + ",";
                }
            }
            if (awardCount == 6 && blueAward) {
                bet.setLevel("一等奖");
                bet.setAward(true);
                bet.setMoney(awardMoney[0]);
                bet.setAwardContent(awardContent);
            } else if (awardCount == 6) {
                bet.setLevel("二等奖");
                bet.setAward(true);
                bet.setMoney(awardMoney[0]);
                bet.setAwardContent(awardContent);
            } else if (awardCount == 5 && blueAward) {
                bet.setLevel("三等奖");
                bet.setAward(true);
                bet.setMoney("3000");
                bet.setAwardContent(awardContent);
            } else if (awardCount == 5 || (awardCount == 4 && blueAward)) {
                bet.setLevel("四等奖");
                bet.setAward(true);
                bet.setMoney("200");
                bet.setAwardContent(awardContent);
            } else if (awardCount == 4 || (awardCount == 3 && blueAward)) {
                bet.setLevel("五等奖");
                bet.setAward(true);
                bet.setMoney("10");
                bet.setAwardContent(awardContent);
            } else if (blueAward) {
                bet.setLevel("六等奖");
                bet.setAward(true);
                bet.setMoney("5");
                bet.setAwardContent(betBlue);
            }
        }
        return bets;
    }

    /*
        0 "直选"
		  1 "组三"
	 2 "组六"
	  3  "直选和值"
	  4 "组选和值"
		  5 "组三单式"

     */
    private static List<DigitBall> pl3(List<DigitBall> bets, PermutationAwardDO award) {
        String[] awardNumber = StringUtils.splitByWholeSeparatorPreserveAllTokens(award.getReward(), ",");
        for (DigitBall bet : bets) {
            String awardContent = "";
            bet.setAward(false);
            bet.setMoney("");
            bet.setAwardContent("");
            bet.setLevel("");

            int awardCounts = 0;
            String[] betContents = StringUtils.splitByWholeSeparatorPreserveAllTokens(bet.getContent(), ",");
            switch (bet.getMode()) {
                case "0":
                    for (int i = 0; i < 3; i++) {
                        if (betContents[i].equals(awardNumber[i])) {
                            awardCounts++;
                            awardContent += awardNumber[i] + ",";
                        }
                    }
                    if (awardCounts == 3) {
                        bet.setAward(true);
                        bet.setMoney("1024");
                        bet.setAwardContent(awardContent);
                        bet.setLevel("直选");
                    }
                    break;
                case "1":
                    //排序
                    Arrays.sort(betContents);
                    Arrays.sort(awardNumber);
                    for (int i = 0; i < 3; i++) {
                        if (betContents[i].equals(awardNumber[i])) {
                            awardCounts++;
                            awardContent += awardNumber[i] + ",";
                        }
                    }
                    boolean zs = false;
                    if ((awardNumber[0].equals(awardNumber[1]) && !awardNumber[1].equals(awardNumber[2]))
                            || (awardNumber[1].equals(awardNumber[2]) && !awardNumber[0].equals(awardNumber[1]))
                            || (awardNumber[0].equals(awardNumber[2]) && !awardNumber[1].equals(awardNumber[2]))) {
                        zs = true;
                    }
                    if (awardCounts == 3 && zs) {
                        bet.setAward(true);
                        bet.setMoney("346");
                        bet.setAwardContent(awardContent);
                        bet.setLevel("组三");
                    }
                    break;
                case "2":
                    //排序
                    Arrays.sort(betContents);
                    Arrays.sort(awardNumber);
                    for (int i = 0; i < 3; i++) {
                        if (betContents[i].equals(awardNumber[i])) {
                            awardCounts++;
                            awardContent += awardNumber[i] + ",";
                        }
                    }
                    boolean z6 = false;
                    if (!awardNumber[0].equals(awardNumber[1]) && !awardNumber[1].equals(awardNumber[2]) && !awardNumber[0].equals(awardNumber[2])) {
                        z6 = true;
                    }
                    if (awardCounts == 3 && z6) {
                        bet.setAward(true);
                        bet.setMoney("173");
                        bet.setAwardContent(awardContent);
                        bet.setLevel("组六");
                    }
                    break;
                case "3":
                    int count = Stream.of(awardNumber).mapToInt(item -> Integer.valueOf(item)).sum();
                    for (String s : betContents) {
                        if (s.equals(String.valueOf(count))) {
                            awardCounts++;
                            awardContent += s + ",";
                        }
                    }
                    if (awardCounts > 0) {
                        bet.setAward(true);
                        bet.setMoney(String.valueOf(awardCounts * 1024));
                        bet.setAwardContent(awardContent);
                        bet.setLevel("直选和值");
                    }
                    break;
                case "4":
                    count = Stream.of(awardNumber).mapToInt(item -> Integer.valueOf(item)).sum();
                    for (String s : betContents) {
                        if (s.equals(String.valueOf(count))) {
                            awardCounts++;
                            awardContent += s + ",";
                        }
                    }
                    zs = false;
                    if ((awardNumber[0].equals(awardNumber[1]) && !awardNumber[1].equals(awardNumber[2]))
                            || (awardNumber[1].equals(awardNumber[2]) && !awardNumber[0].equals(awardNumber[1]))
                            || (awardNumber[0].equals(awardNumber[2]) && !awardNumber[1].equals(awardNumber[2]))) {
                        zs = true;
                    }
                    z6 = false;
                    if (!awardNumber[0].equals(awardNumber[1]) && !awardNumber[1].equals(awardNumber[2]) && !awardNumber[0].equals(awardNumber[2])) {
                        z6 = true;
                    }
                    if (awardCounts > 0 && (zs || z6)) {
                        bet.setAward(true);
                        bet.setMoney(String.valueOf(awardCounts * (zs ? 346 : 173)));
                        bet.setAwardContent(awardContent);
                        bet.setLevel("组选和值");
                    }
                    break;
                case "5":
                    //排序
                    Arrays.sort(betContents);
                    Arrays.sort(awardNumber);
                    for (int i = 0; i < 3; i++) {
                        if (betContents[i].equals(awardNumber[i])) {
                            awardCounts++;
                            awardContent += awardNumber[i] + ",";
                        }
                    }
                    zs = false;
                    if ((awardNumber[0].equals(awardNumber[1]) && !awardNumber[1].equals(awardNumber[2]))
                            || (awardNumber[1].equals(awardNumber[2]) && !awardNumber[0].equals(awardNumber[1]))
                            || (awardNumber[0].equals(awardNumber[2]) && !awardNumber[1].equals(awardNumber[2]))) {
                        zs = true;
                    }
                    if (awardCounts > 0 && zs) {
                        bet.setAward(true);
                        bet.setMoney(String.valueOf(awardCounts * 346));
                        bet.setAwardContent(awardContent);
                        bet.setLevel("组三");
                    }
                    break;
            }

        }
        return bets;
    }

    /*
     0 直选 1 组三，2 组六
    * */
    private static List<DigitBall> fc3d(List<DigitBall> bets, PermutationAwardDO award) {
        String[] awardNumber = StringUtils.splitByWholeSeparatorPreserveAllTokens(award.getReward(), ",");
        for (DigitBall bet : bets) {
            String awardContent = "";
            bet.setAward(false);
            bet.setMoney("");
            bet.setAwardContent("");
            bet.setLevel("");

            int awardCounts = 0;
            String[] betContents = StringUtils.splitByWholeSeparatorPreserveAllTokens(bet.getContent(), ",");
            switch (bet.getMode()) {
                case "0":
                    for (int i = 0; i < 3; i++) {
                        if (betContents[i].equals(awardNumber[i])) {
                            awardCounts++;
                            awardContent += awardNumber[i] + ",";
                        }
                    }
                    if (awardCounts == 3) {
                        bet.setAward(true);
                        bet.setMoney("1024");
                        bet.setAwardContent(awardContent);
                        bet.setLevel("直选");
                    }
                    break;
                case "1":
                    //排序
                    Arrays.sort(betContents);
                    Arrays.sort(awardNumber);
                    for (int i = 0; i < 3; i++) {
                        if (betContents[i].equals(awardNumber[i])) {
                            awardCounts++;
                            awardContent += awardNumber[i] + ",";
                        }
                    }
                    boolean zs = false;
                    if ((awardNumber[0].equals(awardNumber[1]) && !awardNumber[1].equals(awardNumber[2]))
                            || (awardNumber[1].equals(awardNumber[2]) && !awardNumber[0].equals(awardNumber[1]))
                            || (awardNumber[0].equals(awardNumber[2]) && !awardNumber[1].equals(awardNumber[2]))) {
                        zs = true;
                    }
                    if (awardCounts == 3 && zs) {
                        bet.setAward(true);
                        bet.setMoney("346");
                        bet.setAwardContent(awardContent);
                        bet.setLevel("组三");
                    }
                    break;
                case "2":
                    //排序
                    Arrays.sort(betContents);
                    Arrays.sort(awardNumber);
                    for (int i = 0; i < 3; i++) {
                        if (betContents[i].equals(awardNumber[i])) {
                            awardCounts++;
                            awardContent += awardNumber[i] + ",";
                        }
                    }
                    boolean z6 = false;
                    if (!awardNumber[0].equals(awardNumber[1]) && !awardNumber[1].equals(awardNumber[2]) && !awardNumber[0].equals(awardNumber[2])) {
                        z6 = true;
                    }
                    if (awardCounts == 3 && z6) {
                        bet.setAward(true);
                        bet.setMoney("173");
                        bet.setAwardContent(awardContent);
                        bet.setLevel("组六");
                    }
                    break;
            }

        }
        return bets;
    }

    private static List<DigitBall> pl5(List<DigitBall> bets, PermutationAwardDO award) {
        String[] awardNumber = StringUtils.splitByWholeSeparatorPreserveAllTokens(award.getReward(), ",");
        for (DigitBall bet : bets) {
            String awardContent = "";
            bet.setAward(false);
            bet.setMoney("");
            bet.setAwardContent("");
            bet.setLevel("");

            int awardCounts = 0;
            String[] betContents = StringUtils.splitByWholeSeparatorPreserveAllTokens(bet.getContent(), ",");
            switch (bet.getMode()) {
                case "0":
                    for (int i = 0; i < 5; i++) {
                        if (betContents[i].equals(awardNumber[i])) {
                            awardCounts++;
                            awardContent += awardNumber[i] + ",";
                        }
                    }
                    if (awardCounts == 5) {
                        bet.setAward(true);
                        bet.setMoney("100000");
                        bet.setAwardContent(awardContent);
                        bet.setLevel("直选");
                    }
                    break;
            }

        }
        return bets;
    }

    private static List<DigitBall> dlt(List<DigitBall> bets, PermutationAwardDO award) {
        String[] awardNubmer = StringUtils.splitByWholeSeparatorPreserveAllTokens(award.getReward(), ",");//5+2
        String[] awardMoney = StringUtils.splitByWholeSeparatorPreserveAllTokens(award.getMoneyAward(), ",");
        String blueNumber = awardNubmer[5];
        String blueNumber2 = awardNubmer[6];

        int awardCount = 0;
        for (DigitBall bet : bets) {
            String awardContent = "";
            bet.setAward(false);
            bet.setMoney("");
            bet.setAwardContent("");
            bet.setLevel("");
            String[] betNum = StringUtils.splitByWholeSeparatorPreserveAllTokens(bet.getContent(), ",");
            String betBlue = betNum[5];
            String betBlue2 = betNum[6];
            //排除最后蓝球
            List<String> betNumbers = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                betNumbers.add(betNum[i]);
            }
            boolean blueAward = betBlue.equals(blueNumber);
            boolean bludAward2 = betBlue2.equals(blueNumber2);
            //一等
            for (int i = 0; i < 5; i++) {
                if (betNumbers.contains(awardNubmer[i])) {
                    awardCount++;
                    awardContent += awardNubmer[i] + ",";
                }
            }
            if (awardCount == 5 && blueAward && bludAward2) {
                bet.setLevel("一等奖");
                bet.setAward(true);
                bet.setMoney(awardMoney[0]);
                bet.setAwardContent(awardContent + "+" + blueNumber + "," + blueNumber2);
            } else if (awardCount == 5 && (blueAward | bludAward2)) {
                bet.setLevel("二等奖");
                bet.setAward(true);
                bet.setMoney(awardMoney[0]);
                bet.setAwardContent(awardContent + "+" + (blueAward ? betBlue : betBlue2));
            } else if (awardCount == 5) {
                bet.setLevel("三等奖");
                bet.setAward(true);
                bet.setMoney("10000");
                bet.setAwardContent(awardContent);
            } else if (awardCount == 4 && bludAward2 && blueAward) {
                bet.setLevel("四等奖");
                bet.setAward(true);
                bet.setMoney("3000");
                bet.setAwardContent(awardContent + "+" + blueNumber + "," + blueNumber2);
            } else if (awardCount == 4 && (bludAward2 || blueAward)) {
                bet.setLevel("五等奖");
                bet.setAward(true);
                bet.setMoney("300");
                bet.setAwardContent(awardContent + "+" + (blueAward ? betBlue : betBlue2));
            } else if (awardCount == 3 && (bludAward2 && blueAward)) {
                bet.setLevel("六等奖");
                bet.setAward(true);
                bet.setMoney("200");
                bet.setAwardContent(awardContent + "+" + blueNumber + "," + blueNumber2);
            } else if (awardCount == 4) {
                bet.setLevel("七等奖");
                bet.setAward(true);
                bet.setMoney("100");
                bet.setAwardContent(awardContent);
            } else if (awardCount == 3 && (bludAward2 || blueAward)) {
                bet.setLevel("八等奖");
                bet.setAward(true);
                bet.setMoney("15");
                bet.setAwardContent(awardContent + "+" + (blueAward ? betBlue : betBlue2));
            } else if (awardCount == 3) {
                bet.setLevel("九等奖");
                bet.setAward(true);
                bet.setMoney("5");
                bet.setAwardContent(awardContent);
            } else if (awardCount == 1 && bludAward2 && blueAward) {
                bet.setLevel("九等奖");
                bet.setAward(true);
                bet.setMoney("5");
                bet.setAwardContent(awardContent + "+" + blueNumber + "," + blueNumber2);
            } else if (awardCount == 2 && (bludAward2 || blueAward)) {
                bet.setLevel("九等奖");
                bet.setAward(true);
                bet.setMoney("5");
                bet.setAwardContent(awardContent + "+" + (blueAward ? betBlue : betBlue2));
            } else if ((bludAward2 && blueAward)) {
                bet.setLevel("九等奖");
                bet.setAward(true);
                bet.setMoney("5");
                bet.setAwardContent("+" + blueNumber + "," + blueNumber2);
            }
        }
        return bets;
    }

    private static List<DigitBall> qxc(List<DigitBall> bets, PermutationAwardDO award) {
        String[] awardNubmer = StringUtils.splitByWholeSeparatorPreserveAllTokens(award.getReward(), ",");//7
        String blueNumber = awardNubmer[6];
        int awardCount = 0;
        for (DigitBall bet : bets) {
            String awardContent = "";
            bet.setAward(false);
            bet.setMoney("");
            bet.setAwardContent("");
            bet.setLevel("");
            String[] betNum = StringUtils.splitByWholeSeparatorPreserveAllTokens(bet.getContent(), ",");
            String betBlue = betNum[6];
            boolean blueAward = betBlue.equals(blueNumber);

            for (int i = 0; i < awardNubmer.length; i++) {
                if (awardNubmer[i].equals(betNum[i])) {
                    awardCount++;
                    awardContent += betNum[i] + ",";
                }
            }
            if (awardCount == 7) {
                bet.setLevel("一等奖");
                bet.setAward(true);
                bet.setMoney("5000000");
                bet.setAwardContent(awardContent);
            } else if (!blueAward && awardCount == 6) {
                bet.setLevel("二等奖");
                bet.setAward(true);
                bet.setMoney("5000000");
                bet.setAwardContent(awardContent);
            } else if (blueAward && awardCount == 6) {//前五个与尾号
                bet.setLevel("三等奖");
                bet.setAward(true);
                bet.setMoney("3000");
                bet.setAwardContent(awardContent);
            } else if (!blueAward && awardCount == 5) {//前五个
                bet.setLevel("四等奖");
                bet.setAward(true);
                bet.setMoney("500");
                bet.setAwardContent(awardContent);
            } else if (!blueAward && awardCount == 4) {//前四个
                bet.setLevel("五等奖");
                bet.setAward(true);
                bet.setMoney("30");
                bet.setAwardContent(awardContent);
            } else if (!blueAward && awardCount == 3) {//前3个
                bet.setLevel("五等奖");
                bet.setAward(true);
                bet.setMoney("5");
                bet.setAwardContent(awardContent);
            } else if (blueAward && awardCount == 1) {//前1 与尾1
                bet.setLevel("五等奖");
                bet.setAward(true);
                bet.setMoney("5");
                bet.setAwardContent(awardContent);
            } else if (blueAward) {//尾1
                bet.setLevel("五等奖");
                bet.setAward(true);
                bet.setMoney("5");
                bet.setAwardContent(awardContent);
            }
        }
        return bets;
    }

}
