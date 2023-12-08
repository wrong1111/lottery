package com.qihang.common.util;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CombinationUtil {

    /**
     * 获取组合数 n(n-1)(n-2)……(n-m+1)/m!
     *
     * @param m
     * @param n
     * @return
     */
    public static int getCombination(int n, int m) {
        if (m > n) {
            throw new IllegalArgumentException("m不能大于n！");
        }
        if (m > n / 2) {
            m = n - m;
        }
        int nn = 1, mm = 1;    //保存最后值
        for (int i = n, j = 1; j <= m /*&& i >= (n - m + 1)*/; nn *= i--, mm *= j++)
            ;
        return nn / mm;
    }


    public static List<List<String>> getCombinations(String[] nums, int n) {
        List<List<String>> result = new ArrayList<>();
        List<String> combination = new ArrayList<>();
        generateCombinations(nums, n, 0, combination, result);
        return result;
    }

    private static void generateCombinations(String[] nums, int n, int start, List<String> combination, List<List<String>> result) {
        if (n == 0) {
            result.add(new ArrayList<>(combination));
            return;
        }

        for (int i = start; i < nums.length; i++) {
            combination.add(nums[i]);
            generateCombinations(nums, n - 1, i + 1, combination, result);
            combination.remove(combination.size() - 1);
        }
    }

    public static List<List<String>> permute(String[] nums, int m) {
        List<List<String>> result = new ArrayList<>();
        backtrack(result, new ArrayList<>(), nums, m);
        return result;
    }

    private static void backtrack(List<List<String>> result, List<String> tempList, String[] nums, int m) {
        if (tempList.size() == m) {
            result.add(new ArrayList<>(tempList));
        } else {
            for (int i = 0; i < nums.length; i++) {
                if (tempList.contains(nums[i])) {
                    continue; // 跳过重复元素
                }
                tempList.add(nums[i]);
                backtrack(result, tempList, nums, m);
                tempList.remove(tempList.size() - 1);
            }
        }
    }

    public static void perm() {
        String[] nums = {"01", "02", "03"};
        int m = 3;
        List<List<String>> permutations = permute(nums, m);
        for (List<String> permutation : permutations) {
            System.out.println(permutation);
        }
    }

    public static void combine() {
        String[] nums = {"01", "02", "03", "04", "05"};
        int n = 3;
        List<List<String>> combinations = getCombinations(nums, n);
        System.out.println(JSON.toJSONString(combinations));
    }

    /**
     * 二维组合, 每行选一个进行组合
     *
     * @param contents 内容
     * @param len      长度 表示取几行
     * @param <T>      类型
     * @return
     */
    public static <T> ArrayList<ArrayList<T>> permTowDimensionIsOrder(List<ArrayList<T>> contents, int len) {
        ArrayList<ArrayList<T>> results = new ArrayList<ArrayList<T>>();
        allMatchPlayerComb(0, contents, new ArrayList<T>(), results, len);
        return results;
    }

    /**
     * @param begRow    0
     * @param contents  二维数组
     * @param result    当前取的内容
     * @param allResult
     * @param len       长度
     */
    private static <T> void allMatchPlayerComb(int begRow, List<ArrayList<T>> contents, ArrayList<T> result, List<ArrayList<T>> allResult, int len) {
        if (result.size() == len) {
            allResult.add(result);
            return;
        }

        for (int r = begRow; r < contents.size(); r++) {   //2 row  迭代行
            ArrayList<T> curRowContent = contents.get(r);

            for (int c = 0; c < curRowContent.size(); c++) {    //迭代列    从当前行中取
                ArrayList<T> temp = new ArrayList<T>();
                temp.addAll(result);
                temp.add(curRowContent.get(c));
                allMatchPlayerComb(r + 1, contents, temp, allResult, len);
            }
        }
    }

    public static void main(String[] args) {
        //combine();
//        List<String> list = new ArrayList<>(Arrays.asList(new String[]{"2", "1", "3"}));
//        System.out.println(getCombinations(list.toArray(new String[0]), 2));

//        List<String> alist = new ArrayList<>(Arrays.asList(new String[]{"1", "2", "3"}));
//        List<String> blist = new ArrayList<>(Arrays.asList(new String[]{"3", "4", "5"}));
//        System.out.println(CollectionUtil.reverseNew(blist));
//        System.out.println(blist);
//        System.out.println(" union 并集 " + CollectionUtil.union(blist, alist));
//        System.out.println(" union 交集 " + CollectionUtil.intersection(blist, alist));
        ArrayList<String> a = new ArrayList<>(Arrays.asList(new String[]{"a", "b"}));
        ArrayList<String> b = new ArrayList<>(Arrays.asList(new String[]{"c", "d"}));
        ArrayList<String> c = new ArrayList<>(Arrays.asList(new String[]{"e", "f"}));
        ArrayList<ArrayList<String>> lists = new ArrayList<>();
        lists.add(a);
        lists.add(b);
        lists.add(c);
        System.out.println(CombinationUtil.permTowDimensionIsOrder(lists, 3));
    }
}
