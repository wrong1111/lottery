package com.qihang.common.util.reward;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.qihang.common.util.CombinationUtil;
import com.qihang.common.vo.BonuseVO;
import com.qihang.constant.Constant;
import com.qihang.controller.basketball.dto.BasketballMatchDTO;
import com.qihang.controller.football.dto.FootballMatchDTO;
import com.qihang.controller.order.admin.lottery.vo.SportSchemeDetailsListVO;
import com.qihang.controller.order.admin.lottery.vo.SportSchemeDetailsVO;
import com.qihang.controller.racingball.app.vo.BallCalculationVO;
import com.qihang.controller.racingball.app.vo.BallCombinationVO;
import com.qihang.controller.racingball.app.vo.BallOptimizationVO;
import com.qihang.domain.basketball.BasketballMatchDO;
import com.qihang.domain.order.LotteryTicketDO;
import com.qihang.domain.order.vo.TicketContentVO;
import com.qihang.domain.order.vo.TicketVO;
import com.qihang.reptile.LotteryProcessor;
import com.qihang.service.racingball.RacingBallServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: bright
 * @description:
 * @time: 2022-11-01 22:03
 */
public class BasketballUtil {
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
     * @param basketballMatchDTOall 比赛场 groupingindex 场次索引
     * @return
     */
    public static int getbetsnum(basketballMatchDTOall[] basketballMatchDTOall, int[] groupingindex)//获取注数
    {
        int result = 1;
        for (int i = 0; i < groupingindex.length; i++) {
            result = result * basketballMatchDTOall[groupingindex[i]].length;
        }
        return result;
    }

    /**
     * 获取所有分组总注数
     *
     * @param basketballMatchDTOall 比赛场 groupingindex 场次索引
     * @return
     */
    public static int getallbetsnum(basketballMatchDTOall[] basketballMatchDTOall, int[][] groupingindex)//获取注数
    {
        int result = 0;
        for (int i = 0; i < groupingindex.length; i++) {
            int[] index = groupingindex[i];
            result = result + getbetsnum(basketballMatchDTOall, index);
        }
        return result;
    }

    /**
     * 获取每个分组最大最小赔率
     *
     * @param basketballMatchDTOall 比赛场 []m选取的比赛索引
     * @return
     */
    public static double[] getrange(basketballMatchDTOall[] basketballMatchDTOall, int[] m)//获取比赛组合中最大赔率与最小赔率
    {
        double max = 1, min = 1;
        for (int i = 0; i < m.length; i++) {
            max = max * basketballMatchDTOall[m[i]].maxodds;
            min = min * basketballMatchDTOall[m[i]].minodds;
        }
        return new double[]{max, min};
    }

