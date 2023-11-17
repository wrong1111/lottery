package com.qihang.common.util.reward;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.qihang.common.util.CombinationUtil;
import com.qihang.common.util.PlayUtil;
import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.BonusVo;
import com.qihang.controller.grandlotto.dto.GrandLottoObjDTO;
import com.qihang.controller.winburden.dto.WinBurdenMatchDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

public class GrandLottoUtil {


    /*
   选十              选九             选八            选七              选六        选五
中10	5000000     中9	300000      中8	50000       中7	10000       中6	3000    中5	1000
中9		8000        中8	2000        中7	800         中6	288         中5	30      中4	21
中8		800         中7	200         中6	88          中5	28          中4	10      中3	3
中7		80          中6	20          中5	28          中4	4           中3	3
中6		5           中5	5           中4	4           中0	2
中5		3           中4  3          中0	2
中0		2           中0	2

选四                 选三
中4	100             中3	53
中3   5             中2	3
中2	3

选二中2		19
选一中1		4.6
     */
    public static List<BonusVo> awardKL8(List<GrandLottoObjDTO> betList, String reaward, String awardMoney, String mode) {
        switch (mode) {
            case "10":
                return mode10(betList, reaward, awardMoney);
            case "9":
                return mode9(betList, reaward, awardMoney);
            case "8":
                return mode8(betList, reaward, awardMoney);
            case "7":
                return mode7(betList, reaward, awardMoney);
            case "6":
                return mode6(betList, reaward, awardMoney);
            case "5":
                return mode5(betList, reaward, awardMoney);
            case "4":
                return mode4(betList, reaward, awardMoney);
            case "3":
                return mode3(betList, reaward, awardMoney);
            case "2":
                return mode2(betList, reaward, awardMoney);
            case "1":
                return mode1(betList, reaward, awardMoney);
            default:
                return new ArrayList<>();
        }

    }

    public static Map<String, Double> awardLevelMap = new HashMap<>();

    static {
        awardLevelMap.put("23-10中10", 5000000d);
        awardLevelMap.put("23-10中9", 8000d);
        awardLevelMap.put("23-10中8", 800d);
        awardLevelMap.put("23-10中7", 80d);
        awardLevelMap.put("23-10中6", 5d);
        awardLevelMap.put("23-10中5", 3d);
        awardLevelMap.put("23-10中0", 2d);
        awardLevelMap.put("23-9中9", 300000d);
        awardLevelMap.put("23-9中8", 2000d);
        awardLevelMap.put("23-9中7", 200d);
        awardLevelMap.put("23-9中6", 20d);
        awardLevelMap.put("23-9中5", 5d);
        awardLevelMap.put("23-9中4", 3d);
        awardLevelMap.put("23-9中0", 2d);
        awardLevelMap.put("23-8中8", 50000d);
        awardLevelMap.put("23-8中7", 800d);
        awardLevelMap.put("23-8中6", 88d);
        awardLevelMap.put("23-8中5", 10d);
        awardLevelMap.put("23-8中4", 3d);
        awardLevelMap.put("23-8中0", 2d);
        awardLevelMap.put("23-7中7", 10000d);
        awardLevelMap.put("23-7中6", 288d);
        awardLevelMap.put("23-7中5", 28d);
        awardLevelMap.put("23-7中4", 4d);
        awardLevelMap.put("23-7中0", 2d);
        awardLevelMap.put("23-6中6", 3000d);
        awardLevelMap.put("23-6中5", 30d);
        awardLevelMap.put("23-6中4", 10d);
        awardLevelMap.put("23-6中3", 3d);
        awardLevelMap.put("23-5中5", 1000d);
        awardLevelMap.put("23-5中4", 21d);
        awardLevelMap.put("23-5中3", 3d);
        awardLevelMap.put("23-4中4", 100d);
        awardLevelMap.put("23-4中3", 5d);
        awardLevelMap.put("23-4中2", 3d);
        awardLevelMap.put("23-3中3", 53d);
        awardLevelMap.put("23-3中2", 3d);
        awardLevelMap.put("23-2中2", 19d);
        awardLevelMap.put("23-1中1", 4.6d);
    }

    private static List<BonusVo> mode10(List<GrandLottoObjDTO> betList, String reward, String awardMoney) {
        BonusVo bonusVo = new BonusVo();
        bonusVo.setAward(false);
        bonusVo.setLevel("");
        bonusVo.setMoney(0d);
        List<BonusVo> bonusVoList = new ArrayList<>();
        String[] awardNumber = StringUtils.splitByWholeSeparatorPreserveAllTokens(reward, ",");


        List<String> danList = loadDan(betList);
        List<String> numberList = loadNotDan(betList);
        int maxNotDan = 10 - danList.size();
        int maxDan = danList.size();

        String awardContent = "";
        if (danList.size() > 0) {

            int awardDan = 0;
            int awardNotDan = 0;
            for (int i = 0; i < awardNumber.length; i++) {
                if (danList.contains(awardNumber[i])) {
                    awardDan++;
                    awardContent += awardNumber[i] + ",";
                }
                if (numberList.contains(awardNumber[i])) {
                    awardNotDan++;
                    awardContent += awardNumber[i] + ",";
                }
            }
            //一等奖
            long notes = PlayUtil.getAwardDTCount(10, maxDan, maxNotDan, awardDan, awardNotDan, 10);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("10中10");
                bonusVo.setMoney(5000000d);
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(9, maxDan, maxNotDan, awardDan, awardNotDan, 10);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("10中9");
                bonusVo.setMoney(8000d);
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(8, maxDan, maxNotDan, awardDan, awardNotDan, 10);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("10中8");
                bonusVo.setMoney(800d);
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(7, maxDan, maxNotDan, awardDan, awardNotDan, 10);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("10中7");
                bonusVo.setMoney(80d);
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(6, maxDan, maxNotDan, awardDan, awardNotDan, 10);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("10中6");
                bonusVo.setMoney(5d);
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(5, maxDan, maxNotDan, awardDan, awardNotDan, 10);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("10中5");
                bonusVo.setMoney(3d);
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(0, maxDan, maxNotDan, awardDan, awardNotDan, 10);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("10中0");
                bonusVo.setMoney(2d);
                List<String> awardList = new ArrayList<>(Arrays.asList(awardNumber));
                bonusVo.setAwardContent(StringUtils.join(numberList.stream().filter(p -> !awardList.contains(p)).collect(Collectors.toList()), ","));
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
        } else {
            int awardNotDan = 0;
            for (int i = 0; i < awardNumber.length; i++) {
                if (numberList.contains(awardNumber[i])) {
                    awardNotDan++;
                    awardContent += awardNumber[i] + ",";
                }
            }
            long notes = PlayUtil.getAwardDTCount(10, 0, maxNotDan, 0, awardNotDan, 10);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("10中10");
                bonusVo.setMoney(5000000d);
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(9, 0, maxNotDan, 0, awardNotDan, 10);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("10中9");
                bonusVo.setMoney(8000d);
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(8, 0, maxNotDan, 0, awardNotDan, 10);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("10中8");
                bonusVo.setMoney(800d);
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(7, 0, maxNotDan, 0, awardNotDan, 10);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("10中7");
                bonusVo.setMoney(80d);
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(6, 0, maxNotDan, 0, awardNotDan, 10);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("10中6");
                bonusVo.setMoney(5d);
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(5, 0, maxNotDan, 0, awardNotDan, 10);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("10中5");
                bonusVo.setMoney(3d);
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(0, 0, maxNotDan, 0, awardNotDan, 10);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("10中0");
                bonusVo.setMoney(2d);
                List<String> awardList = new ArrayList<>(Arrays.asList(awardNumber));
                bonusVo.setAwardContent(StringUtils.join(numberList.stream().filter(p -> !awardList.contains(p)).collect(Collectors.toList()), ","));
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
        }
        return bonusVoList;
    }

