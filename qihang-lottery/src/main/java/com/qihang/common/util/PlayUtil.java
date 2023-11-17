/**
 * <pre>
 * ContentUtil_34_301.java
 * Copyright (c) 2013 YTX.INC
 * </pre>
 */
package com.qihang.common.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @date 2013年10月25日
 */
public final class PlayUtil {

    private PlayUtil() {
    }

    /**
     * 获取排列数(重复)
     *
     * @param n
     * @param m
     * @return
     */
    public static int getPermutationRepeat(int n, int m) {
        int c = n;
        for (int i = 1; i < m; i++, c *= n)
            ;
        return c;
    }

    /**
     * 获取排列数
     *
     * @param n
     * @param m
     * @return
     */
    public static int getPermutation(int n, int m) {
        if (m > n)
            throw new IllegalArgumentException("m > n!");
        int c = 1;
        for (int i = n - m; i < n; c *= ++i)
            ;
        return c;
    }

    /**
     * 获取组合数
     *
     * @param m
     * @param n
     * @return
     */
    public static long getCombination(long n, int m) {
        if (m > n) {
            throw new IllegalArgumentException("m > n!");
        }
        long n1 = 1, n2 = 1;
        for (long i = n, j = 1; j <= m; n1 *= i--, n2 *= j++)
            ;
        return n1 / n2;
    }

    /**
     * 获取组合数(重复)
     *
     * @param m
     * @param n
     * @return
     */
    public static long getCombinationRepeat(long n, int m) {
        if (m > n) {
            throw new IllegalArgumentException("m > n!");
        }
        long n1 = 1, n2 = 1;
        for (long i = m + n - 1, j = 1; j <= m; n1 *= i--, n2 *= j++)
            ;
        return n1 / n2;
    }

    private static long combination(int n, int m) {
        if (n < m) {
            return 0;
        }
        return getCombination(n, m);
    }

    /**
     * 获取中奖组合数
     *
     * @param award    中奖数
     * @param awardcnt 已中奖数
     * @param betcnt   投注数
     * @param base     基础数
     * @return
     */
    public static long getAwardCount(int award, int awardcnt, int betcnt,
                                     int base) {
        if (betcnt < awardcnt) {
            throw new IllegalArgumentException("betcnt<awardcnt!");
        }
        if (base < award) {
            throw new IllegalArgumentException("base<award!");
        }
        return combination(awardcnt, award)
                * combination(betcnt - awardcnt, base - award);
    }

    /**
     * 获取胆拖类中奖数
     *
     * @param award 必须中几个
     * @param dm_cnt 胆码个数
     * @param tm_cnt 拖码个数
     * @param dm_award 胆码中几个
     * @param tm_award 拖码中几个
     * @param base    几个为一注
     * @return
     */
    public static long getAwardDTCount(int award, int dm_cnt, int tm_cnt,
                                       int dm_award, int tm_award, int base) {
        int dtbase = base - dm_cnt;
        int dtaward = award - dm_award;
        if (dtaward < 0) {
            return 0;
        }
        if (dtbase < dtaward) { // 不够容纳
            return 0;
        }
        if (tm_cnt < tm_award) { // 不够排
            return 0;
        }
        return getAwardCount(dtaward, tm_award, tm_cnt, dtbase);
    }

    /**
     * 组合排列拆分回调函数，避免内存过高
     *
     * @author Hope
     * @date 2013年12月13日
     */
    public static interface ContentCallBack {
        void callback(String[] content);
    }

    /**
     * 排列拆单式
     *
     * @param arr
     * @return
     */
    public static List<String[]> splitPermutation(String[] arr) {
        List<String[]> list = new LinkedList<String[]>();
        permutation(arr, arr.length, list, new String[arr.length], 0, null);
        return list;
    }

    /**
     * 排列拆单式
     *
     * @param arr
     * @return
     */
    public static void splitPermutation(String[] arr, ContentCallBack callback) {
        permutation(arr, arr.length, null, new String[arr.length], 0, callback);
    }

