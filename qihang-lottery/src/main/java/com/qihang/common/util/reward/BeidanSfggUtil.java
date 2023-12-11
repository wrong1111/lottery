package com.qihang.common.util.reward;

import com.alibaba.fastjson.JSON;
import com.qihang.common.util.CombinationUtil;
import com.qihang.constant.Constant;
import com.qihang.controller.beidan.dto.BeiDanMatchDTO;
import com.qihang.controller.order.admin.lottery.vo.SportSchemeDetailsListVO;
import com.qihang.controller.order.admin.lottery.vo.SportSchemeDetailsVO;
import com.qihang.domain.order.LotteryTicketDO;
import com.qihang.domain.order.vo.TicketContentVO;
import com.qihang.domain.order.vo.TicketVO;
import com.qihang.service.racingball.RacingBallServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class BeidanSfggUtil {

    public static List<LotteryTicketDO> getBeidanSfggTicketVOBySechme(String schedetail, List<BeiDanMatchDTO> footballMatchDTOS, String orderNo, String mode) {
        List<SportSchemeDetailsListVO> sportsDetails = JSON.parseArray(schedetail, SportSchemeDetailsListVO.class);
        Map<String, BeiDanMatchDTO> matchMap = footballMatchDTOS.stream().collect(Collectors.toMap(BeiDanMatchDTO::getNumber, a -> a));
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

    public static LotteryTicketDO replaceTicketVOBySechmeBallList(List<SportSchemeDetailsVO> detailsVOS, Map<String, BeiDanMatchDTO> footballMatchDTOMap, String orderNo, Integer times, Integer idx, String mode) {
        LotteryTicketDO lotteryTicketDO = new LotteryTicketDO();
        List<TicketVO> ticketVOList = new ArrayList<>();
        BigDecimal foreast = BigDecimal.ONE;
        List<String> matchList = new ArrayList<>();
        for (SportSchemeDetailsVO detailsVO : detailsVOS) {
            BeiDanMatchDTO footballMatchDTO = footballMatchDTOMap.get(detailsVO.getNumber());
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
        lotteryTicketDO.setTicketNo(idx + "");
        lotteryTicketDO.setRevokePrice(BigDecimal.ZERO);
        lotteryTicketDO.setPrice(BigDecimal.valueOf(2).multiply(BigDecimal.valueOf(lotteryTicketDO.getTimes())));
        lotteryTicketDO.setForecast(foreast.multiply(BigDecimal.valueOf(2)).multiply(BigDecimal.valueOf(lotteryTicketDO.getTimes())));
        lotteryTicketDO.setBetType("" + detailsVOS.size());
        lotteryTicketDO.setMatchs(StringUtils.join(matchList, ","));
        lotteryTicketDO.setTicketContent(JSON.toJSONString(ticketVOList));
        return lotteryTicketDO;
    }

    private static String[] parseSelectedOdds(String result) {
        // 胜(2.3)
        int idx = result.indexOf("(");
        int lastIdx = result.lastIndexOf(")");
        return new String[]{result.substring(0, idx), result.substring(idx + 1, lastIdx)};
    }

    /**
     * 串关 拆分票据
     */
    public static List<LotteryTicketDO> getBeidanSfggTicketVO(List<BeiDanMatchDTO> footballMatchDTOS, List<Integer> playType, String orderNo, Integer times) {
        List<LotteryTicketDO> lotteryTicketDOS = new ArrayList<>(footballMatchDTOS.size() * playType.size());
        //串一过关
        String[] matchArrays = new String[footballMatchDTOS.size()];
        int idx = 0;
        Map<String, ArrayList<TicketVO>> ticketMap = new HashMap<>(footballMatchDTOS.size());
        for (BeiDanMatchDTO dto : footballMatchDTOS) {
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
            p.setTicketNo(String.valueOf(idx++));
            p.setTicketState(0);
            p.setCreateTime(new Date());
            p.setRevokePrice(BigDecimal.ZERO);
            p.setState(0);
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
        // int multiPer = times % Constant.MAX_TICKET_MULTI == 0 ? times / Constant.MAX_TICKET_MULTI : times / Constant.MAX_TICKET_MULTI + 1;
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

                // for (int i = 0; i < multiPer; i++) {
//                    int mult = Constant.MAX_TICKET_MULTI;
//                    if (i == multiPer - 1) {
//                        //最后一票倍数
//                        mult = times % Constant.MAX_TICKET_MULTI;
//                    }
                int mult = times;
                LotteryTicketDO lotteryTicketDO = new LotteryTicketDO();
                lotteryTicketDO.setTicketNo(String.valueOf(++idx));
                lotteryTicketDO.setForecast(maxOddsList.stream().reduce(BigDecimal.ONE, BigDecimal::multiply).multiply(BigDecimal.valueOf(1.3d)).multiply(BigDecimal.valueOf(mult)));
                lotteryTicketDO.setBets(bets);
                lotteryTicketDO.setBetType("" + play);
                lotteryTicketDO.setTimes(mult);
                lotteryTicketDO.setOrderId(orderId);
                lotteryTicketDO.setPrice(BigDecimal.valueOf(bets).multiply(BigDecimal.valueOf(2)).multiply(BigDecimal.valueOf(mult)));
                lists.sort((a, b) -> a.compareTo(b));
                lotteryTicketDO.setMatchs(StringUtils.join(lists, ","));
                lotteryTicketDO.setTicketContent(JSON.toJSONString(ordersTicketList));
                lotteryTicketDOS.add(lotteryTicketDO);
                // }
            }
        }
        return lotteryTicketDOS;
    }

    private static ArrayList<TicketVO> buildTicketVO(BeiDanMatchDTO dto) {
        ArrayList<TicketVO> ticketVOList = new ArrayList<>();
        TicketVO ticketVO = new TicketVO();
        ticketVO.setMatch(dto.getMatch());
        ticketVO.setLetBall(dto.getLetBall());
        ticketVO.setNumber(dto.getNumber());
        ticketVO.setHomeTeam(dto.getHomeTeam());
        ticketVO.setVisitingTeam(dto.getVisitingTeam());
        List<TicketContentVO> ticketContentVOList = new ArrayList<>(100);
        //负
        if (!CollectionUtils.isEmpty(dto.getSfggOdds())) {
            ticketVO.setMode("5");
            for (Map<String, Object> list : dto.getSfggOdds()) {
                TicketContentVO ticketContentVO = JSON.parseObject(JSON.toJSONString(list), TicketContentVO.class);
                ticketContentVO.setMode("5");
                ticketContentVOList.add(ticketContentVO);
            }
            TicketVO newTicket = new TicketVO();
            BeanUtils.copyProperties(ticketVO, newTicket);
            newTicket.setTicketContentVOList(ticketContentVOList);
            ticketVOList.add(newTicket);
        }
        return ticketVOList;
    }

}