    /**
     * 获取所有分组最大最小赔率
     *
     * @param basketballMatchDTOall 比赛场 [][]m 所有分组比赛索引
     * @return
     */
    public static double[] getallrange(basketballMatchDTOall[] basketballMatchDTOall, int[][] m) {
        int[] x = m[0];
        double[] result = getrange(basketballMatchDTOall, x);
        double[] zresult = new double[2];
        for (int i = 1; i < m.length; i++) {
            x = m[i];
            zresult = getrange(basketballMatchDTOall, x);
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
    public static List<List<BallCombinationVO>> getallfootballOptimization(List<BasketballMatchDTO> footballMatchList, int[][] groupingindex) {
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
    public static List<List<BallCombinationVO>> getfootballOptimization(List<BasketballMatchDTO> footballMatchList, int[] groupingindex) {
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
    public static List<BallCombinationVO> getOptions(BasketballMatchDTO footballMatchDTO, List<Map<String, Object>> oddlist, int type) {
        List<BallCombinationVO> footballCombinationVOList = new ArrayList<>();
        for (int i = 0; i < oddlist.size(); i++) {
            BallCombinationVO footballCombinationVO = new BallCombinationVO();
            footballCombinationVO.setHomeTeam(footballMatchDTO.getHomeTeam());
            footballCombinationVO.setNumber(footballMatchDTO.getNumber());
            footballCombinationVO.setVisitingTeam(footballMatchDTO.getVisitingTeam());
            footballCombinationVO.setState("0");
            String fen = "";
            if (type == 1) {
                fen = footballMatchDTO.getSizeOddsList().get(0).get("score").toString();
            } else if (type == 2) {
                fen = footballMatchDTO.getCedePoints();
            }
            footballCombinationVO.setContent((type == 2 ? "让" : "") + oddlist.get(i).get("describe").toString() + (StringUtils.isNotBlank(fen) ? "[" + fen + "]" : "") + "(" + Double.parseDouble(oddlist.get(i).get("odds").toString()) + ")");
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
    public static List<BallCombinationVO> getAllOptions(BasketballMatchDTO footballMatchDTO) {
        List<BallCombinationVO> footballCombinationVOList = new ArrayList<>();
        footballCombinationVOList.addAll(getOptions(footballMatchDTO, footballMatchDTO.getDifferenceOddsList(), 0));//
        footballCombinationVOList.addAll(getOptions(footballMatchDTO, footballMatchDTO.getSizeOddsList(), 1));//大小分，需要加上大分值
        footballCombinationVOList.addAll(getOptions(footballMatchDTO, footballMatchDTO.getCedePointsOddsList(), 2));//让分，需要加上让分值
        footballCombinationVOList.addAll(getOptions(footballMatchDTO, footballMatchDTO.getWinNegativeOddsList(), 3));
        return footballCombinationVOList;
    }


    public static double[] getOptimizationodds(List<List<BallCombinationVO>> basketballOptimization) {
        double[] odds = new double[basketballOptimization.size()];
        for (int i = 0; i < basketballOptimization.size(); i++) {
            double odd = 1.0;
            List<BallCombinationVO> ballOptimizationVO = basketballOptimization.get(i);
            for (int j = 0; j < ballOptimizationVO.size(); j++) {
                odd = odd * Double.valueOf(ballOptimizationVO.get(j).getContent().split("\\(")[1].split("\\)")[0]);
            }
            odds[i] = odd;
        }
        return odds;
    }

    public static int findIndex(double[] odds, String type) {
        double re = odds[0];
        int d = 0;
        if (type.equals("max")) {
            for (int i = 1; i < odds.length; i++) {
                if (re < odds[i]) {
                    re = odds[i];
                    d = i;
                }
            }
        }
        if (type.equals("min")) {
            for (int i = 1; i < odds.length; i++) {
                if (re > odds[i]) {
                    re = odds[i];
                    d = i;
                }
            }
        }
        return d;
    }

    public static double sumodds(double[] odds) {
        double res = 0.0;
        for (int i = 0; i < odds.length; i++) {
            res = res + 1.0 / odds[i];
        }
        return res;
    }

    public static int findminabs(Integer[] Notes, double[] odds, double oddsum) {
        double abs = oddsum;
        int minabsindex = 0;
        double absx;
        for (int i = 0; i < Notes.length; i++) {
            absx = Math.abs((Notes[i] - 1) * odds[i] - oddsum);
            if (abs > absx) {
                abs = absx;
                minabsindex = i;
            }

        }
        return minabsindex;
    }

    public static Integer[][] getNote(double[] odds, Integer multiple) {
        Integer[][] Notes = new Integer[3][odds.length];
        if (multiple != 1) {
            double oddsum = sumodds(odds);
            int sum = odds.length * multiple;
            int maxinedx = findIndex(odds, "max"), minindex = findIndex(odds, "min");
            int maxsum = 0, avgsum = 0;
            for (int j = 0; j < odds.length; j++) {
                Notes[0][j] = (int) Math.ceil((sum / odds[j]) / oddsum);
                avgsum = avgsum + Notes[0][j];
                Notes[1][j] = (int) Math.ceil(sum / odds[j]);
                Notes[2][j] = (int) Math.ceil(sum / odds[j]);
                maxsum = maxsum + Notes[1][j];
            }
            if (sum > maxsum) {
                Notes[1][maxinedx] = Notes[1][maxinedx] + sum - maxsum;
                Notes[2][minindex] = Notes[2][minindex] + sum - maxsum;
            } else {
                while (maxsum > sum) {
                    int minabsindex = findminabs(Notes[1], odds, sum);
                    int minabsindexinmin = findminabs(Notes[2], odds, sum);
                    Notes[1][minabsindex]--;
                    Notes[2][minabsindexinmin]--;
                    maxsum--;
                }

            }
            double avg = sum / oddsum;
            while (avgsum > sum) {
                int avgabsminindex = findminabs(Notes[0], odds, avg);
                Notes[0][avgabsminindex]--;
                avgsum--;
            }
        } else if (multiple == 1) {
            for (int i = 0; i < 3; i++)
                for (int j = 0; j < odds.length; j++)
                    Notes[i][j] = 1;
        }

        return Notes;
    }

    /**
     * 计算每一注奖金并将List<List<FootballCombinationVO>>返回为List<FootballOptimizationVO>
     *
     * @param footballOptimization 一重list表示注数 二重表示每一注有哪些选项
     * @return
     */
    public static List<BallOptimizationVO>[] getFootballOptimizationVOlist(List<List<BallCombinationVO>> footballOptimization, Integer multiple) {
        double[] odds = getOptimizationodds(footballOptimization);
        Integer[][] Notes = getNote(odds, multiple);
        List<BallOptimizationVO>[] footballOptimizationVOList = new ArrayList[3];
        for (int m = 0; m < 3; m++) {
            footballOptimizationVOList[m] = new ArrayList<>();
            for (int i = 0; i < footballOptimization.size(); i++) {
                BallOptimizationVO footballOptimizationVO = new BallOptimizationVO();
                footballOptimizationVO.setBallCombinationList(footballOptimization.get(i));
                footballOptimizationVO.setType(footballOptimization.get(i).size() + "串1");
                footballOptimizationVO.setNotes(Notes[m][i]);
                footballOptimizationVO.setForecastBonus(NumberUtil.round(Notes[m][i] * 2 * odds[i], 2));
                footballOptimizationVOList[m].add(footballOptimizationVO);
            }
        }
        return footballOptimizationVOList;

    }


    /**
     * 计算 组 注 预测金额
     *
     * @param basketballMatchList 选中的比赛列表
     * @param multiple            倍数
     * @param pssTypeList         类型，可以单个，可以多个，例如 二串一 三串一 (可以单也可以组合)
     * @return
     */
    public static BallCalculationVO calculation(List<BasketballMatchDTO> basketballMatchList, Integer multiple, List<Integer> pssTypeList) {
        BallCalculationVO baskeballCalculation = new BallCalculationVO();
        basketballMatchDTOall[] basketballMatchDTOalls = new basketballMatchDTOall[basketballMatchList.size()];
        for (int i = 0; i < basketballMatchList.size(); i++) {
            basketballMatchDTOalls[i] = new basketballMatchDTOall(basketballMatchList.get(i));
        }
        double[] range = new double[2];
        int[][] Indexgroup = getIndexgroup(basketballMatchList.size(), pssTypeList.get(0));
        int betsnum = getallbetsnum(basketballMatchDTOalls, Indexgroup);
        range = getallrange(basketballMatchDTOalls, Indexgroup);
//        double allmax = range[0];
//        double allmin = range[1];
        List<BallOptimizationVO> basketballOptimization = new ArrayList<>();
        List<List<BallCombinationVO>> basketballOptimizationz = getallfootballOptimization(basketballMatchList, Indexgroup);

        for (int i = 1; i < pssTypeList.size(); i++) {
            int[][] Indexgroup1 = getIndexgroup(basketballMatchList.size(), pssTypeList.get(i));
            betsnum = betsnum + getallbetsnum(basketballMatchDTOalls, Indexgroup1);
            range = getallrange(basketballMatchDTOalls, Indexgroup1);
//            allmax = range[0] + allmax;
//            if (allmin > range[1]) {
//                allmin = range[1];
//            }
            basketballOptimizationz.addAll(getallfootballOptimization(basketballMatchList, Indexgroup1));
        }
        BigDecimal allmax = BigDecimal.ZERO;
        BigDecimal allmin = BigDecimal.ZERO;
        int idx = 0;
        for (List<BallCombinationVO> p : basketballOptimizationz) {
            BallOptimizationVO vo = new BallOptimizationVO();
            vo.setBallCombinationList(p);
            vo.setType(p.size() + "串1");
            vo.setNotes(1);//会影响后续倍数展示及有开奖
            BigDecimal forest = FootballUtil.foreast(vo.getBallCombinationList()).multiply(BigDecimal.valueOf(multiple));
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
            basketballOptimization.add(vo);
            idx++;
        }


        List<BallOptimizationVO>[] FootballOptimizationVOlist = getFootballOptimizationVOlist(basketballOptimizationz, multiple);
        baskeballCalculation.setNotes(betsnum);
        baskeballCalculation.setMaxPrice(allmax);
        baskeballCalculation.setMinPrice(allmin);
        baskeballCalculation.setAverageOptimizationList(FootballOptimizationVOlist[0]);
        baskeballCalculation.setColdOptimizationList(FootballOptimizationVOlist[1]);
        baskeballCalculation.setHeatOptimizationList(FootballOptimizationVOlist[2]);
        baskeballCalculation.setNormalOptimizatinList(basketballOptimization);

        return baskeballCalculation;
    }


    static class basketballMatchDTOall {
        private Integer id;
        private String number;
        private String match;
        private int length;
        private Double maxodds;
        private Double minodds;
        private String homeTeam;
        private String visitingTeam;
        private String cedePoints;

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

        private void sumdifferenceOddsandwinNegativeOdds(Double[] winNegaArray, Double[] differenceArray, Double[] differenceArraymin) {
            Double z = 0.0;
            for (int i = 0; i < 12; i++) {
                if (i < 6) {
                    z = winNegaArray[0];
                } else {
                    z = winNegaArray[1];
                }
                differenceArray[i] = differenceArray[i] + z;
                differenceArraymin[i] = differenceArraymin[i] + z;
            }
        }

        private void sumdifferenceOddsandcedePoints(Double[] cedePointsArray, Double[] differenceArray, Double[] differenceArraymin) {
            Double cedePoints = Double.valueOf(this.cedePoints);
            Double max = Math.max(cedePointsArray[0], cedePointsArray[1]);
            Double min = Math.min(cedePointsArray[0], cedePointsArray[1]);
            int offest = (int) (cedePoints / 5);
            if (cedePoints > 0) {
                for (int i = 0; i < 12; i++) {
                    if (i < 6 + offest) {
                        differenceArray[i] = differenceArray[i] + cedePointsArray[0];
                        differenceArraymin[i] = differenceArraymin[i] + cedePointsArray[0];
                    } else if (i > 6 + offest) {
                        differenceArray[i] = differenceArray[i] + cedePointsArray[1];
                        differenceArraymin[i] = differenceArraymin[i] + cedePointsArray[1];
                    } else {
                        differenceArray[i] = differenceArray[i] + max;
                        differenceArraymin[i] = differenceArraymin[i] + min;
                    }
                }
            } else {
                for (int i = 0; i < 12; i++) {
                    if (i < -offest || i > 5) {
                        differenceArray[i] = differenceArray[i] + cedePointsArray[1];
                        differenceArraymin[i] = differenceArraymin[i] + cedePointsArray[1];
                    } else if (i < 6 && i > -offest) {
                        differenceArray[i] = differenceArray[i] + cedePointsArray[0];
                        differenceArraymin[i] = differenceArraymin[i] + cedePointsArray[0];
                    } else {
                        differenceArray[i] = differenceArray[i] + max;
                        differenceArraymin[i] = differenceArraymin[i] + min;
                    }
                }
            }
        }

        private void sumsizeOddsanddifference(Double[] sizeArray, Double[] differenceArray, Double[] differenceArraymin) {
            Double max = Math.max(sizeArray[0], sizeArray[1]);
            Double min = Math.min(sizeArray[0], sizeArray[1]);
            for (int i = 0; i < 12; i++) {
                differenceArray[i] = differenceArray[i] + max;
                differenceArraymin[i] = differenceArraymin[i] + min;
            }
        }

        private void setimlist(List<Double> list, Double[] oddsArray) {
            for (int i = 0; i < oddsArray.length; i++) {
                if (oddsArray[i] != 0.0) {
                    list.add(oddsArray[i]);
                }
            }
        }

        public List<Double>[] getoddsinallodds(List<Map<String, Object>> winNegativeOddsList, List<Map<String, Object>> cedePointsOddsList, List<Map<String, Object>> sizeOddsList, List<Map<String, Object>> differenceOddsList) {
            List<Double>[] list = new ArrayList[2];
            list[0] = new ArrayList<>();
            list[1] = new ArrayList<>();
            String[] winNegativeOdds = {"主胜", "主负"};
            String[] cedePointsOdds = {"主胜", "主负"};
            String[] sizeOdds = {"大", "小"};
            String[] differenceOdds = {"主胜1-5", "主胜6-10", "主胜11-15", "主胜16-20", "主胜21-25", "主胜26+", "主负1-5", "主负6-10", "主负11-15", "主负16-20", "主负21-25", "主负26+"};
            //每项的值
            Double[] winNegaArray = getoddsintype(winNegativeOddsList, winNegativeOdds);
            Double[] cedePointsArray = getoddsintype(cedePointsOddsList, cedePointsOdds);
            Double[] sizeArray = getoddsintype(sizeOddsList, sizeOdds);
            Double[] differenceArray = getoddsintype(differenceOddsList, differenceOdds);
            Double[] differenceArraymin = getoddsintype(differenceOddsList, differenceOdds);
            //记录比分表特殊节点

            sumdifferenceOddsandwinNegativeOdds(winNegaArray, differenceArray, differenceArraymin);
            sumsizeOddsanddifference(sizeArray, differenceArray, differenceArraymin);
            sumdifferenceOddsandcedePoints(cedePointsArray, differenceArray, differenceArraymin);
            setimlist(list[0], differenceArray);
            setimlist(list[1], differenceArraymin);
            if (list[1].size() == 0) {
                list[1] = list[0];
            }
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

        public basketballMatchDTOall(BasketballMatchDTO basketballMatchDTO) {
            this.id = basketballMatchDTO.getId();
            this.number = basketballMatchDTO.getNumber();
            this.match = basketballMatchDTO.getMatch();
            this.homeTeam = basketballMatchDTO.getHomeTeam();
            this.visitingTeam = basketballMatchDTO.getVisitingTeam();
            this.length = basketballMatchDTO.getDifferenceOddsList().size() + basketballMatchDTO.getSizeOddsList().size() + basketballMatchDTO.getWinNegativeOddsList().size() + basketballMatchDTO.getCedePointsOddsList().size();
            this.cedePoints = basketballMatchDTO.getCedePoints();
            List<Double>[] OddsList = new ArrayList[2];
            OddsList = getoddsinallodds(basketballMatchDTO.getWinNegativeOddsList(), basketballMatchDTO.getCedePointsOddsList(), basketballMatchDTO.getSizeOddsList(), basketballMatchDTO.getDifferenceOddsList());
            setMaxodds(OddsList[0]);
            setMinodds(OddsList[1]);
        }
    }

    /**
     * 出奖结果计算
     *
     * @param basketballMatchList 用户选中的数组
     * @param multiple            倍数
     * @param pssTypeList         过关类型
     * @param str                 开奖结果固定形式
     *                            第一场开奖结果： 主负,主胜,主负6-10,大,85:59
     *                            第二场开奖结果： 主胜,主负,主胜6-10,大,56:59
     *                            第三场开奖结果： 主胜,主负,主胜1-5,大,85:95
     * @return 用户中了多少注把中的每一注金额相加在进行返回
     */
    public static Double award(List<BasketballMatchDTO> basketballMatchList, Integer multiple, List<Integer> pssTypeList, List<String> str) {
        Double res = 0.0;
        String[][] AllFootconse = new String[str.size()][4];
        List<List<Map<String, String>>> hasFootconse = new ArrayList<>();
        for (int i = 0; i < str.size(); i++) {
            String[] eachid = new String[5];
            AllFootconse[i] = str.get(i).split(",");

            String[] bif = AllFootconse[i][4].split(":");
            int zsc = Integer.valueOf(bif[1]);
            int ksc = Integer.valueOf(bif[0]);
            Double offset = Double.valueOf(basketballMatchList.get(i).getCedePoints());
            if (offset + zsc > ksc) {
                AllFootconse[i][1] = "主胜";
            } else if (offset + zsc < ksc) {
                AllFootconse[i][1] = "主负";
            }

            if (!basketballMatchList.get(i).getSizeOddsList().isEmpty()) {
                Double yusc = Double.valueOf(basketballMatchList.get(i).getSizeOddsList().get(0).get("score").toString());
                if (zsc + ksc > yusc) {
                    AllFootconse[i][3] = "大";
                } else if (zsc + ksc < yusc) {
                    AllFootconse[i][3] = "小";
                }
            }

            eachid[0] = Findoddsbytype(basketballMatchList.get(i).getWinNegativeOddsList(), AllFootconse[i][0]);
            eachid[1] = Findoddsbytype(basketballMatchList.get(i).getCedePointsOddsList(), AllFootconse[i][1]);
            eachid[2] = Findoddsbytype(basketballMatchList.get(i).getDifferenceOddsList(), AllFootconse[i][2]);
            eachid[3] = Findoddsbytype(basketballMatchList.get(i).getSizeOddsList(), AllFootconse[i][3]);
            if ((eachid[0] + eachid[1] + eachid[2] + eachid[3]).equals("")) {
                str.remove(i);
                basketballMatchList.remove(i);
                i--;
            } else {
                List<Map<String, String>> realFoot = new ArrayList<>();
                for (int j = 0; j < 4; j++) {
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
            return 0.0;
        }
        List<Map<String, String>>[] realFoot = new ArrayList[basketballMatchList.size()];
        for (int i = 0; i < basketballMatchList.size(); i++) {
            realFoot[i] = hasFootconse.get(i);
        }
        for (int i = 0; i < pssTypeList.size(); i++) {
            int[][] Indexgroup = getIndexgroup(basketballMatchList.size(), pssTypeList.get(i));
            List<List<Map<String, String>>> realallop = getallop(realFoot, Indexgroup);
            res = res + getAllbonus(realallop);
        }
        return res * multiple * 2;
    }

    public static String Findoddsbytype(List<Map<String, Object>> chose, String realoutcome) {
        String res = "";
        for (int i = 0; i < chose.size(); i++) {
            if (chose.get(i).get("describe").toString().equals(realoutcome)) res = chose.get(i).get("odds").toString();
        }
        return res;
    }

    public static Double getAllbonus(List<List<Map<String, String>>> realallop) {
        Double res = 0.0;
        for (int i = 0; i < realallop.size(); i++) {
            Double z = 1.0;
            for (int j = 0; j < realallop.get(i).size(); j++) {
                z = z * Double.parseDouble(realallop.get(i).get(j).get("odds"));
            }
            res = res + z;
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

    public static void awardSchemeDetails(List<SportSchemeDetailsListVO> sportsDetails, Map<String, BasketballMatchDO> awardMap) {

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

                    BasketballMatchDO match = awardMap.get(keyEntity.getKey());
                    //官方开奖结果
                    // 主负,主负,主负11-15,大
                    //String[] allfootconse = StringUtils.split(match.getAward(), ",");
                    //比分,120:108
                    String[] scores = match.getHalfFullCourt().split(":");
                    String odd = getOddByResult(content, scores);
                    if (StringUtils.isNotBlank(odd)) {
                        resultOddsList.add(odd);
                    }
                }
                if (resultOddsList.size() == matchMaps.size()) {
                    //中了
                    BigDecimal money = FootballUtil.sumItem(resultOddsList).multiply(BigDecimal.valueOf(Integer.valueOf(detailsVOS.getNotes())));
                    detailsVOS.setMoney("" + money.setScale(2, RoundingMode.HALF_DOWN));
                    detailsVOS.setAward(true);
                }
            }
        }

    }

    static String getOddByResult(String bets, String[] score) {
        //赛果
        int a = Integer.valueOf(score[0]);//客队
        int b = Integer.valueOf(score[1]);//主队
        //让分胜负
        if (bets.indexOf("[") > 0 && bets.indexOf("让") > -1) {
            int idx = bets.indexOf("[");
            int last = bets.indexOf("]");
            String bet = bets.substring(0, idx);
            String rf = bets.substring(idx + 1, last);
            String odd = bets.substring(last + 2, bets.length() - 1);
            if (a - Integer.valueOf(rf) > b) {
                //主负
                if ("主负".equals(bet)) {
                    return odd;
                }
            } else {
                if ("主胜".equals(bet)) {
                    return odd;
                }
            }
        } else if (bets.indexOf("[") > 0) {
            //大小分 大[241.5](1.70)
            int idx = bets.indexOf("[");
            int last = bets.indexOf("]");
            String bet = bets.substring(0, idx);
            String rf = bets.substring(idx + 1, last);
            String odd = bets.substring(last + 2, bets.length() - 1);
            if (a + b > Integer.valueOf(rf)) {
                if ("大".equals(bet)) {
                    return odd;
                }
            } else {
                if ("小".equals(bet)) {
                    return odd;
                }
            }
        }

        //胜负
        String win = "";
        if (a < b) {
            win = "主胜";
        } else if (a > b) {
            win = "主负";
        }
        //胜分差   主负16-20(9.6) 主胜1-5(5.5) 主负26+(19.00)
        String sub = (a > b ? "主负" : "主胜") + getChaName(a - b);
        if (bets.startsWith(sub)) {
            int idx = bets.indexOf("(");
            return bets.substring(idx + 1, bets.length() - 1);
        }
        int idx = bets.indexOf("(");
        int last = bets.indexOf(")");
        String bet = bets.substring(0, idx);
        if (win.equals(bet)) {
            return bets.substring(idx + 1, last);
        }
        return "";
    }

    private static String getChaName(int cha) {
        int c = Math.abs(cha);
        if (c <= 5) {
            return "1-5";
        } else if (c <= 10) {
            return "6-10";
        } else if (c <= 15) {
            return "11-15";
        } else if (c <= 20) {
            return "16-20";
        } else if (c <= 25) {
            return "21-25";
        } else {
            return "26+";
        }
    }

    static String[] getBetContentAndOddFromContent(String content) {
        int idx = content.indexOf("(");
        int last = content.indexOf(")");
        String[] odds = new String[2];
        odds[0] = content.substring(0, idx);
        odds[1] = content.substring(idx + 1, last);
        return odds;
    }


    /**
     * 串关 拆分票据
     */
    public static List<LotteryTicketDO> getBasketTicketVO(List<BasketballMatchDTO> footballMatchDTOS, List<Integer> playType, String orderNo, Integer times) {
        List<LotteryTicketDO> lotteryTicketDOS = new ArrayList<>(footballMatchDTOS.size() * playType.size());
        //串一过关
        String[] matchArrays = new String[footballMatchDTOS.size()];
        int idx = 0;
        Map<String, ArrayList<TicketVO>> ticketMap = new HashMap<>(footballMatchDTOS.size());
        for (BasketballMatchDTO dto : footballMatchDTOS) {
            ArrayList<TicketVO> ticketVOS = buildTicketVO(dto);
            matchArrays[idx] = dto.getNumber();
            ticketMap.put(dto.getNumber(), ticketVOS);
            idx++;
        }
        for (Integer play : playType) {
            List<List<String>> lists = CombinationUtil.getCombinations(matchArrays, play);
            lotteryTicketDOS.addAll(replaceTicketVO(lists, ticketMap, orderNo, play, times));
        }
        idx = 1;
        for (LotteryTicketDO p : lotteryTicketDOS) {
            p.setTicketState(0);
            p.setCreateTime(new Date());
            p.setRevokePrice(BigDecimal.ZERO);
            p.setState(0);
            p.setTicketNo(String.valueOf(idx++));
        }
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
        //  int multiPer = times % Constant.MAX_TICKET_MULTI == 0 ? times / Constant.MAX_TICKET_MULTI : times / Constant.MAX_TICKET_MULTI + 1;
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

//                for (int i = 0; i < multiPer; i++) {
//                    int mult = Constant.MAX_TICKET_MULTI;
//                    if (i == multiPer - 1) {
//                        //最后一票倍数
//                        mult = times % Constant.MAX_TICKET_MULTI;
//                    }
                int mult = times;
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
                //}
            }


        }


        return lotteryTicketDOS;
    }

    private static ArrayList<TicketVO> buildTicketVO(BasketballMatchDTO dto) {
        ArrayList<TicketVO> ticketVOList = new ArrayList<>(100);
        TicketVO ticketVO = new TicketVO();
        ticketVO.setMatch(dto.getMatch());
        ticketVO.setLetBall(dto.getCedePoints());
        ticketVO.setNumber(dto.getNumber());
        ticketVO.setHomeTeam(dto.getHomeTeam());
        ticketVO.setVisitingTeam(dto.getVisitingTeam());
        List<TicketContentVO> ticketContentVOList = new ArrayList<>(100);
        //主胜,主负,主胜1-5,小 胜负，让球胜负，胜分差，大小分
        if (!CollectionUtils.isEmpty(dto.getWinNegativeOddsList())) {
            ticketContentVOList = new ArrayList<>();
            ticketVO.setMode("0");
            for (Map<String, Object> list : dto.getWinNegativeOddsList()) {
                TicketContentVO ticketContentVO = JSON.parseObject(JSON.toJSONString(list), TicketContentVO.class);
                ticketContentVO.setMode("0");
                ticketContentVOList.add(ticketContentVO);
            }
            TicketVO newTicket = new TicketVO();
            BeanUtils.copyProperties(ticketVO, newTicket);
            newTicket.setTicketContentVOList(ticketContentVOList);
            ticketVOList.add(newTicket);
        }
        if (!CollectionUtils.isEmpty(dto.getCedePointsOddsList())) {
            ticketContentVOList = new ArrayList<>();
            ticketVO.setMode("1");
            for (Map<String, Object> list : dto.getCedePointsOddsList()) {
                TicketContentVO ticketContentVO = JSON.parseObject(JSON.toJSONString(list), TicketContentVO.class);
                ticketContentVO.setMode("1");
                ticketContentVO.setLetball(dto.getCedePoints());
                ticketContentVO.setDescribe("让" + ticketContentVO.getDescribe());
                ticketContentVOList.add(ticketContentVO);
            }
            TicketVO newTicket = new TicketVO();
            BeanUtils.copyProperties(ticketVO, newTicket);
            newTicket.setTicketContentVOList(ticketContentVOList);
            ticketVOList.add(newTicket);
        }
        if (!CollectionUtils.isEmpty(dto.getDifferenceOddsList())) {
            ticketContentVOList = new ArrayList<>();
            ticketVO.setMode("2");
            for (Map<String, Object> list : dto.getDifferenceOddsList()) {
                TicketContentVO ticketContentVO = JSON.parseObject(JSON.toJSONString(list), TicketContentVO.class);
                ticketContentVO.setMode("2");
                ticketContentVOList.add(ticketContentVO);
            }
            TicketVO newTicket = new TicketVO();
            BeanUtils.copyProperties(ticketVO, newTicket);
            newTicket.setTicketContentVOList(ticketContentVOList);
            ticketVOList.add(newTicket);
        }
        if (!CollectionUtils.isEmpty(dto.getSizeOddsList())) {
            ticketContentVOList = new ArrayList<>();
            ticketVO.setMode("3");
            for (Map<String, Object> list : dto.getSizeOddsList()) {
                TicketContentVO ticketContentVO = JSON.parseObject(JSON.toJSONString(list), TicketContentVO.class);
                ticketContentVO.setMode("3");
                ticketContentVO.setLetball(list.containsKey("score") ? list.get("score").toString() : "");
                ticketContentVOList.add(ticketContentVO);
            }
            TicketVO newTicket = new TicketVO();
            BeanUtils.copyProperties(ticketVO, newTicket);
            newTicket.setTicketContentVOList(ticketContentVOList);
            ticketVOList.add(newTicket);
        }
        return ticketVOList;
    }

    public static List<LotteryTicketDO> getBasketTicketVOBySechme(String schedetail, List<BasketballMatchDTO> footballMatchDTOS, String orderNo, String mode) {
        List<SportSchemeDetailsListVO> sportsDetails = JSON.parseArray(schedetail, SportSchemeDetailsListVO.class);
        Map<String, BasketballMatchDTO> matchMap = footballMatchDTOS.stream().collect(Collectors.toMap(BasketballMatchDTO::getNumber, a -> a));
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

    public static LotteryTicketDO replaceTicketVOBySechmeBallList(List<SportSchemeDetailsVO> detailsVOS, Map<String, BasketballMatchDTO> footballMatchDTOMap, String orderNo, Integer times, Integer idx, String mode) {
        LotteryTicketDO lotteryTicketDO = new LotteryTicketDO();
        List<TicketVO> ticketVOList = new ArrayList<>();
        BigDecimal foreast = BigDecimal.ONE;
        List<String> matchList = new ArrayList<>();
        for (SportSchemeDetailsVO detailsVO : detailsVOS) {
            BasketballMatchDTO footballMatchDTO = footballMatchDTOMap.get(detailsVO.getNumber());
            matchList.add(footballMatchDTO.getNumber());
            TicketVO ticketVO = new TicketVO();
            ticketVO.setMode(mode);
            ticketVO.setLetBall(footballMatchDTO.getCedePoints());
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
            ticketContentVO.setLetball(content[2]);
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
        int idx2 = result.indexOf("[");
        int lastIdx2 = result.lastIndexOf("]");
        return new String[]{idx2 > 0 ? result.substring(0, idx2) : result.substring(0, idx), result.substring(idx + 1, lastIdx), (idx2 > 0) ?
                result.substring(idx2 + 1, lastIdx2) : ""};
    }

    /**
     * 按票 开奖
     * 传比分进来
     * 篮球比分，，主队在后 ，客队在前。
     * 主队分+让球
     */

    public static BonuseVO award(List<LotteryTicketDO> ticketDOList, Map<String, String> resultMap) {
        BonuseVO bonuseVO = BonuseVO.builder().build();
        if (CollectionUtils.isEmpty(ticketDOList)) {
            return bonuseVO;
        }
        for (LotteryTicketDO ticketDO : ticketDOList) {
            List<TicketVO> ticketDTOList = JSONUtil.toList(ticketDO.getTicketContent(), TicketVO.class);
            List<String> oddsList = new ArrayList<>();
            boolean delayMatch = false;
            for (TicketVO ticketVO : ticketDTOList) {
                String awardResult = resultMap.get(ticketVO.getNumber());
                //球队
                //赛果 负,胜,7,负-负,负其它
                if ("延期".equals(awardResult)) {
                    //此注本金还还。
                    delayMatch = true;
                    break;
                } else {
                    List<TicketContentVO> ticketContentVOList = ticketVO.getTicketContentVOList();
                    for (TicketContentVO contentVO : ticketContentVOList) {
                        getAwardDescript(contentVO, awardResult);
                        if (contentVO.getShoted()) {
                            oddsList.add(contentVO.getOdds());
                        }
                    }
                }
            }
            //更新
            ticketDO.setTicketContent(JSONUtil.toJsonStr(ticketDTOList));
            if (delayMatch) {
                //延期返本金
                ticketDO.setWinPrice(ticketDO.getPrice());
                if (ticketDO.getTicketState() != 2) {
                    ticketDO.setState(3);
                    bonuseVO.setShoted(true);
                    bonuseVO.setMoney(bonuseVO.getMoney().add(ticketDO.getPrice()));
                }
            } else if (ticketDTOList.size() == oddsList.size()) {
                BigDecimal price = FootballUtil.sumItem(oddsList);//不乘倍数，考虑倍数有减少行为，导致倍数为0
                ticketDO.setWinPrice(price.setScale(2, RoundingMode.HALF_UP));
                if (ticketDO.getTicketState() != 2) {
                    ticketDO.setState(3);
                    ticketDO.setWinPrice(price.multiply(BigDecimal.valueOf(ticketDO.getTimes())).setScale(2, RoundingMode.HALF_UP));
                    bonuseVO.setShoted(true);
                    bonuseVO.setBonus(bonuseVO.getBonus().add(ticketDO.getWinPrice()));
                }
            } else {
                ticketDO.setState(2);
            }
        }
        return bonuseVO;
    }


    /**
     * 竞蓝  0 胜负 1 让分胜负 2 胜分差 3 大小分
     * 主负,主负,客胜26+,1
     *
     * @param contentVO
     * @param awardResult
     * @return
     */
    private static String getAwardDescript(TicketContentVO contentVO, String awardResult) {
        String[] resultArys = StringUtils.split(awardResult, ":");
        Integer home = Integer.valueOf(resultArys[1]);
        Integer vist = Integer.valueOf(resultArys[0]);
        String r = "";
        switch (contentVO.getMode()) {
            case "0":
                r = home > vist ? "主胜" : (home < vist ? "主负" : "主平");
                if (contentVO.getDescribe().equals(r)) {
                    contentVO.setShoted(true);
                }
                break;
            case "1":
                if (home + Double.valueOf(contentVO.getLetball()) > vist) {
                    r = "让主胜";
                } else if (home + Double.valueOf(contentVO.getLetball()) < vist) {
                    r = "让主负";
                } else {
                    r = "让主平";
                }
                if (r.equals(contentVO.getDescribe())) {
                    contentVO.setShoted(true);
                }
                break;
            case "2":
                int cha = Math.abs(home - vist);
                if (home > vist) {
                    //主胜
                    r = "0" + ((cha % 5) + 1);
                } else if (home < vist) {
                    //客胜
                    r = "1" + ((cha % 5) + 1);
                }
                r = LotteryProcessor.ARRAYS_MAP.get(r);
                if (r.equals(contentVO.getDescribe())) {
                    contentVO.setShoted(true);
                }
                break;
            case "3":
                if (home + vist > Double.valueOf(contentVO.getLetball())) {
                    r = "大";
                } else if (home + vist < Double.valueOf(contentVO.getLetball())) {
                    r = "小";
                }
                if (r.equals(contentVO.getDescribe())) {
                    contentVO.setShoted(true);
                }
                break;
        }
        return "";
    }

}