    private static void permutation(String[] fsarr, int m, List<String[]> list,
                                    String[] arr, int layer, ContentCallBack callback) {
        if (layer >= m)
            return;
        char[] nums = fsarr[layer].toCharArray();
        for (char num : nums) {
            arr[layer] = num + "";
            if (layer >= m - 1) {
                int length = arr.length;
                String[] narr = new String[length];
                System.arraycopy(arr, 0, narr, 0, length);
                if (callback != null) {
                    callback.callback(narr);
                } else if (list != null) {
                    list.add(narr);
                }
            } else {
                permutation(fsarr, m, list, arr, layer + 1, callback);
            }
        }
    }

    /**
     * 组合拆单式
     *
     * @param arr
     * @param m
     * @return
     */
    public static void splitCombination(String[] arr, int m,
                                        ContentCallBack callback) {
        String[] t = new String[arr.length];
        _splitCombination(arr, m, null, t, 0, 0, callback);
    }

    /**
     * 组合拆单式
     *
     * @param arr
     * @param m
     * @return
     */
    public static List<String[]> splitCombination(String[] arr, int m) {
        List<String[]> list = new LinkedList<String[]>();
        String[] t = new String[arr.length];
        _splitCombination(arr, m, list, t, 0, 0, null);
        return list;
    }

    private static void _splitCombination(String[] arr, int m,
                                          List<String[]> list, String[] t, int start, int layer,
                                          ContentCallBack callback) {
        int length = arr.length;
        for (int i = start; i < length; i++) {
            t[layer] = arr[i];
            if (layer >= m - 1) {
                String[] narr = new String[m];
                System.arraycopy(t, layer - (m - 1), narr, 0, m);
                if (callback != null) {
                    callback.callback(narr);
                } else if (list != null) {
                    list.add(narr);
                }
            } else {
                _splitCombination(arr, m, list, t, i + 1, layer + 1, callback);
            }
        }
    }

    /**
     * <pre>
     * 比较数组是否包含相等，即number包含awardnumber的号码
     * 注意：number为多个数字组成，且number范围为(0~9),且不重复
     * </pre>
     */
    public static boolean equals_multnum(String[] awardNumbers,
                                         String[] numbers, int awardStart, int numStart, int count) {
        if (awardNumbers == null || numbers == null)
            return false;
        int end = numStart + count;
        for (int i = numStart, ai = awardStart; i < end; i++, ai++)
            if (!numbers[i].contains(awardNumbers[ai]))
                return false;
        return true;
    }

    /**
     * 比较数组是否相同
     */
    public static boolean equals(String[] awardNumbers, String[] numbers,
                                 int awardStart, int numStart, int count) {
        if (awardNumbers == null || numbers == null)
            return false;
        int end = numStart + count;
        for (int i = numStart, ai = awardStart; i < end; i++, ai++)
            if (!numbers[i].equals(awardNumbers[ai]))
                return false;
        return true;
    }

    /**
     * 比较数组是否相同
     */
    public static boolean equals(int[] awardNumbers, int[] numbers,
                                 int awardStart, int numStart, int count) {
        if (awardNumbers == null || numbers == null)
            return false;
        int end = numStart + count;
        for (int i = numStart, ai = awardStart; i < end; i++, ai++)
            if (numbers[i] != awardNumbers[ai])
                return false;
        return true;
    }

    /**
     * 获取和值数
     *
     * @param awardNumbers
     * @param awardStart
     * @param count
     * @return
     */
    public static int sum(String[] awardNumbers, int awardStart, int count) {
        int sum = 0;
        for (int i = awardStart, n = awardStart + count; i < n; i++) {
            sum += Integer.parseInt(awardNumbers[i]);
        }
        return sum;
    }

    /**
     * 检查numbers内容中是否包含了number
     *
     * @param numbers
     * @param number
     * @return
     */
    public static boolean contains(String[] numbers, String number) {
        // return ArrayUtils.contains(numbers, number);
        for (String num : numbers) {
            int numInt = Integer.parseInt(num);
            int num2Int = Integer.parseInt(number);
            if (numInt == num2Int) {
                return true;
            }
        }
        return false;
    }

