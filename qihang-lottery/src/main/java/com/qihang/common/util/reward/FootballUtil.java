package com.qihang.common.util.reward;

import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSON;
import com.qihang.annotation.TenantIgnore;
import com.qihang.common.util.CombinationUtil;
import com.qihang.constant.Constant;
import com.qihang.controller.football.dto.FootballMatchDTO;
import com.qihang.controller.order.admin.lottery.vo.SportSchemeDetailsListVO;
import com.qihang.controller.order.admin.lottery.vo.SportSchemeDetailsVO;
import com.qihang.controller.racingball.app.vo.BallCalculationVO;
import com.qihang.controller.racingball.app.vo.BallCombinationVO;
import com.qihang.controller.racingball.app.vo.BallOptimizationVO;
import com.qihang.domain.order.LotteryTicketDO;
import com.qihang.domain.order.vo.TicketContentVO;
import com.qihang.domain.order.vo.TicketVO;
import com.qihang.service.racingball.RacingBallServiceImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static cn.hutool.core.util.NumberUtil.isInteger;
import static cn.hutool.core.util.NumberUtil.max;
import static com.qihang.service.racingball.RacingBallServiceImpl.fillZero;

/**
 * @author: bright
 * @description:
 * @time: 2022-11-01 22:10
 */
@Slf4j
public class FootballUtil {
    /**
     * 从m个数中选n个数的排列组合
     *
     * @param m n
     * @return
     */
    static int[][] getIndexgroup(int m, int n)//m中选n个 返回索引组合
    {
        int[] a = new int[m];
        for (int i = 0; i < m; i++) {
            a[i] = i;
        }
        List<int[]> list = new ArrayList<>();
        int[] b = new int[n];
        C(m, n, a, b, list);
        int[][] rest = new int[list.size()][n];
        for (int i = 0; i < list.size(); i++) {
            int[] re = list.get(i);
            for (int j = 0; j < n; j++) {
                rest[i][j] = re[j];
            }
        }
        return rest;
    }

    /**
     * 从m个数中选n个数的排列组合 是getIndexgroup的子问题
     *
     * @param m n
     * @return
     */
    static void C(int m, int n, int[] a, int[] b, List<int[]> list) {
        int i, j;
        for (i = n; i <= m; i++) {
            b[n - 1] = i - 1;
            if (n > 1) {
                C(i - 1, n - 1, a, b, list);
            } else {
                int[] op = new int[b.length];
                for (j = 0; j <= b.length - 1; j++) {
                    op[j] = a[b[j]];
                }
                list.add(op);
            }
        }
    }

    /**
     * 获取某个分组注数
     *
     * @param footballMatchDTOalls 比赛场 groupingindex 场次索引
     * @return
     */
    public static int getbetsnum(FootballMatchDTOall[] footballMatchDTOalls, int[] groupingindex)//获取注数
    {
        int result = 1;
        for (int i = 0; i < groupingindex.length; i++) {
            result = result * footballMatchDTOalls[groupingindex[i]].length;
        }
        return result;
    }

    /**
     * 获取所有分组总注数
     *
     * @param footballMatchDTOalls 比赛场 groupingindex 场次索引
     * @return
     */
    public static int getallbetsnum(FootballMatchDTOall[] footballMatchDTOalls, int[][] groupingindex)//获取注数
    {
        int result = 0;
        for (int i = 0; i < groupingindex.length; i++) {
            int[] index = groupingindex[i];
            result = result + getbetsnum(footballMatchDTOalls, index);
        }
        return result;
    }

    /**
     * 获取每个分组最大最小赔率
     *
     * @param footballMatchDTOalls 比赛场 []m选取的比赛索引
     * @return
     */
    public static double[] getrange(FootballMatchDTOall[] footballMatchDTOalls, int[] m)//获取比赛组合中最大赔率与最小赔率
    {
        double max = 1, min = 1;
        for (int i = 0; i < m.length; i++) {
            max = max * footballMatchDTOalls[m[i]].maxodds;
            min = min * footballMatchDTOalls[m[i]].minodds;
        }
        return new double[]{max, min};
    }

    /**
     * 获取所有分组最大最小赔率
     *
     * @param footballMatchDTOalls 比赛场 [][]m 所有分组比赛索引
     * @return
     */
    public static double[] getallrange(FootballMatchDTOall[] footballMatchDTOalls, int[][] m) {
        int[] x = m[0];
        double[] result = getrange(footballMatchDTOalls, x);
        double[] zresult = new double[2];
        for (int i = 1; i < m.length; i++) {
            x = m[i];
            zresult = getrange(footballMatchDTOalls, x);
            result[0] = result[0] + zresult[0];
            if (zresult[1] < result[1]) {
                result[1] = zresult[1];
            }
        }
        return result;
    }

    /**
     * 获取比赛按所有分组方式的所有组合
     *
     * @param footballMatchList 比赛场
     * @return
     */
    public static List<List<BallCombinationVO>> getallfootballOptimization(List<FootballMatchDTO> footballMatchList, int[][] groupingindex) {
        List<List<BallCombinationVO>> foot = new ArrayList<>();
        for (int i = 0; i < groupingindex.length; i++) {
            int[] x = groupingindex[i];
            foot.addAll(getfootballOptimization(footballMatchList, x));
        }
        return foot;

    }

    /**
     * 获取比赛按一场分组方式的所有组合
     *
     * @param footballMatchList 比赛场
     * @return
     */
    public static List<List<BallCombinationVO>> getfootballOptimization(List<FootballMatchDTO> footballMatchList, int[] groupingindex) {
        List<BallCombinationVO>[] footballMatchDTOlistarry = new ArrayList[groupingindex.length];
        for (int i = 0; i < groupingindex.length; i++) {
            footballMatchDTOlistarry[i] = getAllOptions(footballMatchList.get(groupingindex[i]));
        }
        int m = footballMatchDTOlistarry.length;
        List<BallCombinationVO> footballCombinationVOList = new ArrayList<>();
        List<List<BallCombinationVO>> reust = new ArrayList<>();
        getfootballOptimization(footballMatchDTOlistarry, m, footballCombinationVOList, reust);
        return reust;
    }

