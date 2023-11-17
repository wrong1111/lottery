package com.qihang.common.util;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
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

    public static void main(String[] args) {
        //combine();
        System.out.println(getCombination(6, 0));
    }
}