    /**
     * <pre>
     * 获取awardNumbers数组中包含了多少numbers中的内容
     * 注意：投注号码>=50的不要使用该函数
     * </pre>
     *
     * @param awardNumbers
     * @param numbers
     * @return
     */
    public static int getContainCount(String[] awardNumbers, String[] numbers) {
        return getContainCount(awardNumbers, numbers, 0, awardNumbers.length);
    }

    /**
     * <pre>
     * 获取awardNumbers数组中包含了多少numbers中的内容
     * 注意：投注号码>=50的不要使用该函数
     * </pre>
     *
     * @param awardNumbers
     * @param numbers
     * @return
     */
    public static int getContainCount(String[] awardNumbers, String[] numbers,
                                      int awardStart, int count) {
        int[] numflag = new int[50]; // 目前数字号码没有超过50的
        for (int i = awardStart, n = awardStart + count; i < n; i++) {
            numflag[Integer.parseInt(awardNumbers[i])]++;
        }
        int cnt = 0;
        for (String num : numbers) {
            int numInt = Integer.parseInt(num);
            if (numflag[numInt] > 0) {
                cnt++;
                numflag[numInt]--;
            }
        }
        return cnt;
    }

    /**
     * <pre>
     * 获取awardNumbers数组中包含了多少numbers中的内容
     * 注意：投注号码>=50的不要使用该函数
     * 该方法只支持number(0~9),且不重复
     * </pre>
     *
     * @param awardNumbers
     * @param numbers
     * @return
     */
    public static int getContainCount_multnum(String[] awardNumbers,
                                              String[] numbers) {
        return getContainCount_multnum(awardNumbers, numbers, 0,
                awardNumbers.length);
    }

    /**
     * <pre>
     * 获取awardNumbers数组中包含了多少numbers中的内容
     * 注意：投注号码>=50的不要使用该函数
     * 该方法只支持number(0~9),且不重复
     * </pre>
     *
     * @param awardNumbers
     * @param numbers
     * @return
     */
    public static int getContainCount_multnum(String[] awardNumbers,
                                              String[] numbers, int awardStart, int count) {
        int[] numflag = new int[50]; // 目前数字号码没有超过50的
        for (int i = awardStart, n = awardStart + count; i < n; i++) {
            numflag[Integer.parseInt(awardNumbers[i])]++;
        }
        int cnt = 0;
        for (String num : numbers) {
            char[] charArray = num.toCharArray();
            for (char ch : charArray) {
                int numInt = (int) ch - 48;
                if (numflag[numInt] > 0) {
                    cnt++;
                    numflag[numInt]--;
                    break;
                }
            }
        }
        return cnt;
    }

    /**
     * 根据len分割number字符串
     *
     * @param number
     * @param len
     * @return
     */
    public static String[] splitNumber(String number, int len) {
        if (len < 1) {
            throw new IllegalArgumentException("len<1!");
        }
        int size = number.length() / len;
        String[] numbers = new String[size];
        for (int i = 0, t = 0; i < size; i++, t += len) {
            numbers[i] = number.substring(t, t + len);
        }
        return numbers;
    }

    /**
     * 截取数组
     *
     * @param
     * @param
     * @return
     */
    public static String[] subarray(String[] numbers, int start, int count) {
        String[] newNumbers = new String[count];
        System.arraycopy(numbers, start, newNumbers, 0, count);
        return newNumbers;
    }

    /**
     * 获取最大的球
     *
     * @param
     */
    public static int maxBall(int[] balls) {
        int max = -1;
        if (balls != null) {
            for (int ball : balls) {
                if (ball > max) {
                    max = ball;
                }
            }
        }
        return max;
    }

    /**
     * 获取最大的球
     *
     * @param
     */
    public static int maxNumber(String[] balls) {
        int max = -1;
        if (balls != null) {
            for (String ball : balls) {
                int num = Integer.parseInt(ball);
                if (num > max) {
                    max = num;
                }
            }
        }
        return max;
    }