    /**
     * 获取某厂比赛所有组合 递归查找
     *
     * @param footballCombinationVOList 记录本次遍历路过元素
     * @return
     */
    public static void getfootballOptimization(List<BallCombinationVO>[] footballMatchDTOlistarry, int m, List<BallCombinationVO> footballCombinationVOList, List<List<BallCombinationVO>> reust) {
        if (m == 0) {
            List<BallCombinationVO> listcopy = new ArrayList<>();
            listcopy.addAll(footballCombinationVOList);
            reust.add(listcopy);
            footballCombinationVOList.remove(footballCombinationVOList.size() - 1);
        } else {
            for (int i = 0; i < footballMatchDTOlistarry[m - 1].size(); i++) {
                footballCombinationVOList.add(footballMatchDTOlistarry[m - 1].get(i));
                getfootballOptimization(footballMatchDTOlistarry, m - 1, footballCombinationVOList, reust);
            }
            if (footballCombinationVOList.isEmpty()) {
                return;
            }
            footballCombinationVOList.remove(footballCombinationVOList.size() - 1);

        }
    }

    /**
     * 获取一场比赛某种取胜方式所有选项
     *
     * @param footballMatchDTO 比赛表
     * @return
     */
    public static List<BallCombinationVO> getOptions(FootballMatchDTO footballMatchDTO, List<Map<String, Object>> oddlist, String... args) {
        List<BallCombinationVO> footballCombinationVOList = new ArrayList<>();
        for (int i = 0; i < oddlist.size(); i++) {
            BallCombinationVO footballCombinationVO = new BallCombinationVO();
            footballCombinationVO.setHomeTeam(footballMatchDTO.getHomeTeam());
            footballCombinationVO.setNumber(footballMatchDTO.getNumber());
            footballCombinationVO.setVisitingTeam(footballMatchDTO.getVisitingTeam());
            footballCombinationVO.setState("0");
            if (args.length > 0) {
                footballCombinationVO.setContent("让" + oddlist.get(i).get("describe").toString() + "(" + Double.parseDouble(oddlist.get(i).get("odds").toString()) + ")");
            } else {
                footballCombinationVO.setContent(oddlist.get(i).get("describe").toString() + "(" + Double.parseDouble(oddlist.get(i).get("odds").toString()) + ")");
            }
            footballCombinationVOList.add(footballCombinationVO);

        }
        return footballCombinationVOList;
    }

    /**
     * 获取一场比赛选取的所有选项
     *
     * @param footballMatchDTO 比赛表
     * @return
     */
    public static List<BallCombinationVO> getAllOptions(FootballMatchDTO footballMatchDTO) {
        List<BallCombinationVO> footballCombinationVOList = new ArrayList<>();
        footballCombinationVOList.addAll(getOptions(footballMatchDTO, footballMatchDTO.getGoalOddsList()));
        footballCombinationVOList.addAll(getOptions(footballMatchDTO, footballMatchDTO.getLetOddsList(), "let"));
        footballCombinationVOList.addAll(getOptions(footballMatchDTO, footballMatchDTO.getScoreOddsList()));
        footballCombinationVOList.addAll(getOptions(footballMatchDTO, footballMatchDTO.getHalfWholeOddsList()));
        footballCombinationVOList.addAll(getOptions(footballMatchDTO, footballMatchDTO.getNotLetOddsList()));
        return footballCombinationVOList;
    }

    /**
     * 计算每一注奖金并将List<List<FootballCombinationVO>>返回为List<FootballOptimizationVO>
     *
     * @param footballOptimization 一重list表示注数 二重表示每一注有哪些选项
     * @return
     */
    public static List<BallOptimizationVO> getFootballOptimizationVOlist(List<List<BallCombinationVO>> footballOptimization, Integer multiple) {
        List<BallOptimizationVO> footballOptimizationVOList = new ArrayList<>();
        for (int i = 0; i < footballOptimization.size(); i++) {
            BallOptimizationVO footballOptimizationVO = new BallOptimizationVO();
            footballOptimizationVO.setBallCombinationList(footballOptimization.get(i));
            footballOptimizationVO.setNotes(multiple);
            footballOptimizationVO.setIsShow(false);
            footballOptimizationVOList.add(footballOptimizationVO);
        }
        return footballOptimizationVOList;

    }

    /**
     * 计算 组 注 预测金额
     *
     * @param footballMatchList 选中的比赛列表
     * @param multiple          倍数
     * @param pssTypeList       类型，可以单个，可以多个，例如 二串一 三串一 可以单也可以组合
     * @return
     */


    public static BallCalculationVO calculation(List<FootballMatchDTO> footballMatchList, Integer multiple, List<Integer> pssTypeList) {
        BallCalculationVO footballCalculation = new BallCalculationVO();
        FootballMatchDTOall[] footballMatchDTOalls = new FootballMatchDTOall[footballMatchList.size()];
        for (int i = 0; i < footballMatchList.size(); i++) {
            footballMatchDTOalls[i] = new FootballMatchDTOall(footballMatchList.get(i));
        }
        double[] range = new double[2];
        int[][] Indexgroup = getIndexgroup(footballMatchList.size(), pssTypeList.get(0));
        int betsnum = getallbetsnum(footballMatchDTOalls, Indexgroup);
//        range = getallrange(footballMatchDTOalls, Indexgroup);
//        double allmax = range[0];
//        double allmin = range[1];
        List<List<BallCombinationVO>> footballOptimizationz = getallfootballOptimization(footballMatchList, Indexgroup);

        for (int i = 1; i < pssTypeList.size(); i++) {
            int[][] Indexgroup1 = getIndexgroup(footballMatchList.size(), pssTypeList.get(i));
            betsnum = betsnum + getallbetsnum(footballMatchDTOalls, Indexgroup1);
//            range = getallrange(footballMatchDTOalls, Indexgroup1);
//            allmax = range[0] + allmax;
//            if (allmin > range[1]) {
//                allmin = range[1];
//            }
            footballOptimizationz.addAll(getallfootballOptimization(footballMatchList, Indexgroup1));
        }
        List<BallOptimizationVO> nomralOptimizationList = new ArrayList<>();

        BigDecimal allmax = BigDecimal.ZERO;
        BigDecimal allmin = BigDecimal.ZERO;
        int idx = 0;
        for (List<BallCombinationVO> p : footballOptimizationz) {
            BallOptimizationVO vo = new BallOptimizationVO();
            vo.setBallCombinationList(p);
            vo.setType(p.size() + "串1");
            vo.setNotes(1);// 这里调整，会影响详情展示，与后台的展示
            BigDecimal forest = foreast(vo.getBallCombinationList()).multiply(BigDecimal.valueOf(multiple));
            vo.setForecastBonus(forest.setScale(2, RoundingMode.HALF_UP));
            if (idx == 0) {
                allmin = vo.getForecastBonus();
            }
            if (vo.getForecastBonus().compareTo(allmax) > 0) {
                allmax = vo.getForecastBonus();
            }
            if (vo.getForecastBonus().compareTo(allmin) < 0) {
                allmin = vo.getForecastBonus();
            }
            nomralOptimizationList.add(vo);
            idx++;
        }
        footballCalculation.setNotes(betsnum);
        footballCalculation.setMaxPrice(allmax);
        footballCalculation.setMinPrice(allmin);
        List<BallOptimizationVO>[] FootballOptimizationVOlist = BasketballUtil.getFootballOptimizationVOlist(footballOptimizationz, multiple);

        FootballOptimizationVOlist[0] = FootballOptimizationVOlist[0].stream().sorted(Comparator.comparing(BallOptimizationVO::getType).thenComparing(BallOptimizationVO::getNotes)).collect(Collectors.toList());
        FootballOptimizationVOlist[1] = FootballOptimizationVOlist[1].stream().sorted(Comparator.comparing(BallOptimizationVO::getType).thenComparing(BallOptimizationVO::getNotes)).collect(Collectors.toList());
        FootballOptimizationVOlist[2] = FootballOptimizationVOlist[2].stream().sorted(Comparator.comparing(BallOptimizationVO::getType).thenComparing(BallOptimizationVO::getNotes)).collect(Collectors.toList());

        footballCalculation.setAverageOptimizationList(FootballOptimizationVOlist[0]);
        footballCalculation.setColdOptimizationList(FootballOptimizationVOlist[1]);
        footballCalculation.setHeatOptimizationList(FootballOptimizationVOlist[2]);
        footballCalculation.setNormalOptimizatinList(nomralOptimizationList);
        return footballCalculation;
    }