    private static List<BonusVo> mode9(List<GrandLottoObjDTO> betList, String reward, String awardMoney) {
        BonusVo bonusVo = new BonusVo();
        List<BonusVo> bonusVoList = new ArrayList<>();
        String[] awardNumber = StringUtils.splitByWholeSeparatorPreserveAllTokens(reward, ",");


        List<String> danList = loadDan(betList);
        List<String> numberList = loadNotDan(betList);
        int maxNotDan = 9 - danList.size();
        int base = 9;
        int maxDan = danList.size();

        String awardContent = "";
        if (danList.size() > 0) {

            int awardDan = 0;
            int awardNotDan = 0;
            for (int i = 0; i < awardNumber.length; i++) {
                if (danList.contains(awardNumber[i])) {
                    awardDan++;
                    awardContent += awardNumber[i] + ",";
                }
                if (numberList.contains(awardNumber[i])) {
                    awardNotDan++;
                    awardContent += awardNumber[i] + ",";
                }
            }
            //一等奖

            long notes = PlayUtil.getAwardDTCount(base, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("9中9");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(8, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("9中8");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(7, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("9中7");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(6, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("9中6");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(5, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("9中5");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(0, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("9中0");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                List<String> awardList = new ArrayList<>(Arrays.asList(awardNumber));
                bonusVo.setAwardContent(StringUtils.join(numberList.stream().filter(p -> !awardList.contains(p)).collect(Collectors.toList()), ","));
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
        } else {
            int awardNotDan = 0;
            for (int i = 0; i < awardNumber.length; i++) {
                if (numberList.contains(awardNumber[i])) {
                    awardNotDan++;
                    awardContent += awardNumber[i] + ",";
                }
            }
            long notes = PlayUtil.getAwardDTCount(9, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("9中9");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(8, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("9中8");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(7, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("9中7");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(6, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("9中6");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(5, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("9中5");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(0, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("9中0");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                List<String> awardList = new ArrayList<>(Arrays.asList(awardNumber));
                bonusVo.setAwardContent(StringUtils.join(numberList.stream().filter(p -> !awardList.contains(p)).collect(Collectors.toList()), ","));
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
        }
        return bonusVoList;
    }

    private static List<BonusVo> mode8(List<GrandLottoObjDTO> betList, String reward, String awardMoney) {
        BonusVo bonusVo = new BonusVo();
        List<BonusVo> bonusVoList = new ArrayList<>();
        String[] awardNumber = StringUtils.splitByWholeSeparatorPreserveAllTokens(reward, ",");


        List<String> danList = loadDan(betList);
        List<String> numberList = loadNotDan(betList);
        int maxNotDan = 8 - danList.size();
        int base = 8;
        int maxDan = danList.size();

        String awardContent = "";
        if (danList.size() > 0) {

            int awardDan = 0;
            int awardNotDan = 0;
            for (int i = 0; i < awardNumber.length; i++) {
                if (danList.contains(awardNumber[i])) {
                    awardDan++;
                    awardContent += awardNumber[i] + ",";
                }
                if (numberList.contains(awardNumber[i])) {
                    awardNotDan++;
                    awardContent += awardNumber[i] + ",";
                }
            }
            //一等奖

            long notes = PlayUtil.getAwardDTCount(8, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("8中8");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(7, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("8中7");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(6, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("8中6");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(5, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("8中5");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(4, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("8中4");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(0, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("8中0");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                List<String> awardList = new ArrayList<>(Arrays.asList(awardNumber));
                bonusVo.setAwardContent(StringUtils.join(numberList.stream().filter(p -> !awardList.contains(p)).collect(Collectors.toList()), ","));
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
        } else {
            int awardNotDan = 0;
            for (int i = 0; i < awardNumber.length; i++) {
                if (numberList.contains(awardNumber[i])) {
                    awardNotDan++;
                    awardContent += awardNumber[i] + ",";
                }
            }
            long notes = PlayUtil.getAwardDTCount(8, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("8中8");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(7, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("8中7");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(6, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("8中6");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(5, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("8中5");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(4, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("8中4");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(0, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("8中0");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                List<String> awardList = new ArrayList<>(Arrays.asList(awardNumber));
                bonusVo.setAwardContent(StringUtils.join(numberList.stream().filter(p -> !awardList.contains(p)).collect(Collectors.toList()), ","));
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
        }
        return bonusVoList;
    }

    private static List<BonusVo> mode7(List<GrandLottoObjDTO> betList, String reward, String awardMoney) {
        BonusVo bonusVo = new BonusVo();
        List<BonusVo> bonusVoList = new ArrayList<>();
        String[] awardNumber = StringUtils.splitByWholeSeparatorPreserveAllTokens(reward, ",");


        List<String> danList = loadDan(betList);
        List<String> numberList = loadNotDan(betList);
        int maxNotDan = 7 - danList.size();
        int base = 7;
        int maxDan = danList.size();

        String awardContent = "";
        if (danList.size() > 0) {

            int awardDan = 0;
            int awardNotDan = 0;
            for (int i = 0; i < awardNumber.length; i++) {
                if (danList.contains(awardNumber[i])) {
                    awardDan++;
                    awardContent += awardNumber[i] + ",";
                }
                if (numberList.contains(awardNumber[i])) {
                    awardNotDan++;
                    awardContent += awardNumber[i] + ",";
                }
            }
            //一等奖

            long notes = PlayUtil.getAwardDTCount(7, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("7中7");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(6, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("7中6");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(5, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("7中5");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(4, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("7中4");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(0, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("7中0");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                List<String> awardList = new ArrayList<>(Arrays.asList(awardNumber));
                bonusVo.setAwardContent(StringUtils.join(numberList.stream().filter(p -> !awardList.contains(p)).collect(Collectors.toList()), ","));
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
        } else {
            int awardNotDan = 0;
            for (int i = 0; i < awardNumber.length; i++) {
                if (numberList.contains(awardNumber[i])) {
                    awardNotDan++;
                    awardContent += awardNumber[i] + ",";
                }
            }
            long notes = PlayUtil.getAwardDTCount(7, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("7中7");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(6, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("7中6");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(5, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("7中5");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(4, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("7中4");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(0, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("7中0");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                List<String> awardList = new ArrayList<>(Arrays.asList(awardNumber));
                bonusVo.setAwardContent(StringUtils.join(numberList.stream().filter(p -> !awardList.contains(p)).collect(Collectors.toList()), ","));
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
        }
        return bonusVoList;
    }

    private static List<BonusVo> mode6(List<GrandLottoObjDTO> betList, String reward, String awardMoney) {
        BonusVo bonusVo = new BonusVo();
        List<BonusVo> bonusVoList = new ArrayList<>();
        String[] awardNumber = StringUtils.splitByWholeSeparatorPreserveAllTokens(reward, ",");


        List<String> danList = loadDan(betList);
        List<String> numberList = loadNotDan(betList);
        int maxNotDan = 6 - danList.size();
        int base = 6;
        int maxDan = danList.size();

        String awardContent = "";
        if (danList.size() > 0) {

            int awardDan = 0;
            int awardNotDan = 0;
            for (int i = 0; i < awardNumber.length; i++) {
                if (danList.contains(awardNumber[i])) {
                    awardDan++;
                    awardContent += awardNumber[i] + ",";
                }
                if (numberList.contains(awardNumber[i])) {
                    awardNotDan++;
                    awardContent += awardNumber[i] + ",";
                }
            }
            //一等奖

            long notes = PlayUtil.getAwardDTCount(6, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("6中6");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(5, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("6中5");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(4, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("6中4");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(3, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("6中3");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
        } else {
            int awardNotDan = 0;
            for (int i = 0; i < awardNumber.length; i++) {
                if (numberList.contains(awardNumber[i])) {
                    awardNotDan++;
                    awardContent += awardNumber[i] + ",";
                }
            }
            long notes = PlayUtil.getAwardDTCount(6, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("6中6");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(5, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("6中5");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(4, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("6中4");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(3, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("6中3");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
        }
        return bonusVoList;
    }

    private static List<BonusVo> mode5(List<GrandLottoObjDTO> betList, String reward, String awardMoney) {
        BonusVo bonusVo = new BonusVo();
        List<BonusVo> bonusVoList = new ArrayList<>();
        String[] awardNumber = StringUtils.splitByWholeSeparatorPreserveAllTokens(reward, ",");


        List<String> danList = loadDan(betList);
        List<String> numberList = loadNotDan(betList);
        int maxNotDan = 5 - danList.size();
        int base = 5;
        int maxDan = danList.size();

        String awardContent = "";
        if (danList.size() > 0) {

            int awardDan = 0;
            int awardNotDan = 0;
            for (int i = 0; i < awardNumber.length; i++) {
                if (danList.contains(awardNumber[i])) {
                    awardDan++;
                    awardContent += awardNumber[i] + ",";
                }
                if (numberList.contains(awardNumber[i])) {
                    awardNotDan++;
                    awardContent += awardNumber[i] + ",";
                }
            }
            //一等奖

            long notes = PlayUtil.getAwardDTCount(5, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("5中5");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(4, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("5中4");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(3, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("5中3");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
        } else {
            int awardNotDan = 0;
            for (int i = 0; i < awardNumber.length; i++) {
                if (numberList.contains(awardNumber[i])) {
                    awardNotDan++;
                    awardContent += awardNumber[i] + ",";
                }
            }
            long notes = PlayUtil.getAwardDTCount(5, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("5中5");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(4, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("5中4");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(3, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("5中3");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
        }
        return bonusVoList;
    }

    private static List<BonusVo> mode4(List<GrandLottoObjDTO> betList, String reward, String awardMoney) {
        BonusVo bonusVo = new BonusVo();
        List<BonusVo> bonusVoList = new ArrayList<>();
        String[] awardNumber = StringUtils.splitByWholeSeparatorPreserveAllTokens(reward, ",");


        List<String> danList = loadDan(betList);
        List<String> numberList = loadNotDan(betList);
        int maxNotDan = 4 - danList.size();
        int base = 4;
        int maxDan = danList.size();

        String awardContent = "";
        if (danList.size() > 0) {

            int awardDan = 0;
            int awardNotDan = 0;
            for (int i = 0; i < awardNumber.length; i++) {
                if (danList.contains(awardNumber[i])) {
                    awardDan++;
                    awardContent += awardNumber[i] + ",";
                }
                if (numberList.contains(awardNumber[i])) {
                    awardNotDan++;
                    awardContent += awardNumber[i] + ",";
                }
            }
            //一等奖

            long notes = PlayUtil.getAwardDTCount(4, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("4中4");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(3, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("4中3");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(2, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("4中2");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
        } else {
            int awardNotDan = 0;
            for (int i = 0; i < awardNumber.length; i++) {
                if (numberList.contains(awardNumber[i])) {
                    awardNotDan++;
                    awardContent += awardNumber[i] + ",";
                }
            }
            long notes = PlayUtil.getAwardDTCount(4, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("4中4");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(3, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("4中3");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(2, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("4中2");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
        }
        return bonusVoList;
    }

    private static List<BonusVo> mode3(List<GrandLottoObjDTO> betList, String reward, String awardMoney) {
        BonusVo bonusVo = new BonusVo();
        List<BonusVo> bonusVoList = new ArrayList<>();
        String[] awardNumber = StringUtils.splitByWholeSeparatorPreserveAllTokens(reward, ",");


        List<String> danList = loadDan(betList);
        List<String> numberList = loadNotDan(betList);
        int maxNotDan = 3 - danList.size();
        int base = 3;
        int maxDan = danList.size();

        String awardContent = "";
        if (danList.size() > 0) {

            int awardDan = 0;
            int awardNotDan = 0;
            for (int i = 0; i < awardNumber.length; i++) {
                if (danList.contains(awardNumber[i])) {
                    awardDan++;
                    awardContent += awardNumber[i] + ",";
                }
                if (numberList.contains(awardNumber[i])) {
                    awardNotDan++;
                    awardContent += awardNumber[i] + ",";
                }
            }
            //一等奖

            long notes = PlayUtil.getAwardDTCount(3, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("3中3");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(2, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("3中2");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
        } else {
            int awardNotDan = 0;
            for (int i = 0; i < awardNumber.length; i++) {
                if (numberList.contains(awardNumber[i])) {
                    awardNotDan++;
                    awardContent += awardNumber[i] + ",";
                }
            }
            long notes = PlayUtil.getAwardDTCount(3, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("3中3");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
            notes = PlayUtil.getAwardDTCount(2, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("3中2");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
        }
        return bonusVoList;
    }

    private static List<BonusVo> mode2(List<GrandLottoObjDTO> betList, String reward, String awardMoney) {
        BonusVo bonusVo = new BonusVo();
        List<BonusVo> bonusVoList = new ArrayList<>();
        String[] awardNumber = StringUtils.splitByWholeSeparatorPreserveAllTokens(reward, ",");


        List<String> danList = loadDan(betList);
        List<String> numberList = loadNotDan(betList);
        int maxNotDan = 2 - danList.size();
        int base = 2;
        int maxDan = danList.size();

        String awardContent = "";
        if (danList.size() > 0) {

            int awardDan = 0;
            int awardNotDan = 0;
            for (int i = 0; i < awardNumber.length; i++) {
                if (danList.contains(awardNumber[i])) {
                    awardDan++;
                    awardContent += awardNumber[i] + ",";
                }
                if (numberList.contains(awardNumber[i])) {
                    awardNotDan++;
                    awardContent += awardNumber[i] + ",";
                }
            }
            //一等奖

            long notes = PlayUtil.getAwardDTCount(2, maxDan, maxNotDan, awardDan, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("2中2");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
        } else {
            int awardNotDan = 0;
            for (int i = 0; i < awardNumber.length; i++) {
                if (numberList.contains(awardNumber[i])) {
                    awardNotDan++;
                    awardContent += awardNumber[i] + ",";
                }
            }
            long notes = PlayUtil.getAwardDTCount(2, 0, maxNotDan, 0, awardNotDan, base);
            if (notes > 0) {
                bonusVo.setAward(true);
                bonusVo.setLevel("2中2");
                bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes((int) notes);
                bonusVoList.add(bonusVo);
            }
        }
        return bonusVoList;
    }

    private static List<BonusVo> mode1(List<GrandLottoObjDTO> betList, String reward, String awardMoney) {
        BonusVo bonusVo = new BonusVo();
        List<BonusVo> bonusVoList = new ArrayList<>();
        String[] awardNumber = StringUtils.splitByWholeSeparatorPreserveAllTokens(reward, ",");

        List<String> numberList = loadNumber(betList);
        String awardContent = "";
        int awardNotDan = 0;
        for (int i = 0; i < awardNumber.length; i++) {
            if (numberList.contains(awardNumber[i])) {
                awardNotDan++;
                awardContent += awardNumber[i] + ",";
            }
        }
        long notes = awardNotDan;
        if (notes > 0) {
            bonusVo.setAward(true);
            bonusVo.setLevel("1中1");
            bonusVo.setMoney(awardLevelMap.get("23-" + bonusVo.getLevel()));
            bonusVo.setAwardContent(awardContent);
            bonusVo.setAwardNotes((int) notes);
            bonusVoList.add(bonusVo);
        }
        return bonusVoList;
    }

    /**
     * 一等奖：投注号码与当期开奖号码全部相同（顺序不限，下同），即中奖；
     * <p>
     * 二等奖：投注号码与当期开奖号码中的6个红色球号码相同，即中奖；
     * <p>
     * 三等奖：投注号码与当期开奖号码中的任意5个红色球号码和1个蓝色球号码相同，即中奖；
     * <p>
     * 四等奖：投注号码与当期开奖号码中的任意5个红色球号码相同，或与任意4个红色球号码和1个蓝色球号码相同，即中奖；
     * <p>
     * 五等奖：投注号码与当期开奖号码中的任意4个红色球号码相同，或与任意3个红色球号码和1个蓝色球号码相同，即中奖；
     * <p>
     * 六等奖：投注号码与当期开奖号码中的1个蓝色球号码相同，即中奖。
     */
    public static BonusVo awardSSQ(List<GrandLottoObjDTO> tenList, List<GrandLottoObjDTO> blueList, String reward, String awardMoney) {
        BonusVo bonusVo = new BonusVo();
        bonusVo.setAward(false);
        bonusVo.setLevel("");
        bonusVo.setMoney(0d);
        String[] awardNumber = StringUtils.splitByWholeSeparatorPreserveAllTokens(reward, ",");
        String[] awardsMoney = StringUtils.splitByWholeSeparatorPreserveAllTokens(awardMoney, ",");
        String blueNumber = awardNumber[6];


        List<String> danList = loadDan(tenList);
        List<String> numberList = loadNotDan(tenList);

        List<String> blues = loadNumber(blueList);
        boolean awardBlue = blues.contains(blueNumber);

        String awardContent = "";
        if (danList.size() > 0) {
            int maxNotDan = 6 - danList.size();
            int maxDan = danList.size();

            int awardDan = 0;
            int awardNotDan = 0;
            for (int i = 0; i < 6; i++) {
                if (danList.contains(awardNumber[i])) {
                    awardDan++;
                    awardContent += awardNumber[i] + ",";
                }
                if (numberList.contains(awardNumber[i])) {
                    awardNotDan++;
                    awardContent += awardNumber[i] + ",";
                }
            }
            int cawardNotDan = awardNotDan >= maxNotDan ? maxNotDan : awardNotDan;
            int cawardDan = awardDan >= maxDan ? maxDan : awardDan;
            if (cawardDan + cawardNotDan >= 6 && awardBlue) {
                //一等奖
                bonusVo.setAward(true);
                bonusVo.setLevel("1");
                bonusVo.setMoney(Double.valueOf(awardsMoney[0]));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes(1);
                return bonusVo;
            } else if (cawardDan + cawardNotDan >= 6) {
                //二等 奖
                bonusVo.setAward(true);
                bonusVo.setLevel("2");
                bonusVo.setMoney(Double.valueOf(awardsMoney[1]));
                bonusVo.setAwardContent(awardContent);
                int notes = awardNotDan > maxNotDan ? CombinationUtil.getCombination(awardNotDan, maxNotDan) : 1;
                bonusVo.setAwardNotes(notes);
                return bonusVo;
            } else if (cawardDan + cawardNotDan >= 5 && awardBlue) {
                //三等 奖 3000
                bonusVo.setAward(true);
                bonusVo.setLevel("3");
                bonusVo.setMoney(Double.valueOf(awardsMoney[2]));
                bonusVo.setAwardContent(awardContent);
                int notes = awardNotDan > maxNotDan ? CombinationUtil.getCombination(awardNotDan, maxNotDan) : 1;
                bonusVo.setAwardNotes(notes);
                return bonusVo;
            } else if (cawardDan + cawardNotDan >= 5 || (cawardDan + cawardNotDan >= 4 && awardBlue)) {
                //四等 奖 200
                bonusVo.setAward(true);
                bonusVo.setLevel("4");
                bonusVo.setMoney(Double.valueOf(awardsMoney[3]));
                bonusVo.setAwardContent(awardContent);
                int notes = awardNotDan > maxNotDan ? CombinationUtil.getCombination(awardNotDan, maxNotDan) : 1;
                bonusVo.setAwardNotes(notes);
                return bonusVo;
            } else if (cawardDan + cawardNotDan >= 4 || (cawardDan + cawardNotDan >= 3 && awardBlue)) {
                //五等 奖 10
                bonusVo.setAward(true);
                bonusVo.setLevel("5");
                bonusVo.setMoney(Double.valueOf(awardsMoney[4]));
                bonusVo.setAwardContent(awardContent);
                int notes = awardNotDan > maxNotDan ? CombinationUtil.getCombination(awardNotDan, maxNotDan) : 1;
                bonusVo.setAwardNotes(notes);
                return bonusVo;
            } else if (awardBlue) {
                //六等 奖 5
                bonusVo.setAward(true);
                bonusVo.setLevel("6");
                bonusVo.setMoney(Double.valueOf(awardsMoney[5]));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes(1);
                return bonusVo;
            }
        } else {
            int awardNotDan = 0;
            for (int i = 0; i < 6; i++) {
                if (numberList.contains(awardNumber[i])) {
                    awardNotDan++;
                    awardContent += awardNumber[i] + ",";
                }
            }
            if (awardBlue && awardNotDan >= 6) {
                //一等
                bonusVo.setAward(true);
                bonusVo.setLevel("1");
                bonusVo.setMoney(Double.valueOf(awardsMoney[0]));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes(1);
                return bonusVo;
            } else if (awardNotDan >= 6) {
                //二等 奖
                bonusVo.setAward(true);
                bonusVo.setLevel("2");
                bonusVo.setMoney(Double.valueOf(awardsMoney[1]));
                bonusVo.setAwardContent(awardContent);
                int notes = 1;
                bonusVo.setAwardNotes(notes);
                return bonusVo;
            } else if (awardNotDan >= 5 && awardBlue) {
                //三等 奖 3000
                bonusVo.setAward(true);
                bonusVo.setLevel("3");
                bonusVo.setMoney(Double.valueOf(awardsMoney[2]));
                bonusVo.setAwardContent(awardContent);
                int notes = 1;
                bonusVo.setAwardNotes(notes);
                return bonusVo;
            } else if (awardNotDan >= 5 || (awardNotDan >= 4 && awardBlue)) {
                //四等 奖 200
                bonusVo.setAward(true);
                bonusVo.setLevel("4");
                bonusVo.setMoney(Double.valueOf(awardsMoney[3]));
                bonusVo.setAwardContent(awardContent);
                int notes = 1;
                bonusVo.setAwardNotes(notes);
                return bonusVo;
            } else if (awardNotDan >= 4 || (awardNotDan >= 3 && awardBlue)) {
                //五等 奖 10
                bonusVo.setAward(true);
                bonusVo.setLevel("5");
                bonusVo.setMoney(Double.valueOf(awardsMoney[4]));
                bonusVo.setAwardContent(awardContent);
                int notes = 1;
                bonusVo.setAwardNotes(notes);
                return bonusVo;
            } else if (awardBlue) {
                //六等 奖 5
                bonusVo.setAward(true);
                bonusVo.setLevel("6");
                bonusVo.setMoney(Double.valueOf(awardsMoney[5]));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes(1);
                return bonusVo;
            }
        }
        return bonusVo;
    }

    /*
     官方出号 7+1
        一等奖：投注号码与当期开奖号码中7个基本号码完全相同（顺序不限，下同）； 奖金总额为当期高奖等奖金的70%；
        二等奖：投注号码与当期开奖号码中任意6个基本号码及特别号码相同； 奖金总额为当期高奖等奖金的10%；
        三等奖：投注号码与当期开奖号码中任意6个基本号码相同；  奖金总额为当期高奖等奖金的20%；
        四等奖：投注号码与当期开奖号码中任意5个基本号码及特别号码相同；  单注奖金额固定为200元；
        五等奖：投注号码与当期开奖号码中任意5个基本号码相同；          单注奖金额固定为50元；
        六等奖：投注号码与当期开奖号码中任意4个基本号码及特别号码相同；   单注奖金额固定为10元；
        七等奖：投注号码与开奖号码中任意4个基本号码相同                单注奖金额固定为5元。
     */
    public static BonusVo awardQLC(List<GrandLottoObjDTO> betList, String reaward, String awardMoney) {
        BonusVo bonusVo = new BonusVo();
        bonusVo.setAward(false);
        bonusVo.setLevel("");
        bonusVo.setMoney(0d);
        String[] awardNumber = StringUtils.splitByWholeSeparatorPreserveAllTokens(reaward, ",");
        String[] awardsMoney = StringUtils.splitByWholeSeparatorPreserveAllTokens(awardMoney, ",");
        //特别号码
        List<String> danList = loadDan(betList);
        List<String> numberList = loadNotDan(betList);
        String blueNumber = awardNumber[7];
        int awardDan = 0;
        int awardNotDan = 0;

        int blueAward = 0;
        //特殊号码是否中
        boolean awardBlue = false;
        boolean danflag = danList.size() > 0 ? true : false;
        //是否是胆拖，否则是复式
        String awardContent = "";
        if (danflag) {
            int maxNotDan = 7 - danList.size();
            int maxDan = danList.size();
            //排除最后一个号码 特殊号码
            for (int i = 0; i < 7; i++) {
                if (danList.contains(awardNumber[i])) {
                    awardDan++;
                    awardContent += awardNumber[i] + ",";
                }
                if (numberList.contains(awardNumber[i])) {
                    awardNotDan++;
                    awardContent += awardNumber[i] + ",";
                }
            }
            awardBlue = danList.contains(blueNumber) || numberList.contains(blueNumber);
            if (awardBlue) {
                awardContent = awardContent + "+" + blueNumber;
            }
            int cawardNotDan = awardNotDan >= maxNotDan ? maxNotDan : awardNotDan;
            int cawardDan = awardDan >= maxDan ? maxDan : awardDan;

            if (cawardDan + cawardNotDan >= 7) {
                //中一等奖
                bonusVo.setAward(true);
                bonusVo.setLevel("1");
                bonusVo.setMoney(Double.valueOf(awardsMoney[0]));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes(1);
                return bonusVo;
            } else if (cawardDan + cawardNotDan >= 6 && awardBlue) {
                //二等奖
                bonusVo.setAward(true);
                bonusVo.setLevel("2");
                bonusVo.setMoney(Double.valueOf(awardsMoney[1]));
                bonusVo.setAwardContent(awardContent);
                int notes = awardNotDan > maxNotDan ? CombinationUtil.getCombination(awardNotDan, maxNotDan) : 1;
                bonusVo.setAwardNotes(notes);
                return bonusVo;
            } else if (cawardDan + cawardNotDan >= 6) {
                //三等奖
                bonusVo.setAward(true);
                bonusVo.setLevel("3");
                bonusVo.setMoney(Double.valueOf(awardsMoney[2]));
                bonusVo.setAwardContent(awardContent);
                int notes = awardNotDan > maxNotDan ? CombinationUtil.getCombination(awardNotDan, maxNotDan) : 1;
                bonusVo.setAwardNotes(notes);
                return bonusVo;
            } else if (cawardDan + cawardNotDan >= 5 && awardBlue) {
                //四等奖
                bonusVo.setAward(true);
                bonusVo.setLevel("4");
                bonusVo.setMoney(Double.valueOf(awardsMoney[3]));
                bonusVo.setAwardContent(awardContent);
                int notes = awardNotDan > maxNotDan ? CombinationUtil.getCombination(awardNotDan, maxNotDan) : 1;
                bonusVo.setAwardNotes(notes);
                return bonusVo;
            } else if (cawardDan + cawardNotDan >= 5) {
                //五等奖
                bonusVo.setAward(true);
                bonusVo.setLevel("5");
                bonusVo.setMoney(Double.valueOf(awardsMoney[4]));
                bonusVo.setAwardContent(awardContent);
                int notes = awardNotDan > maxNotDan ? CombinationUtil.getCombination(awardNotDan, maxNotDan) : 1;
                bonusVo.setAwardNotes(notes);
                return bonusVo;
            } else if (cawardDan + cawardNotDan >= 4 && awardBlue) {
                //六等奖
                bonusVo.setAward(true);
                bonusVo.setLevel("6");
                bonusVo.setMoney(Double.valueOf(awardsMoney[5]));
                bonusVo.setAwardContent(awardContent);
                int notes = awardNotDan > maxNotDan ? CombinationUtil.getCombination(awardNotDan, maxNotDan) : 1;
                bonusVo.setAwardNotes(notes);
                return bonusVo;
            } else if (cawardDan + cawardNotDan >= 4) {
                //七等奖
                bonusVo.setAward(true);
                bonusVo.setLevel("7");
                bonusVo.setMoney(Double.valueOf(awardsMoney[6]));
                bonusVo.setAwardContent(awardContent);
                int notes = awardNotDan > maxNotDan ? CombinationUtil.getCombination(awardNotDan, maxNotDan) : 1;
                bonusVo.setAwardNotes(notes);
                return bonusVo;
            }
        } else {
            //排除最后一个号码 特殊号码
            for (int i = 0; i < 7; i++) {
                if (numberList.contains(awardNumber[i])) {
                    awardNotDan++;
                    awardContent += awardNumber[i] + ",";
                }
            }
            awardBlue = numberList.contains(blueNumber);
            if (awardBlue) {
                awardContent = awardContent + "+" + blueNumber;
            }
            if (awardNotDan >= 7) {
                //中一等奖
                bonusVo.setAward(true);
                bonusVo.setLevel("1");
                bonusVo.setMoney(Double.valueOf(awardsMoney[0]));
                bonusVo.setAwardContent(awardContent);
                bonusVo.setAwardNotes(1);
                return bonusVo;
            } else if (awardNotDan >= 6 && awardBlue) {
                //二等奖
                bonusVo.setAward(true);
                bonusVo.setLevel("2");
                bonusVo.setMoney(Double.valueOf(awardsMoney[1]));
                bonusVo.setAwardContent(awardContent);
                int notes = 1;
                bonusVo.setAwardNotes(notes);
                return bonusVo;
            } else if (awardNotDan >= 6) {
                //三等奖
                bonusVo.setAward(true);
                bonusVo.setLevel("3");
                bonusVo.setMoney(Double.valueOf(awardsMoney[2]));
                bonusVo.setAwardContent(awardContent);
                int notes = 1;
                bonusVo.setAwardNotes(notes);
                return bonusVo;
            } else if (awardNotDan >= 5 && awardBlue) {
                //四等奖
                bonusVo.setAward(true);
                bonusVo.setLevel("4");
                bonusVo.setMoney(Double.valueOf(awardsMoney[3]));
                bonusVo.setAwardContent(awardContent);
                int notes = 1;
                bonusVo.setAwardNotes(notes);
                return bonusVo;
            } else if (awardNotDan >= 5) {
                //五等奖
                bonusVo.setAward(true);
                bonusVo.setLevel("5");
                bonusVo.setMoney(Double.valueOf(awardsMoney[4]));
                bonusVo.setAwardContent(awardContent);
                int notes = 1;
                bonusVo.setAwardNotes(notes);
                return bonusVo;
            } else if (awardNotDan >= 4 && awardBlue) {
                //六等奖
                bonusVo.setAward(true);
                bonusVo.setLevel("6");
                bonusVo.setMoney(Double.valueOf(awardsMoney[5]));
                bonusVo.setAwardContent(awardContent);
                int notes = 1;
                bonusVo.setAwardNotes(notes);
                return bonusVo;
            } else if (awardNotDan >= 4) {
                //七等奖
                bonusVo.setAward(true);
                bonusVo.setLevel("7");
                bonusVo.setMoney(Double.valueOf(awardsMoney[6]));
                bonusVo.setAwardContent(awardContent);
                int notes = 1;
                bonusVo.setAwardNotes(notes);
                return bonusVo;
            }
        }
        return bonusVo;
    }

    public static List<String> loadDan(List<GrandLottoObjDTO> bet) {
        return bet.stream().filter(item -> item.getIsGallbladder()).map(i -> i.getNum()).collect(Collectors.toList());
    }

    public static List<String> loadNotDan(List<GrandLottoObjDTO> bet) {
        return bet.stream().filter(item -> !item.getIsGallbladder()).map(i -> i.getNum()).collect(Collectors.toList());
    }

    public static List<String> loadNumber(List<GrandLottoObjDTO> bet) {
        return bet.stream().map(i -> i.getNum()).collect(Collectors.toList());
    }

    /*
    最多可以选择25个红球，胆最多不能超过5个。
     */
    public static List<String> calculationSsq(List<GrandLottoObjDTO> redListDTO, List<GrandLottoObjDTO> blueListDTO) {
        List<String> danList = collectDanSSQ(redListDTO);
        List<String> redList = collectNumberSSQ(redListDTO);
        List<String> reList = new ArrayList<>();
        List<String> blueList = collectNumberSSQ(blueListDTO);
        //胆只有6个。
        if (danList.size() > 6) {
            return new ArrayList<>();
        }
        if (danList.size() + redList.size() > 25) {
            return new ArrayList<>();
        }
        List<String> frontList = new ArrayList<>();
        int maxSelected = 6 - danList.size();
        List<String> redStringList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(danList)) {
            List<List<String>> combine = CombinationUtil.getCombinations(redList.toArray(new String[0]), maxSelected);
            String danString = StringUtils.join(danList, ",");
            combine.stream().forEach(item -> {
                redStringList.add(danString + "," + StringUtils.join(item, ","));
            });
        } else {
            List<List<String>> combine = CombinationUtil.getCombinations(redList.toArray(new String[0]), maxSelected);
            combine.stream().forEach(item -> {
                redStringList.add(StringUtils.join(item, ","));
            });
        }
        blueList.stream().forEach(p -> {
            redStringList.forEach(p1 -> {
                //排序号码
                String[] ballString = StringUtils.splitByWholeSeparatorPreserveAllTokens(p1, ",");
                List<String> balls = Arrays.asList(ballString);
                balls.sort((a, b) -> {
                    return a.compareTo(b);
                });
                frontList.add(StringUtils.join(balls, ",") + "," + p);
            });
        });
        return frontList;
    }

    /*
        七乐彩最多可以选择16个红球，胆最多不能超过6个。
         */
    public static List<String> calculationQLC(List<GrandLottoObjDTO> redListDTO) {
        List<String> danList = collectDanSSQ(redListDTO);
        List<String> redList = collectNumberSSQ(redListDTO);
        List<String> reList = new ArrayList<>();
        //胆只有6个。
        if (danList.size() > 6) {
            return new ArrayList<>();
        }
        if (danList.size() + redList.size() > 16) {
            return new ArrayList<>();
        }
        List<String> frontList = new ArrayList<>();
        int maxSelected = 7 - danList.size();
        List<String> redStringList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(danList)) {
            List<List<String>> combine = CombinationUtil.getCombinations(redList.toArray(new String[0]), maxSelected);
            String danString = StringUtils.join(danList, ",");
            combine.stream().forEach(item -> {
                redStringList.add(danString + "," + StringUtils.join(item, ","));
            });
        } else {
            List<List<String>> combine = CombinationUtil.getCombinations(redList.toArray(new String[0]), maxSelected);
            combine.stream().forEach(item -> {
                redStringList.add(StringUtils.join(item, ","));
            });
        }
        //排序号码
        redStringList.forEach(p -> {
            String[] ballString = StringUtils.splitByWholeSeparatorPreserveAllTokens(p, ",");
            List<String> balls = Arrays.asList(ballString);
            balls.sort((a, b) -> {
                return a.compareTo(b);
            });
            frontList.add(StringUtils.join(balls, ","));
        });

        return frontList;
    }

    /*
     快乐8 最多16个号码
     */
    public static List<String> calculationKL8(List<GrandLottoObjDTO> redListDTO, String model) {
        int maxNumber = Integer.valueOf(model);
        List<String> danList = collectDanSSQ(redListDTO);
        List<String> redList = collectNumberSSQ(redListDTO);
        //胆只有6个。
        if (danList.size() > 6) {
            return new ArrayList<>();
        }
        if (danList.size() + redList.size() > 16) {
            return new ArrayList<>();
        }
        List<String> frontList = new ArrayList<>();
        int maxSelected = maxNumber - danList.size();
        List<String> redStringList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(danList)) {
            List<List<String>> combine = CombinationUtil.getCombinations(redList.toArray(new String[0]), maxSelected);
            String danString = StringUtils.join(danList, ",");
            combine.stream().forEach(item -> {
                redStringList.add(danString + "," + StringUtils.join(item, ","));
            });
        } else {
            List<List<String>> combine = CombinationUtil.getCombinations(redList.toArray(new String[0]), maxSelected);
            combine.stream().forEach(item -> {
                redStringList.add(StringUtils.join(item, ","));
            });
        }
        //排序号码
        redStringList.forEach(p -> {
            String[] ballString = StringUtils.splitByWholeSeparatorPreserveAllTokens(p, ",");
            List<String> balls = Arrays.asList(ballString);
            balls.sort((a, b) -> {
                return a.compareTo(b);
            });
            frontList.add(StringUtils.join(balls, ","));
        });

        return frontList;
    }

    private static List<String> collectNumberSSQ(List<GrandLottoObjDTO> redListDTO) {
        return redListDTO.stream().filter(item -> !item.getIsGallbladder()).map(item -> item.getNum()).collect(Collectors.toList());
    }

    private static List<String> collectDanSSQ(List<GrandLottoObjDTO> redListDTO) {
        return redListDTO.stream().filter(item -> item.getIsGallbladder()).map(item -> item.getNum()).collect(Collectors.toList());
    }

    /**
     * redList 红球
     * blueList 蓝球
     */
    public static List<String> calculation(List<GrandLottoObjDTO> redListDTO, List<GrandLottoObjDTO> blueListDTO) {
        Map<String, List<String>> bluemakeUpMap = new HashMap<>();
        redMakeUp(redListDTO, bluemakeUpMap);

        Map<String, List<String>> redmakeUpMap = new HashMap<>();
        blueMakeUp(blueListDTO, redmakeUpMap);

        List<String> newblueList = new ArrayList();
        for (String blueList : bluemakeUpMap.get("makeUpList")) {
            List<String> bluesList = new ArrayList();
            for (String s : blueList.split(",")) {
                if (!"X".equals(s)) {
                    bluesList.add(s);
                }
            }
            newblueList.add(StringUtils.join(bluesList, ","));
        }

        List<String> newredList = new ArrayList();
        for (String redList : redmakeUpMap.get("makeUpList")) {
            List<String> redsList = new ArrayList();
            for (String s : redList.split(",")) {
                if (!"X".equals(s)) {
                    redsList.add(s);
                }
            }
            newredList.add(StringUtils.join(redsList, ","));
        }

        List<String> rlist = new ArrayList();
        for (String bstring : newblueList) {
            for (String rstring : newredList) {
                rlist.add(bstring + "," + rstring);
            }
        }
        return rlist;
    }

    /**
     * @param redListDTO       红球
     * @param blueListDTO      蓝球
     * @param target           开奖号码
     * @param firstPrizeBonus  一等奖
     * @param secondPrizeBonus 二等奖
     * @return
     */
    public static Long award(List<GrandLottoObjDTO> redListDTO, List<GrandLottoObjDTO> blueListDTO, String target, String firstPrizeBonus, String secondPrizeBonus) {
        List<String> list = GrandLottoUtil.calculation(redListDTO, blueListDTO);
        Long price = 0L;
        for (String str : list) {
            List<String> strList = Convert.toList(String.class, str);
            List<String> targetList = Arrays.asList(target.split(","));
            int count = 0;
            for (String s : targetList) {
                if (strList.contains(s)) {
                    count++;
                    continue;
                }
            }
            //一等奖：投注号码与当期开奖号码全部相同(顺序不限，下同)，即中奖；
            if (count == 7) {
                price += Long.valueOf(firstPrizeBonus);
                continue;
            }
            String regexTwo1 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";
            String regexTwo2 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            //二等奖：投注号码与当期开奖号码中的五个前区号码及任意一个后区号码相同，即中奖
            if (str.matches(regexTwo1) || str.matches(regexTwo2)) {
                price += Long.valueOf(secondPrizeBonus);
                continue;
            }
            //三等奖：投注号码与当期开奖号码中的五个前区号码相同，即中奖；
            String regexThree1 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," + "[0-9][0-9]";
            if (str.matches(regexThree1)) {
                price += Long.valueOf(10000);
                continue;
            }
            //四等奖：投注号码与当期开奖号码中的任意四个前区号码及两个后区号码相同，即中奖；
            String regexFour1 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexFour2 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexFour3 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexFour4 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexFour5 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            if (str.matches(regexFour1) || str.matches(regexFour2) || str.matches(regexFour3) || str.matches(regexFour4) || str.matches(regexFour5)) {
                price += Long.valueOf(3000);
                continue;
            }
            //五等奖：投注号码与当期开奖号码中的任意四个前区号码及任意一个后区号码相同，即中奖；
            String regexFive1 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";

            String regexFive2 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";

            String regexFive3 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";

            String regexFive4 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";

            String regexFive5 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";

            String regexFive6 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";

            String regexFive7 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";

            String regexFive8 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";

            String regexFive9 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";

            String regexFive10 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";
            if (str.matches(regexFive1) || str.matches(regexFive2) || str.matches(regexFive3) || str.matches(regexFive4) || str.matches(regexFive5) || str.matches(regexFive6) || str.matches(regexFive7) || str.matches(regexFive8) || str.matches(regexFive9) || str.matches(regexFive10)) {
                price += Long.valueOf(300);
                continue;
            }
            //六等奖：投注号码与当期开奖号码中的任意三个前区号码及两个后区号码相同，即中奖；
            String regexSix1 = "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexSix2 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexSix3 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexSix4 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexSix5 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexSix6 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexSix7 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexSix8 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexSix9 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexSix10 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            if (str.matches(regexSix1) || str.matches(regexSix2) || str.matches(regexSix3) || str.matches(regexSix4) || str.matches(regexSix5) || str.matches(regexSix6) || str.matches(regexSix7) || str.matches(regexSix8) || str.matches(regexSix9) || str.matches(regexSix10)) {
                price += Long.valueOf(200);
                continue;
            }
            //七等奖：投注号码与当期开奖号码中的任意四个前区号码相同，即中奖；
            String regexSeven1 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]";
            String regexSeven2 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]";
            String regexSeven3 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]";
            String regexSeven4 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]";
            String regexSeven5 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]";
            if (str.matches(regexSeven1) || str.matches(regexSeven2) || str.matches(regexSeven3) || str.matches(regexSeven4) || str.matches(regexSeven5)) {
                price += Long.valueOf(100);
                continue;
            }
            //八等奖：投注号码与当期开奖号码中的任意三个前区号码及任意一个后区号码相同，或者任意两个前区号码及两个后区号码相同，即中奖;
            String regexEight1 = "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexEight2 = "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";
            String regexEight3 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexEight4 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";
            String regexEight5 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexEight6 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";
            String regexEight7 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexEight8 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";
            String regexEight9 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexEight10 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";
            String regexEight11 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexEight12 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";
            String regexEight13 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexEight14 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";
            String regexEight15 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexEight16 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";
            String regexEight17 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexEight18 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";
            String regexEight19 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexEight20 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";
            String regexEight21 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexEight22 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexEight23 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexEight24 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexEight25 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexEight26 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexEight27 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexEight28 = "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexEight29 = "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexEight30 = "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            if (str.matches(regexEight1)
                    || str.matches(regexEight2)
                    || str.matches(regexEight3)
                    || str.matches(regexEight4)
                    || str.matches(regexEight5)
                    || str.matches(regexEight6)
                    || str.matches(regexEight7)
                    || str.matches(regexEight8)
                    || str.matches(regexEight9)
                    || str.matches(regexEight10)
                    || str.matches(regexEight11)
                    || str.matches(regexEight12)
                    || str.matches(regexEight13)
                    || str.matches(regexEight14)
                    || str.matches(regexEight15)
                    || str.matches(regexEight16)
                    || str.matches(regexEight17)
                    || str.matches(regexEight18)
                    || str.matches(regexEight19)
                    || str.matches(regexEight20)
                    || str.matches(regexEight21)
                    || str.matches(regexEight22)
                    || str.matches(regexEight23)
                    || str.matches(regexEight24)
                    || str.matches(regexEight25)
                    || str.matches(regexEight26)
                    || str.matches(regexEight27)
                    || str.matches(regexEight28)
                    || str.matches(regexEight29)
                    || str.matches(regexEight30)
            ) {
                price += Long.valueOf(15);
                continue;
            }
            //九等奖：投注号码与当期开奖号码中的任意三个前区号码相同，或者任意一个前区号码及两个后区号码相同，或者任意两个前区号码及任意一个后区号码相同，或者两个后区号码相同，即中奖。
            String regexNine1 = "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]";
            String regexNine2 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]";
            String regexNine3 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]";
            String regexNine4 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]";
            String regexNine5 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]";
            String regexNine6 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]";
            String regexNine7 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]";
            String regexNine8 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]";
            String regexNine9 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]";
            String regexNine10 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]";
            String regexNine11 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexNine12 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexNine13 = "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexNine14 = "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexNine15 = "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexNine16 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexNine17 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";
            String regexNine18 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexNine19 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]" +
                    "[0-9][0-9]";
            String regexNine20 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexNine21 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";
            String regexNine22 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexNine23 = "[" + tear(targetList.get(0))[0] + "][" + tear(targetList.get(0))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";
            String regexNine24 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexNine25 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";
            String regexNine26 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexNine27 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";
            String regexNine28 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexNine29 = "[0-9][0-9]," +
                    "[" + tear(targetList.get(1))[0] + "][" + tear(targetList.get(1))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";
            String regexNine30 = "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexNine31 = "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";
            String regexNine32 = "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexNine33 = "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(2))[0] + "][" + tear(targetList.get(2))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";
            String regexNine34 = "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            String regexNine35 = "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(3))[0] + "][" + tear(targetList.get(3))[1] + "]," +
                    "[" + tear(targetList.get(4))[0] + "][" + tear(targetList.get(4))[1] + "]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[0-9][0-9]";
            String regexNine36 = "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[0-9][0-9]," +
                    "[" + tear(targetList.get(5))[0] + "][" + tear(targetList.get(5))[1] + "]," +
                    "[" + tear(targetList.get(6))[0] + "][" + tear(targetList.get(6))[1] + "]";
            if (str.matches(regexSix1)
                    || str.matches(regexNine1)
                    || str.matches(regexNine2)
                    || str.matches(regexNine3)
                    || str.matches(regexNine4)
                    || str.matches(regexNine5)
                    || str.matches(regexNine6)
                    || str.matches(regexNine7)
                    || str.matches(regexNine8)
                    || str.matches(regexNine9)
                    || str.matches(regexNine10)
                    || str.matches(regexNine11)
                    || str.matches(regexNine12)
                    || str.matches(regexNine13)
                    || str.matches(regexNine14)
                    || str.matches(regexNine15)
                    || str.matches(regexNine16)
                    || str.matches(regexNine17)
                    || str.matches(regexNine18)
                    || str.matches(regexNine19)
                    || str.matches(regexNine20)
                    || str.matches(regexNine21)
                    || str.matches(regexNine22)
                    || str.matches(regexNine23)
                    || str.matches(regexNine24)
                    || str.matches(regexNine25)
                    || str.matches(regexNine26)
                    || str.matches(regexNine27)
                    || str.matches(regexNine28)
                    || str.matches(regexNine29)
                    || str.matches(regexNine30)
                    || str.matches(regexNine31)
                    || str.matches(regexNine32)
                    || str.matches(regexNine33)
                    || str.matches(regexNine34)
                    || str.matches(regexNine35)
                    || str.matches(regexNine36)
            ) {
                price += Long.valueOf(5);
                continue;
            }
        }
        return price;
    }

    /**
     * 将一个数字拆成二个数字
     *
     * @param str
     * @return
     */
    private static String[] tear(String str) {
        String[] arr = new String[2];
        Integer num = Integer.valueOf(str);
        String g = String.valueOf(num % 10);
        String s = String.valueOf(num / 10 % 10);
        arr[0] = s;
        arr[1] = g;
        return arr;
    }

    /**
     * 组配下注结果
     *
     * @param blueList
     * @param makeUpMap
     * @return
     */
    private static Map<String, List<String>> redMakeUp(List<GrandLottoObjDTO> blueList, Map<String, List<String>> makeUpMap) {
        //组合list
        List<String> makeUpList = new ArrayList<>();

        int x = 0;

        List<GrandLottoObjDTO> rList = new ArrayList<>();
        for (int i = 0; i < 35; i++) {
            String y = "" + (i + 1);
            if (i < 9) {
                y = "0" + (i + 1);
            }

            if (blueList.size() > x && blueList.get(x).getNum().equals(y)) {
                if (!blueList.get(x).getIsGallbladder()) {
                    blueList.get(x).setNum(blueList.get(x).getNum() + ",X");
                }
                rList.add(blueList.get(x));
                x++;
            } else {
                GrandLottoObjDTO grandLottoObjDTO = new GrandLottoObjDTO();
                grandLottoObjDTO.setIsGallbladder(false);
                grandLottoObjDTO.setNum("X");
                rList.add(grandLottoObjDTO);
            }
        }

        for (String B1 : rList.get(0).getNum().split(",")) {
            for (String B2 : rList.get(1).getNum().split(",")) {
                for (String B3 : rList.get(2).getNum().split(",")) {
                    for (String B4 : rList.get(3).getNum().split(",")) {
                        for (String B5 : rList.get(4).getNum().split(",")) {
                            for (String B6 : rList.get(5).getNum().split(",")) {
                                for (String B7 : rList.get(6).getNum().split(",")) {
                                    for (String B8 : rList.get(7).getNum().split(",")) {
                                        for (String B9 : rList.get(8).getNum().split(",")) {
                                            for (String B10 : rList.get(9).getNum().split(",")) {
                                                for (String B11 : rList.get(10).getNum().split(",")) {
                                                    for (String B12 : rList.get(11).getNum().split(",")) {
                                                        for (String B13 : rList.get(12).getNum().split(",")) {
                                                            for (String B14 : rList.get(13).getNum().split(",")) {
                                                                for (String B15 : rList.get(14).getNum().split(",")) {
                                                                    for (String B16 : rList.get(15).getNum().split(",")) {
                                                                        for (String B17 : rList.get(16).getNum().split(",")) {
                                                                            for (String B18 : rList.get(17).getNum().split(",")) {
                                                                                for (String B19 : rList.get(18).getNum().split(",")) {
                                                                                    for (String B20 : rList.get(19).getNum().split(",")) {
                                                                                        for (String B21 : rList.get(20).getNum().split(",")) {
                                                                                            for (String B22 : rList.get(21).getNum().split(",")) {
                                                                                                for (String B23 : rList.get(22).getNum().split(",")) {
                                                                                                    for (String B24 : rList.get(23).getNum().split(",")) {
                                                                                                        for (String B25 : rList.get(24).getNum().split(",")) {
                                                                                                            for (String B26 : rList.get(25).getNum().split(",")) {
                                                                                                                for (String B27 : rList.get(26).getNum().split(",")) {
                                                                                                                    for (String B28 : rList.get(27).getNum().split(",")) {
                                                                                                                        for (String B29 : rList.get(28).getNum().split(",")) {
                                                                                                                            for (String B30 : rList.get(29).getNum().split(",")) {
                                                                                                                                for (String B31 : rList.get(30).getNum().split(",")) {
                                                                                                                                    for (String B32 : rList.get(31).getNum().split(",")) {
                                                                                                                                        for (String B33 : rList.get(32).getNum().split(",")) {
                                                                                                                                            for (String B34 : rList.get(33).getNum().split(",")) {
                                                                                                                                                for (String B35 : rList.get(34).getNum().split(",")) {
                                                                                                                                                    String str = B1 + "," + B2 + "," + B3 + "," + B4 + "," + B5 + "," + B6 + "," + B7 + "," + B8 + "," + B9 + "," + B10 + "," + B11 + "," + B12 + "," + B13 + "," + B14 + "," + B15 + "," + B16 + "," + B17 + "," + B18 + "," + B19 + "," + B20 + "," + B21 + "," + B22 + "," + B23 + "," + B24 + "," + B25 + "," + B26 + "," + B27 + "," + B28 + "," + B29 + "," + B30 + "," + B31 + "," + B32 + "," + B33 + "," + B34 + "," + B35;
                                                                                                                                                    makeUpList.add(str);
                                                                                                                                                }
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            }
                                                                                                                        }
                                                                                                                    }
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Set<String> makeUpRSet = new HashSet<>();
        List<String> makeUpRList = new ArrayList<>();
        for (String s : makeUpList) {
            int iCunt = getStrCunt(s, "X");
            if (iCunt == 30) {
                if (makeUpRSet.add(s)) {
                    makeUpRList.add(s);
                }
            }
        }
        makeUpMap.put("makeUpList", makeUpRList);
        return makeUpMap;
    }

    /**
     * 组配下注结果
     *
     * @param blueList
     * @param makeUpMap
     * @return
     */
    private static Map<String, List<String>> blueMakeUp(List<GrandLottoObjDTO> blueList, Map<String, List<String>> makeUpMap) {
        //组合list
        List<String> makeUpList = new ArrayList<>();

        int x = 0;

        List<GrandLottoObjDTO> rList = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            String y = "" + (i + 1);
            if (i < 9) {
                y = "0" + (i + 1);
            }

            if (blueList.size() > x && blueList.get(x).getNum().equals(y)) {
                if (!blueList.get(x).getIsGallbladder()) {
                    blueList.get(x).setNum(blueList.get(x).getNum() + ",X");
                }
                rList.add(blueList.get(x));
                x++;
            } else {
                GrandLottoObjDTO grandLottoObjDTO = new GrandLottoObjDTO();
                grandLottoObjDTO.setIsGallbladder(false);
                grandLottoObjDTO.setNum("X");

                rList.add(grandLottoObjDTO);
            }
        }

        for (String B1 : rList.get(0).getNum().split(",")) {
            for (String B2 : rList.get(1).getNum().split(",")) {
                for (String B3 : rList.get(2).getNum().split(",")) {
                    for (String B4 : rList.get(3).getNum().split(",")) {
                        for (String B5 : rList.get(4).getNum().split(",")) {
                            for (String B6 : rList.get(5).getNum().split(",")) {
                                for (String B7 : rList.get(6).getNum().split(",")) {
                                    for (String B8 : rList.get(7).getNum().split(",")) {
                                        for (String B9 : rList.get(8).getNum().split(",")) {
                                            for (String B10 : rList.get(9).getNum().split(",")) {
                                                for (String B11 : rList.get(10).getNum().split(",")) {
                                                    for (String B12 : rList.get(11).getNum().split(",")) {
                                                        String str = B1 + "," + B2 + "," + B3 + "," + B4 + "," + B5 + "," + B6 + "," + B7 + "," + B8 + "," + B9 + "," + B10 + "," + B11 + "," + B12;
                                                        makeUpList.add(str);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Set<String> makeUpRSet = new HashSet<>();
        List<String> makeUpRList = new ArrayList<>();
        for (String s : makeUpList) {
            int iCunt = getStrCunt(s, "X");
            if (iCunt == 10) {
                if (makeUpRSet.add(s)) {
                    makeUpRList.add(s);
                }
            }
        }
        makeUpMap.put("makeUpList", makeUpRList);
        return makeUpMap;
    }

    private static int getStrCunt(String mainStr, String subStr) {
        // 声明一个要返回的变量
        int count = 0;
        // 声明一个初始的下标，从初始位置开始查找
        int index = 0;
        // 获取主数据的长度
        int mainStrLength = mainStr.length();
        // 获取要查找的数据长度
        int subStrLength = subStr.length();
        // 如果要查找的数据长度大于主数据的长度则返回0
        if (subStrLength > mainStrLength) {
            return 0;
        }
        // 循环使用indexOf查找出现的下标，如果出现一次则count++
        while ((index = mainStr.indexOf(subStr, index)) != -1) {
            count++;
            // 从找到的位置下标加上要查找的字符串长度，让指针往后移动继续查找
            index += subStrLength;
        }
        return count;
    }

    public static void main(String[] args) {
        //int award, int dm_cnt, int tm_cnt,
        //                                       int dm_award, int tm_award, int base
        System.out.println(PlayUtil.getAwardDTCount(0, 4, 8, 0, 0, 5));
    }
}