    /**
     * 验证内容是否重复，内容必须为数字性
     *
     * @param
     */
    public static boolean isRepeat(int[] balls) {
        // 验证重复数字
        if (balls != null && balls.length > 0) {
            int[] nums = new int[maxBall(balls) + 1]; // 空间换时间，丢弃第一个位
            for (int ball : balls) {
                if (nums[ball] > 0) {
                    return true;
                }
                nums[ball]++;
            }
        }
        return false;
    }

    /**
     * 获取最大重复次数
     */
    public static int getRepeatCount(int[] balls) {
        int cnt = 0;
        if (balls != null && balls.length > 0) {
            int[] nums = new int[maxBall(balls) + 1]; // 空间换时间，丢弃第一个位
            for (int ball : balls) {
                int tmp = ++nums[ball];
                if (tmp > cnt) {
                    cnt = tmp;
                }
            }
        }
        return cnt;
    }

    /**
     * 获取最大重复次数
     */
    public static int getRepeatCount(String[] balls) {
        return getRepeatCount(convertIntArray(balls));
    }

    /**
     * 验证内容是否重复，内容必须为数字性
     *
     * @param
     */
    public static boolean isRepeat(String[] balls) {
        return isRepeat(convertIntArray(balls));
    }

    /**
     * <pre>
     * 直选复式拆分(数字可重复)，排列
     * PS: 可重复选则一个号码的直选复式拆分，如时时彩3星直选复式
     *
     * <pre>
     * @param
     * @param
     * @return
     */
    public static int getMulitCompoundSplitCount(Object[][] mulitBets) {
        int number = 1;
        for (Object[] bets : mulitBets) {
            number *= bets.length;
        }
        return number;
    }

    /**
     * <pre>
     * 直选复式拆分(每位数字互不重复)
     * PS: 每位数字互不重复，如11选5前三直选复式
     *
     * <pre>
     * @param
     * @param
     * @return
     */
    public static int getMulitCompoundUniqueSplitCount(Object[][] mulitBets) {
        int layerCnt = mulitBets.length;
        Object[] selectBets = new Object[layerCnt];
        return _getMulitCompoundUniqueSplitCount(1, layerCnt, selectBets,
                mulitBets);
    }

    private static int _getMulitCompoundUniqueSplitCount(int layer,
                                                         int layerCount, Object[] selectBets, Object[][] mulitBets) {
        int layerIndex = layer - 1;
        Object[] layerBets = mulitBets[layerIndex];
        // 循环本层号码
        int number = 0;
        betLoop:
        for (Object bet : layerBets) {
            // 如果当前号码与前面选择号码重复则跳过
            for (int i = 0, n = layerIndex; i < n; i++) {
                if (bet.equals(selectBets[i])) {
                    // 如果相等则跳过此轮号码
                    continue betLoop;
                }
            }
            // 不重复且到最后一层号码则+1
            if (layer >= layerCount) {
                selectBets[layerIndex] = bet;
                number++;
            } else {
                // 进入下一层
                selectBets[layerIndex] = bet; // 记录当前层选择号码
                number += _getMulitCompoundUniqueSplitCount(layer + 1,
                        layerCount, selectBets, mulitBets);
            }
        }
        return number;
    }

    /**
     * <pre>
     * 拆分胆拖注数（组合类）
     * PS: 例子，11选5 “任选胆拖”
     * </pre>
     *
     * @param dms       胆码
     * @param tms       拖码
     * @param chooseCnt 该玩法的选球数
     * @return
     */
    public static long getTowedSplitCount(Object[] dms, Object[] tms,
                                          int chooseCnt) {
        return getCombination(tms.length, chooseCnt - dms.length);
    }