    public static BigDecimal foreast(List<BallCombinationVO> vos) {
        BigDecimal bigDecimal = BigDecimal.ONE;
        for (BallCombinationVO v : vos) {
            int idx = v.getContent().indexOf("(");
            int last = v.getContent().indexOf(")");
            String odd = v.getContent().substring(idx + 1, last);
            bigDecimal = bigDecimal.multiply(BigDecimal.valueOf(Double.valueOf(odd)));
        }
        return bigDecimal.multiply(BigDecimal.valueOf(2));
    }

    static class FootballMatchDTOall {
        private Integer id;
        private String number;
        private String match;
        private int length;
        private Double maxodds;
        private Double minodds;
        private String homeTeam;
        private String visitingTeam;
        private String letBall;

        private void setMaxodds(List<Double> OddsList) {
            Double odds = OddsList.get(0);
            for (int i = 1; i < OddsList.size(); i++) {
                Double z = OddsList.get(i);
                if (z > odds) {
                    odds = z;
                }
            }
            this.maxodds = odds;
        }

        private void setMinodds(List<Double> OddsList) {
            Double odds = OddsList.get(0);
            for (int i = 1; i < OddsList.size(); i++) {
                Double z = OddsList.get(i);
                if (z < odds) {
                    odds = z;
                }
            }
            this.minodds = odds;
        }

        private void sumscoreOddsandNotLet(Double[][] scortable, Double[] NotLetArray, Double[] scorespecial) {
            for (int m = 0; m < 6; m++)
                for (int n = 0; n < 6; n++) {
                    if (m > n && n < 3) {
                        scortable[m][n] = scortable[m][n] + NotLetArray[0];
                    } else if (m < n && m < 3) {
                        scortable[m][n] = scortable[m][n] + NotLetArray[2];
                    } else if (m == n && m < 4) {
                        scortable[m][n] = scortable[m][n] + NotLetArray[1];
                    }
                }
            for (int i = 0; i < 3; i++) {
                scorespecial[i] = scorespecial[i] + NotLetArray[i];
            }
        }