    /**
     * <pre>
     * 拆分胆拖注数(排列类)
     * PS: 例子，11选5 “前二，前三直选胆拖”
     * </pre>
     *
     * @param dms       胆码
     * @param tms       拖码
     * @param chooseCnt 该玩法的选球数
     * @return
     */
    public static long getTowedSequenceSplitCount(Object[] dms, Object[] tms,
                                                  int chooseCnt) {
        return getCombination(tms.length, chooseCnt - dms.length)
                * factorial(chooseCnt);
    }

    /**
     * 阶乘n!
     *
     * @return
     */
    public static int factorial(int n) {
        if (n > 12) {
            throw new IllegalArgumentException("超出Int阶乘限制，n不能大于12！~ " + n);
        }
        if (n > 0) {
            int number = 1;
            while (n > 0) {
                number *= n--;
            }
            return number;
        }
        return 0;
    }

    /**
     * 拆分复式注数
     *
     * @param
     * @param
     * @return
     */
    public static long getZ3CompoundSplitCount(Object[] bets) {
        return getCombination(bets.length, 2) * 2;
    }

    /**
     * 拆分复式注数
     *
     * @param
     * @param
     * @return
     */
    public static long getZ6CompoundSplitCount(Object[] bets) {
        return getCombination(bets.length, 3);
    }

    /**
     * String数组转换Int数组
     *
     * @param strs
     * @return
     */
    public static int[] convertIntArray(String[] strs) {
        int strsLen = strs.length;
        int[] ints = new int[strsLen];
        for (int i = 0; i < strsLen; i++) {
            ints[i] = Integer.parseInt(strs[i]);
        }
        return ints;
    }

    /**
     * byte数组转换Int数组
     *
     * @param strs
     * @return
     */
    public static int[] convertCharArray(char[] strs) {
        int strsLen = strs.length;
        int[] ints = new int[strsLen];
        for (int i = 0; i < strsLen; i++) {
            ints[i] = strs[i];
        }
        return ints;
    }

    /**
     * 拆分和值注数（豹子）
     *
     * @param ball  选择的和值号码
     * @param balls 用那些数字进行和值操作
     * @return
     */
    public static int getLeopardSumSplitCount(int ball, int[] balls) {
        // 对分割后的列表进行过滤，增加适当的性能开销来提高代码的维护性（通过外层缓存来提高性能）
        List<int[]> sumSplitList = getSumSplitList(ball, 3, balls);
        int number = 0;
        for (int[] splitBalls : sumSplitList) {
            // 豹子必须3个号码一样
            if (splitBalls[0] == splitBalls[1]
                    && splitBalls[1] == splitBalls[2]) {
                number++;
            }
        }
        return number;
    }

    /**
     * 拆分和值注数（组三）
     *
     * @param ball  选择的和值号码
     * @param balls 用那些数字进行和值操作
     * @return
     */
    public static int getZ3SumSplitCount(int ball, int[] balls) {
        // 对分割后的列表进行过滤，增加适当的性能开销来提高代码的维护性（通过外层缓存来提高性能）
        List<int[]> sumSplitList = getSumSplitList(ball, 3, balls);
        int number = 0;
        for (int[] splitBalls : sumSplitList) {
            // 组三必须两边号码一样
            if (splitBalls[0] == splitBalls[1]
                    || splitBalls[1] == splitBalls[2]) {
                number++;
            }
        }
        return number / 2;
    }

    /**
     * 拆分和值注数（组三）
     *
     * @param ball  选择的和值号码
     * @param balls 用那些数字进行和值操作
     * @return
     */
    public static int getZ6SumSplitCount(int ball, int[] balls) {
        // 对分割后的列表进行过滤，增加适当的性能开销来提高代码的维护性（通过外层缓存来提高性能）
        List<int[]> sumSplitList = getSumSplitList(ball, 3, balls);
        int maxBall = maxBall(balls);
        int number = 0;
        for (int[] splitBalls : sumSplitList) {
            int[] cnts = new int[maxBall + 1];
            boolean flag = true;
            for (int b : splitBalls) {
                if (++cnts[b] > 1) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                number++;
            }
        }
        return number / 6;
    }

    /**
     * 获取和值分割后的号码组
     *
     * @param ball
     * @param cnt
     * @param balls
     * @return
     */
    public static List<int[]> getSumSplitList(int ball, int cnt, int[] balls) {
        // 对分割后的列表过滤，增加适当的性能开销来提高代码的维护性（外层缓存来提高性能）
        List<int[]> list = new LinkedList<int[]>();
        _getSumSplitList(1, cnt, ball, balls, 0, new int[cnt], list);
        return list;
    }

    private static void _getSumSplitList(int layer, int layerCnt, int ball,
                                         int[] balls, int sum, int[] splitBalls, List<int[]> list) {
        // 循环本层号码
        for (int bet : balls) {
            splitBalls[layer - 1] = bet;
            int tsum = bet + sum;
            // 达到合成数
            if (layer >= layerCnt) {
                // 等于和值数+1
                if (tsum == ball) {
                    int[] sBalls = new int[layerCnt];
                    System.arraycopy(splitBalls, 0, sBalls, 0, layerCnt);
                    list.add(sBalls);
                }
            } else {
                // 如果还没达到和值则进入下级循环
                if (tsum <= ball) {
                    // 进入下一层
                    _getSumSplitList(layer + 1, layerCnt, ball, balls, tsum,
                            splitBalls, list);
                }
            }
        }
    }

    public static void permutation(String[] fsarr, int m, List<String[]> list,
                                   String[] arr) {
        if (m < 0)
            return;
        char[] nums = fsarr[m].toCharArray();
        for (char num : nums) {
            arr[m] = num + "";
            if (m <= 0) {
                int length = arr.length;
                String[] narr = new String[length];
                System.arraycopy(arr, 0, narr, 0, length);
                list.add(narr);
            } else {
                permutation(fsarr, m - 1, list, arr);
            }
        }
    }

    /**
     * 拆分和值注数
     *
     * @param ball  选择的和值号码
     * @param cnt   合成几位数
     * @param balls 用那些数字进行和值操作
     * @return
     */
    public static int getSumSplitCount(int ball, int cnt, int[] balls) {
        return _getSumSplitCount(1, cnt, ball, balls, 0);
    }

    private static int _getSumSplitCount(int layer, int layerCnt, int ball,
                                         int[] balls, int sum) {
        // 循环本层号码
        int number = 0;
        for (int bet : balls) {
            int tsum = bet + sum;
            // 达到合成数
            if (layer >= layerCnt) {
                // 等于和值数+1
                if (tsum == ball) {
                    number++;
                }
            } else {
                // 如果还没达到和值则进入下级循环
                if (tsum <= ball) {
                    // 进入下一层
                    number += _getSumSplitCount(layer + 1, layerCnt, ball,
                            balls, tsum);
                }
            }
        }
        return number;
    }

    /**
     * trim str数组
     *
     * @param strArr
     * @return
     */
    public static String[] trimArray(String[] strArr) {
        String[] arr = new String[strArr.length];
        for (int i = 0; i < strArr.length; i++) {
            arr[i] = strArr[i].trim();
        }
        return arr;
    }

    /**
     * 验证号码是否有效，null为无效
     *
     * @param numbers
     * @param start
     * @param end
     * @return
     */
    public static boolean validAwardNumbers(String[] numbers, int start, int end) {
        if (numbers == null || start < 0 || end > numbers.length) {
            return false;
        }
        for (int i = start; i < end; i++) {
            if (numbers[i] == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * 计算中奖注数
     *
     * @return
     */
    private long award(int front, int back, int f_dm_cnt, int f_tm_cnt,
                       int f_dm_award, int f_tm_award, int b_dm_cnt, int b_tm_cnt,
                       int b_dm_award, int b_tm_award) {
        long fcnt = getAwardDTCount(front, f_dm_cnt, f_tm_cnt, f_dm_award,
                f_tm_award, 6);
        long bcnt = getAwardDTCount(back, b_dm_cnt, b_tm_cnt, b_dm_award,
                b_tm_award, 1);
        return fcnt * bcnt;
    }


}