        private void sumscoreOddsandLet(Double[][] scortable, Double[] letLetArray, Double[] scorespecial) {
            int offset = Integer.valueOf(this.letBall.trim());
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    if (i + offset > j) {
                        if ((i > j && j < 3) || (i == j && i < 4) || (i < j && i < 3)) {
                            scortable[i][j] = scortable[i][j] + letLetArray[0];
                        }
                    } else if (i + offset == j) {
                        if ((i > j && j < 3) || (i == j && i < 4) || (i < j && i < 3)) {
                            scortable[i][j] = scortable[i][j] + letLetArray[1];
                        }
                    } else if (i + offset < j) {
                        if ((i > j && j < 3) || (i == j && i < 4) || (i < j && i < 3)) {
                            scortable[i][j] = scortable[i][j] + letLetArray[2];
                        }
                    }
                }
            }
            if (offset < 0) {
                scorespecial[2] = scorespecial[2] + letLetArray[2];
                scorespecial[1] = scorespecial[1] + letLetArray[2];
            } else if (offset > 0) {
                scorespecial[0] = scorespecial[0] + letLetArray[0];
                scorespecial[1] = scorespecial[1] + letLetArray[0];
            }
        }

        private void sumscoreOddsandgoalodd(Double[][] scortable, Double[] goalOddsArray, Double[] scorespecial) {
            for (int i = 0; i < 6; i++)
                for (int j = 0; j < 6; j++) {
                    if ((i > j && j < 3) || (i == j && i < 4) || (i < j && i < 3)) {
                        scortable[i][j] = scortable[i][j] + goalOddsArray[i + j];
                    }
                }
            for (int i = 0; i < 3; i++) {
                scorespecial[i] = scorespecial[i] + goalOddsArray[7];
            }
        }

        private List<Double>[] sumscoreOddsandgoaloddandLet(Double[][] scortable, Double[] letLetArray, Double[] scorespecial, Double[] halfWholeArray) {
            List<Double> oddsList = new ArrayList<>();
            List<Double> oddsListmin = new ArrayList<>();
            Double[][] scortablemin = new Double[6][6];
            Double[] scorespecialmin = new Double[3];
            System.arraycopy(scorespecial, 0, scorespecialmin, 0, 3);
            for (int i = 0; i < 6; i++) {
                System.arraycopy(scortable[i], 0, scortablemin[i], 0, 6);
            }
            Double[][] half = new Double[3][3];
            for (int i = 0; i < 3; i++) {
                System.arraycopy(halfWholeArray, 3 * i, half[i], 0, 3);
            }
            for (int m = 0; m < 6; m++) {
                for (int n = 0; n < 6; n++) {
                    if (m > n && n < 3) {
                        if (n != 0) {
                            scortable[m][n] = scortable[m][n] + Arrays.stream(half[0]).max(Double::compare).get();
                            scortablemin[m][n] = scortablemin[m][n] + Arrays.stream(half[0]).min(Double::compare).get();
                        } else {
                            scortable[m][n] = scortable[m][n] + Math.max(half[0][0], half[0][1]);
                            scortablemin[m][n] = scortablemin[m][n] + Math.min(half[0][0], half[0][1]);
                        }
                    } else if (m < n && m < 3) {
                        if (m != 0) {
                            scortable[m][n] = scortable[m][n] + Arrays.stream(half[2]).max(Double::compare).get();
                            scortablemin[m][n] = scortablemin[m][n] + Arrays.stream(half[2]).min(Double::compare).get();
                        } else {
                            scortable[m][n] = scortable[m][n] + Math.max(half[2][1], half[2][2]);
                            scortablemin[m][n] = scortablemin[m][n] + Math.min(half[2][1], half[2][2]);
                        }
                    } else if (m == n && m < 4) {
                        if (m != 0) {
                            scortable[m][n] = scortable[m][n] + Arrays.stream(half[1]).max(Double::compare).get();
                            scortablemin[m][n] = scortablemin[m][n] + Arrays.stream(half[1]).min(Double::compare).get();
                        } else {
                            scortable[m][n] = scortable[m][n] + half[1][1];
                            scortablemin[m][n] = scortablemin[m][n] + half[1][1];
                        }
                    }
                }
            }
            for (int i = 0; i < 3; i++) {
                scorespecial[i] = scorespecial[i] + Arrays.stream(half[i]).max(Double::compare).get();
                scorespecialmin[i] = scorespecialmin[i] + Arrays.stream(half[i]).min(Double::compare).get();
            }
            int offset = Integer.valueOf(this.letBall.trim());
            if (offset < 0) {
                if (offset < -1) {
                    scorespecial[0] = scorespecial[0] + Arrays.stream(letLetArray).max(Double::compare).get();
                    scorespecialmin[0] = scorespecialmin[0] + Arrays.stream(letLetArray).min(Double::compare).get();
                } else {
                    scorespecial[0] = scorespecial[0] + Math.max(letLetArray[1], letLetArray[0]);
                    scorespecialmin[0] = scorespecialmin[0] + Math.min(letLetArray[1], letLetArray[0]);
                }
            } else if (offset > 0) {
                if (offset > 1) {
                    scorespecial[2] = scorespecial[2] + Arrays.stream(letLetArray).max(Double::compare).get();
                    scorespecialmin[2] = scorespecialmin[2] + Arrays.stream(letLetArray).min(Double::compare).get();
                } else {
                    scorespecial[2] = scorespecial[2] + Math.max(letLetArray[1], letLetArray[2]);
                    scorespecialmin[2] = scorespecialmin[2] + Math.min(letLetArray[1], letLetArray[2]);
                }
            }
            setimlist(oddsList, scorespecial);
            setimlist(oddsListmin, scorespecialmin);
            for (int i = 0; i < 6; i++) {
                setimlist(oddsList, scortable[i]);
                setimlist(oddsListmin, scortablemin[i]);
            }
            List<Double>[] list = new ArrayList[2];
            list[0] = oddsList;
            if (oddsListmin.size() == 0) {
                list[1] = oddsList;
            } else {
                list[1] = oddsListmin;
            }
            return list;
        }

        private void setimlist(List<Double> list, Double[] oddsArray) {
            for (int i = 0; i < oddsArray.length; i++) {
                if (oddsArray[i] != 0.0) {
                    list.add(oddsArray[i]);
                }
            }
        }

        public Double[][] getscorTable(String[] score, List<Map<String, Object>> scoreoddList) {
            Double[][] reust = new Double[6][6];
            for (int i = 0; i < 6; i++)
                Arrays.fill(reust[i], 0.0);
            if (!scoreoddList.isEmpty()) {
                for (int i = 0; i < scoreoddList.size(); i++) {
                    for (int j = 0; j < score.length; j++) {
                        if (scoreoddList.get(i).get("describe").toString().trim().equals(score[j])) {
                            if (isInteger(score[j].substring(0, 1)))
                                reust[Integer.parseInt(score[j].substring(0, 1))][Integer.parseInt(score[j].substring(2))] = Double.parseDouble(scoreoddList.get(i).get("odds").toString());
                            break;
                        }
                    }
                }
            }
            return reust;
        }

        public List<Double>[] getoddsinGoalandNotLet(List<Map<String, Object>> NotLetOddsList, List<Map<String, Object>> LetoddList, List<Map<String, Object>> goaloddList, List<Map<String, Object>> scoreoddList, List<Map<String, Object>> halfWholeOddsList) {
            List<Double>[] list = new ArrayList[2];
            String[] NotLetOdds = {"胜", "平", "负"};
            String[] letLetOdds = {"胜", "平", "负"};
            String[] goalOdds = {"0", "1", "2", "3", "4", "5", "6", "7+"};
            String[] scoreOdds = {"1:0", "2:0", "2:1", "3:0", "3:1", "3:2", "4:0", "4:1", "4:2", "5:0", "5:1", "5:2", "胜其他",
                    "0:0", "1:1", "2:2", "3:3", "平其他", "0:1", "0:2", "1:2", "0:3", "1:3", "2:3", "0:4", "1:4", "2:4",
                    "0:5", "1:5", "2:5", "负其他"};
            String[] halfWholeOdds = {"胜-胜", "平-胜", "负-胜", "胜-平", "平-平", "负-平", "胜-负", "平-负", "负-负"};
            //每项的值
            Double[] NotLetArray = getoddsintype(NotLetOddsList, NotLetOdds);
            Double[] letLetArray = getoddsintype(LetoddList, letLetOdds);
            Double[] goalOddsArray = getoddsintype(goaloddList, goalOdds);
            Double[] scoreArray = getoddsintype(scoreoddList, scoreOdds);
            Double[] halfWholeArray = getoddsintype(halfWholeOddsList, halfWholeOdds);
            Double[][] scortable = getscorTable(scoreOdds, scoreoddList);
            //记录比分表特殊节点
            Double[] scorespecial = {scoreArray[12], scoreArray[17], scoreArray[30]};

            sumscoreOddsandNotLet(scortable, NotLetArray, scorespecial);
            sumscoreOddsandLet(scortable, letLetArray, scorespecial);
            sumscoreOddsandgoalodd(scortable, goalOddsArray, scorespecial);
            list = sumscoreOddsandgoaloddandLet(scortable, letLetArray, scorespecial, halfWholeArray);
            return list;
        }

        public Double[] getoddsintype(List<Map<String, Object>> oddlist, String[] typelist) {
            Double[] reust = new Double[typelist.length];
            Arrays.fill(reust, 0.0);
            if (!oddlist.isEmpty()) {
                for (int i = 0; i < oddlist.size(); i++) {
                    for (int j = 0; j < typelist.length; j++) {
                        if (oddlist.get(i).get("describe").toString().trim().equals(typelist[j])) {
                            reust[j] = Double.parseDouble(oddlist.get(i).get("odds").toString());
                            break;
                        }
                    }
                }
            }
            return reust;
        }

        public FootballMatchDTOall(FootballMatchDTO footballMatchDTO) {
            this.id = footballMatchDTO.getId();
            this.number = footballMatchDTO.getNumber();
            this.match = footballMatchDTO.getMatch();
            this.homeTeam = footballMatchDTO.getHomeTeam();
            this.visitingTeam = footballMatchDTO.getVisitingTeam();
            this.length = footballMatchDTO.getGoalOddsList().size() + footballMatchDTO.getHalfWholeOddsList().size() + footballMatchDTO.getLetOddsList().size() + footballMatchDTO.getNotLetOddsList().size() + footballMatchDTO.getScoreOddsList().size();
            this.letBall = footballMatchDTO.getLetBall();
            List<Double>[] OddsList = new ArrayList[2];
            OddsList = getoddsinGoalandNotLet(footballMatchDTO.getNotLetOddsList(), footballMatchDTO.getLetOddsList(), footballMatchDTO.getGoalOddsList(), footballMatchDTO.getScoreOddsList(), footballMatchDTO.getHalfWholeOddsList());
            setMaxodds(OddsList[0]);
            setMinodds(OddsList[1]);
        }
    }

    /**
     * 出奖结果计算
     *
     * @param footballMatchList 用户选中的数组
     * @param multiple          倍数
     * @param pssTypeList       过关类型
     * @param str               开奖结果固定形式
     *                          第一场开奖结果： 胜,胜,7+,胜胜,7:0
     *                          第二场开奖结果： 胜,胜,1,平胜,1:0
     *                          第三场开奖结果： 胜,胜,2,胜胜,2:0
     * @return 用户中了多少注把中的每一注金额相加在进行返回
     */
    public static BigDecimal award(List<FootballMatchDTO> footballMatchList, Integer multiple, List<Integer> pssTypeList, List<String> str) {
        BigDecimal res = BigDecimal.ZERO;
        String[][] AllFootconse = new String[str.size()][5];
        List<List<Map<String, String>>> hasFootconse = new ArrayList<>();
        for (int i = 0; i < str.size(); i++) {
            String[] eachid = new String[5];
            AllFootconse[i] = str.get(i).split(",");
            String[] bif = AllFootconse[i][4].split(":");
            if (Integer.valueOf(bif[0]) + Integer.valueOf(bif[1]) > 7 || Integer.valueOf(bif[0]) > 5 || Integer.valueOf(bif[1]) > 5) {
                if (Integer.valueOf(bif[0]) > Integer.valueOf(bif[1]))
                    AllFootconse[i][4] = "胜其他";
                else if (Integer.valueOf(bif[0]) == Integer.valueOf(bif[1]))
                    AllFootconse[i][4] = "平其他";
                else if (Integer.valueOf(bif[0]) < Integer.valueOf(bif[1]))
                    AllFootconse[i][4] = "负其他";
            }

            int offset = Integer.valueOf(footballMatchList.get(i).getLetBall().trim());
            if (offset + Integer.valueOf(bif[0]) > Integer.valueOf(bif[1])) {
                AllFootconse[i][1] = "胜";
            } else if (offset + Integer.valueOf(bif[0]) < Integer.valueOf(bif[1])) {
                AllFootconse[i][1] = "负";
            } else if (offset + Integer.valueOf(bif[0]) == Integer.valueOf(bif[1])) {
                AllFootconse[i][1] = "平";
            }

            eachid[0] = Findoddsbytype(footballMatchList.get(i).getNotLetOddsList(), AllFootconse[i][0]);
            eachid[1] = Findoddsbytype(footballMatchList.get(i).getLetOddsList(), AllFootconse[i][1]);
            eachid[2] = Findoddsbytype(footballMatchList.get(i).getGoalOddsList(), AllFootconse[i][2]);
            eachid[3] = Findoddsbytype(footballMatchList.get(i).getScoreOddsList(), AllFootconse[i][4]);
            eachid[4] = Findoddsbytype(footballMatchList.get(i).getHalfWholeOddsList(), AllFootconse[i][3]);
            if ((eachid[0] + eachid[1] + eachid[2] + eachid[3] + eachid[4]).equals("")) {
                str.remove(i);
                footballMatchList.remove(i);
                i--;
            } else {
                List<Map<String, String>> realFoot = new ArrayList<>();
                for (int j = 0; j < 5; j++) {
                    if (!eachid[j].equals("")) {
                        Map<String, String> x = new HashMap();
                        x.put("odds", eachid[j]);
                        realFoot.add(x);
                    }
                }
                hasFootconse.add(realFoot);
            }
        }
        for (int i = 0; i < pssTypeList.size(); i++) {
            if (pssTypeList.get(i) > str.size()) {
                pssTypeList.remove(i--);
            }
        }
        if (pssTypeList.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<Map<String, String>>[] realFoot = new ArrayList[footballMatchList.size()];
        for (int i = 0; i < footballMatchList.size(); i++) {
            realFoot[i] = hasFootconse.get(i);
        }
        for (int i = 0; i < pssTypeList.size(); i++) {
            int[][] Indexgroup = getIndexgroup(footballMatchList.size(), pssTypeList.get(i));
            List<List<Map<String, String>>> realallop = getallop(realFoot, Indexgroup);
            res = res.add(getAllbonus(realallop));
        }
        return res.multiply(BigDecimal.valueOf(multiple)).multiply(BigDecimal.valueOf(2));
    }

    public static String Findoddsbytype(List<Map<String, Object>> chose, String realoutcome) {
        String res = "";
        for (int i = 0; i < chose.size(); i++) {
            if (chose.get(i).get("describe").toString().equals(realoutcome))
                res = chose.get(i).get("odds").toString();
        }
        return res;
    }

    public static BigDecimal getAllbonus(List<List<Map<String, String>>> realallop) {
        BigDecimal res = BigDecimal.ZERO;
        for (int i = 0; i < realallop.size(); i++) {
            BigDecimal z = BigDecimal.ONE;
            for (int j = 0; j < realallop.get(i).size(); j++) {
                z = z.multiply(BigDecimal.valueOf(Double.valueOf(realallop.get(i).get(j).get("odds"))));
            }
            res = res.add(z);
        }
        return res;
    }

    public static List<List<Map<String, String>>> getallop(List<Map<String, String>>[] chooseFoot, int[][] index) {
        List<List<Map<String, String>>> foot = new ArrayList<>();
        for (int i = 0; i < index.length; i++) {
            int[] x = index[i];
            foot.addAll(getfootballOption(chooseFoot, x));
        }
        return foot;
    }

    public static List<List<Map<String, String>>> getfootballOption(List<Map<String, String>>[] chooseFoot, int[] groupingindex) {
        List<Map<String, String>>[] footballMatchDTOlistarry = new ArrayList[groupingindex.length];
        for (int i = 0; i < groupingindex.length; i++) {
            footballMatchDTOlistarry[i] = chooseFoot[groupingindex[i]];
        }
        int m = groupingindex.length;
        List<Map<String, String>> footballCombinationVOList = new ArrayList<>();
        List<List<Map<String, String>>> reust = new ArrayList<>();
        getfootballOption(footballMatchDTOlistarry, m, footballCombinationVOList, reust);
        return reust;
    }

    public static void getfootballOption(List<Map<String, String>>[] footballMatchDTOlistarry, int m, List<Map<String, String>> footballCombinationVOList, List<List<Map<String, String>>> reust) {
        if (m == 0) {
            List<Map<String, String>> listcopy = new ArrayList<>();
            listcopy.addAll(footballCombinationVOList);
            reust.add(listcopy);
            footballCombinationVOList.remove(footballCombinationVOList.size() - 1);
        } else {
            for (int i = 0; i < footballMatchDTOlistarry[m - 1].size(); i++) {
                footballCombinationVOList.add(footballMatchDTOlistarry[m - 1].get(i));
                getfootballOption(footballMatchDTOlistarry, m - 1, footballCombinationVOList, reust);
            }
            if (footballCombinationVOList.isEmpty()) {
                return;
            }
            footballCombinationVOList.remove(footballCombinationVOList.size() - 1);
        }
    }

    public static void awardSchemeDetails(List<SportSchemeDetailsListVO> sportsDetails, Map<String, String> awardMap) {

        for (SportSchemeDetailsListVO detailsVOS : sportsDetails) {

            Map<String, String> matchMaps = detailsVOS.getBallCombinationList().stream().collect(Collectors.toMap(SportSchemeDetailsVO::getNumber, SportSchemeDetailsVO::getContent, (a, b) -> a));

            boolean allFinished = true;
            for (Map.Entry<String, String> keyEntity : matchMaps.entrySet()) {
                if (awardMap.get(keyEntity.getKey()) == null) {
                    //没有开奖
                    allFinished = false;
                    break;
                }
            }
            if (allFinished) {
                List<String> resultOddsList = new ArrayList<>();
                for (Map.Entry<String, String> keyEntity : matchMaps.entrySet()) {
                    String content = keyEntity.getValue();

                    String award = awardMap.get(keyEntity.getKey());
                    //官方开奖结果
                    //胜,胜,4,胜-胜,4:0
                    //胜平负，让球胜平负，总进球数，半全场，比分
                    String[] allfootconse = StringUtils.split(award, ",");

                    //投注 胜（2.0）
                    String[] betContents = getBetContentAndOddFromContent(content);
                    // for (int i = 0; i < allfootconse.length; i++) {
                    //官方返回的是少了让字。
                    if (allfootconse[1].length() == 1) {
                        allfootconse[1] = "让" + allfootconse[1];
                    }
                    String[] bif = allfootconse[4].split(":");
                    if (Integer.valueOf(bif[0]) + Integer.valueOf(bif[1]) > 7 || Integer.valueOf(bif[0]) > 5 || Integer.valueOf(bif[1]) > 5) {
                        if (Integer.valueOf(bif[0]) > Integer.valueOf(bif[1]))
                            allfootconse[4] = "胜其他";
                        else if (Integer.valueOf(bif[0]) == Integer.valueOf(bif[1]))
                            allfootconse[4] = "平其他";
                        else if (Integer.valueOf(bif[0]) < Integer.valueOf(bif[1]))
                            allfootconse[4] = "负其他";
                    }
                    // }
                    String odd = getOddByResult(betContents, allfootconse);
                    if (StringUtils.isNotBlank(odd)) {
                        resultOddsList.add(odd);
                    }
                }
                if (resultOddsList.size() == matchMaps.size()) {
                    //中了
                    BigDecimal money = sumItem(resultOddsList).multiply(BigDecimal.valueOf(Integer.valueOf(detailsVOS.getNotes())));
                    detailsVOS.setMoney("" + money.setScale(2, RoundingMode.HALF_DOWN));
                    detailsVOS.setAward(true);
                }
            }
        }

    }

    static String getOddByResult(String[] content, String[] footconse) {
        for (String result : footconse) {
            if (content[0].equals(result)) {
                return content[1];
            }
        }
        return "";
    }

    static String[] getBetContentAndOddFromContent(String content) {
        int idx = content.indexOf("(");
        int last = content.indexOf(")");
        String[] odds = new String[2];
        odds[0] = content.substring(0, idx);
        odds[1] = content.substring(idx + 1, last);
        return odds;
    }

    public static BigDecimal sumItem(List<String> strings) {
        BigDecimal all = BigDecimal.valueOf(Double.valueOf(strings.get(0)));
        for (int i = 1; i < strings.size(); i++) {
            all = all.multiply(new BigDecimal(Double.valueOf(strings.get(i))));
        }
        return all.multiply(BigDecimal.valueOf(2));
    }

    public static List<LotteryTicketDO> getJCTicketVOBySechme(String schedetail, List<FootballMatchDTO> footballMatchDTOS, String orderNo, String mode) {
        List<SportSchemeDetailsListVO> sportsDetails = JSON.parseArray(schedetail, SportSchemeDetailsListVO.class);
        Map<String, FootballMatchDTO> matchMap = footballMatchDTOS.stream().collect(Collectors.toMap(FootballMatchDTO::getNumber, a -> a));
        List<LotteryTicketDO> lotteryTicketDOList = new ArrayList<>(sportsDetails.size());
        int idx = 1;
        for (SportSchemeDetailsListVO detailsVO : sportsDetails) {
            String notes = detailsVO.getNotes();
            List<SportSchemeDetailsVO> ballSelectedList = detailsVO.getBallCombinationList();
            //生成票数据
            LotteryTicketDO lotteryTicketDO = replaceTicketVOBySechmeBallList(ballSelectedList, matchMap, orderNo, Integer.valueOf(notes), idx, mode);
            lotteryTicketDOList.add(lotteryTicketDO);
            idx++;
        }
        return lotteryTicketDOList;
    }

    public static LotteryTicketDO replaceTicketVOBySechmeBallList(List<SportSchemeDetailsVO> detailsVOS, Map<String, FootballMatchDTO> footballMatchDTOMap, String orderNo, Integer times, Integer idx, String mode) {
        LotteryTicketDO lotteryTicketDO = new LotteryTicketDO();
        List<TicketVO> ticketVOList = new ArrayList<>();
        BigDecimal foreast = BigDecimal.ONE;
        List<String> matchList = new ArrayList<>();
        for (SportSchemeDetailsVO detailsVO : detailsVOS) {
            FootballMatchDTO footballMatchDTO = footballMatchDTOMap.get(detailsVO.getNumber());
            matchList.add(footballMatchDTO.getNumber());
            TicketVO ticketVO = new TicketVO();
            ticketVO.setMode(mode);
            ticketVO.setLetBall(footballMatchDTO.getLetBall());
            ticketVO.setMatch(footballMatchDTO.getMatch());
            ticketVO.setNumber(footballMatchDTO.getNumber());
            ticketVO.setHomeTeam(footballMatchDTO.getHomeTeam());
            ticketVO.setVisitingTeam(footballMatchDTO.getVisitingTeam());
            TicketContentVO ticketContentVO = new TicketContentVO();
            String[] content = parseSelectedOdds(detailsVO.getContent());
            ticketContentVO.setDescribe(content[0]);
            ticketContentVO.setShoted(false);
            ticketContentVO.setIndex(0);
            ticketContentVO.setActive(true);
            ticketContentVO.setOdds(content[1]);
            ticketContentVO.setMode(mode);

            foreast = foreast.multiply(BigDecimal.valueOf(Double.valueOf(content[1])));
            List<TicketContentVO> ticketContentVOList = new ArrayList<>();
            ticketContentVOList.add(ticketContentVO);
            ticketVO.setTicketContentVOList(ticketContentVOList);
            ticketVOList.add(ticketVO);
        }
        Collections.sort(matchList);
        lotteryTicketDO.setOrderId(orderNo);
        lotteryTicketDO.setTimes(Integer.valueOf(times));
        lotteryTicketDO.setTicketState(0);
        lotteryTicketDO.setTicketNo("" + idx);
        lotteryTicketDO.setRevokePrice(BigDecimal.ZERO);
        lotteryTicketDO.setPrice(BigDecimal.valueOf(2).multiply(BigDecimal.valueOf(lotteryTicketDO.getTimes())));
        lotteryTicketDO.setForecast(foreast.multiply(BigDecimal.valueOf(2)).multiply(BigDecimal.valueOf(lotteryTicketDO.getTimes())));
        lotteryTicketDO.setBetType("" + detailsVOS.size());
        lotteryTicketDO.setMatchs(StringUtils.join(matchList, ","));
        lotteryTicketDO.setTicketContent(JSON.toJSONString(ticketVOList));
        return lotteryTicketDO;
    }

    private static String[] parseSelectedOdds(String result) {
        // 胜[0-22](2.3)
        int idx = result.indexOf("(");
        int lastIdx = result.lastIndexOf(")");
        return new String[]{result.substring(0, idx), result.substring(idx + 1, lastIdx)};
    }

    /**
     * 串关 拆分票据
     */
    public static List<LotteryTicketDO> getJCTicketVO(List<FootballMatchDTO> footballMatchDTOS, List<Integer> playType, String orderNo, Integer times) {
        List<LotteryTicketDO> lotteryTicketDOS = new ArrayList<>(footballMatchDTOS.size() * playType.size());
        //串一过关
        String[] matchArrays = new String[footballMatchDTOS.size()];
        int idx = 0;
        Map<String, ArrayList<TicketVO>> ticketMap = new HashMap<>(footballMatchDTOS.size());
        for (FootballMatchDTO dto : footballMatchDTOS) {
            ArrayList<TicketVO> ticketVOS = buildTicketVO(dto);
            matchArrays[idx] = dto.getNumber();
            ticketMap.put(dto.getNumber(), ticketVOS);
            idx++;
        }
        for (Integer play : playType) {
            List<List<String>> lists = CombinationUtil.getCombinations(matchArrays, play);
            lotteryTicketDOS.addAll(replaceTicketVO(lists, ticketMap, orderNo, play, times));
        }
        lotteryTicketDOS.forEach(p -> {
            p.setTicketState(0);
            p.setCreateTime(new Date());
            p.setRevokePrice(BigDecimal.ZERO);
            p.setState(0);
        });
        return lotteryTicketDOS;
    }


    /**
     * 此处都按串一处理。
     *
     * @param combines
     * @param ticketVOMap
     * @param play
     * @return
     */
    private static List<LotteryTicketDO> replaceTicketVO(List<List<String>> combines, Map<String, ArrayList<TicketVO>> ticketVOMap, String orderId, Integer play, Integer times) {
        List<LotteryTicketDO> lotteryTicketDOS = new ArrayList<>();
        int multiPer = times % Constant.MAX_TICKET_MULTI == 0 ? times / Constant.MAX_TICKET_MULTI : times / Constant.MAX_TICKET_MULTI + 1;
        int idx = 0;
        for (List<String> lists : combines) {

            List<ArrayList<TicketVO>> ticketVOList = new ArrayList<>(lists.size());
            for (String number : lists) {
                ArrayList<TicketVO> ticketVOS = ticketVOMap.get(number);
                ticketVOList.add(ticketVOS);
            }
            //组成串，不同的玩法组一个
            ArrayList<ArrayList<TicketVO>> ticketVOList2 = CombinationUtil.permTowDimensionIsOrder(ticketVOList, play);

            for (ArrayList<TicketVO> ticketVOS : ticketVOList2) {
                List<TicketVO> ordersTicketList = new ArrayList<>();
                //一张票
                List<BigDecimal> maxOddsList = new ArrayList<>();
                int bets = 1;
                for (TicketVO ticketVO : ticketVOS) {
                    List<TicketContentVO> ticketContentVOList = ticketVO.getTicketContentVOList();
                    String maxOdd = ticketContentVOList.stream().max((o1, o2) -> Double.valueOf(o1.getOdds()).compareTo(Double.valueOf(o2.getOdds()))).get().getOdds();
                    maxOddsList.add(new BigDecimal(maxOdd));
                    bets = bets * ticketContentVOList.size();
                    ordersTicketList.add(ticketVO);
                }

                for (int i = 0; i < multiPer; i++) {
                    int mult = Constant.MAX_TICKET_MULTI;
                    if (i == multiPer - 1) {
                        //最后一票倍数
                        mult = times % Constant.MAX_TICKET_MULTI;
                    }
                    LotteryTicketDO lotteryTicketDO = new LotteryTicketDO();
                    lotteryTicketDO.setTicketNo(String.valueOf(++idx));
                    lotteryTicketDO.setForecast(maxOddsList.stream().reduce(BigDecimal.ONE, BigDecimal::multiply).multiply(BigDecimal.valueOf(2)).multiply(BigDecimal.valueOf(mult)));
                    lotteryTicketDO.setBets(bets);
                    lotteryTicketDO.setBetType("" + play);
                    lotteryTicketDO.setTimes(mult);
                    lotteryTicketDO.setOrderId(orderId);
                    lotteryTicketDO.setPrice(BigDecimal.valueOf(bets).multiply(BigDecimal.valueOf(2)).multiply(BigDecimal.valueOf(mult)));
                    lists.sort((a, b) -> a.compareTo(b));
                    lotteryTicketDO.setMatchs(StringUtils.join(lists, ","));
                    lotteryTicketDO.setTicketContent(JSON.toJSONString(ordersTicketList));
                    lotteryTicketDOS.add(lotteryTicketDO);
                }
            }


        }


        return lotteryTicketDOS;
    }

    private static ArrayList<TicketVO> buildTicketVO(FootballMatchDTO dto) {
        ArrayList<TicketVO> ticketVOList = new ArrayList<>(100);
        TicketVO ticketVO = new TicketVO();
        ticketVO.setMatch(dto.getMatch());
        ticketVO.setLetBall(dto.getLetBall());
        ticketVO.setNumber(dto.getNumber());
        ticketVO.setHomeTeam(dto.getHomeTeam());
        ticketVO.setVisitingTeam(dto.getVisitingTeam());

        List<TicketContentVO> ticketContentVOList = new ArrayList<>(100);
        if (!CollectionUtils.isEmpty(dto.getLetOddsList())) {
            ticketContentVOList = new ArrayList<>(100);
            ticketVO.setMode("0");
            for (Map<String, Object> list : dto.getLetOddsList()) {
                TicketContentVO ticketContentVO = JSON.parseObject(JSON.toJSONString(list), TicketContentVO.class);
                ticketContentVO.setMode("0");
                ticketContentVO.setDescribe("让" + ticketContentVO.getDescribe());
                ticketContentVOList.add(ticketContentVO);
            }
            TicketVO newTicket = new TicketVO();
            BeanUtils.copyProperties(ticketVO, newTicket);
            newTicket.setTicketContentVOList(ticketContentVOList);
            ticketVOList.add(newTicket);
        }
        if (!CollectionUtils.isEmpty(dto.getNotLetOddsList())) {
            ticketContentVOList = new ArrayList<>(100);
            ticketVO.setMode("1");
            for (Map<String, Object> list : dto.getNotLetOddsList()) {
                TicketContentVO ticketContentVO = JSON.parseObject(JSON.toJSONString(list), TicketContentVO.class);
                ticketContentVO.setMode("1");
                ticketContentVOList.add(ticketContentVO);
            }
            TicketVO newTicket = new TicketVO();
            BeanUtils.copyProperties(ticketVO, newTicket);
            newTicket.setTicketContentVOList(ticketContentVOList);
            ticketVOList.add(newTicket);
        }
        if (!CollectionUtils.isEmpty(dto.getGoalOddsList())) {
            ticketContentVOList = new ArrayList<>(100);
            ticketVO.setMode("2");
            for (Map<String, Object> list : dto.getGoalOddsList()) {
                TicketContentVO ticketContentVO = JSON.parseObject(JSON.toJSONString(list), TicketContentVO.class);
                ticketContentVO.setMode("2");
                ticketContentVOList.add(ticketContentVO);
            }
            TicketVO newTicket = new TicketVO();
            BeanUtils.copyProperties(ticketVO, newTicket);
            newTicket.setTicketContentVOList(ticketContentVOList);
            ticketVOList.add(newTicket);
        }
        if (!CollectionUtils.isEmpty(dto.getHalfWholeOddsList())) {
            ticketContentVOList = new ArrayList<>(100);
            ticketVO.setMode("3");
            for (Map<String, Object> list : dto.getHalfWholeOddsList()) {
                TicketContentVO ticketContentVO = JSON.parseObject(JSON.toJSONString(list), TicketContentVO.class);
                ticketContentVO.setMode("3");
                ticketContentVOList.add(ticketContentVO);
            }
            TicketVO newTicket = new TicketVO();
            BeanUtils.copyProperties(ticketVO, newTicket);
            newTicket.setTicketContentVOList(ticketContentVOList);
            ticketVOList.add(newTicket);
        }
        if (!CollectionUtils.isEmpty(dto.getScoreOddsList())) {
            ticketContentVOList = new ArrayList<>(100);
            ticketVO.setMode("4");
            for (Map<String, Object> list : dto.getScoreOddsList()) {
                TicketContentVO ticketContentVO = JSON.parseObject(JSON.toJSONString(list), TicketContentVO.class);
                ticketContentVO.setMode("4");
                ticketContentVOList.add(ticketContentVO);
            }
            TicketVO newTicket = new TicketVO();
            BeanUtils.copyProperties(ticketVO, newTicket);
            newTicket.setTicketContentVOList(ticketContentVOList);
            ticketVOList.add(newTicket);
        }
        return ticketVOList;
    }

    public static void main(String[] args) {
        List<TicketContentVO> ticketContentVOList = new ArrayList<>();
        TicketContentVO contentVO = new TicketContentVO();
        contentVO.setOdds("15.0");
        ticketContentVOList.add(contentVO);

        TicketContentVO contentVO1 = new TicketContentVO();
        contentVO1.setOdds("2.0");
        ticketContentVOList.add(contentVO1);

        TicketContentVO contentVO2 = new TicketContentVO();
        contentVO2.setOdds("1.0");
        ticketContentVOList.add(contentVO2);
        String maxOdd = ticketContentVOList.stream().max((o1, o2) -> Double.valueOf(o1.getOdds()).compareTo(Double.valueOf(o2.getOdds()))).get().getOdds();
        System.out.println(maxOdd);
        List<BigDecimal> maxOddsList = new ArrayList<>();
        maxOddsList.add(new BigDecimal("15.0"));
        maxOddsList.add(new BigDecimal("2.0"));
        maxOddsList.add(new BigDecimal("3.0"));
        System.out.println(maxOddsList.stream().reduce(BigDecimal.ONE, BigDecimal::multiply));
    }
}
