package com.qihang.service.transfer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.MD5;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qihang.annotation.TenantIgnore;
import com.qihang.common.util.SpringContextUtils;
import com.qihang.common.util.http.HttpReq;
import com.qihang.common.util.upload.LocalUtil;
import com.qihang.common.vo.BaseDataVO;
import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.constant.TransferEnum;
import com.qihang.controller.shop.app.vo.ShopVO;
import com.qihang.controller.transferIn.admin.dto.LotteryAutoStateDTO;
import com.qihang.controller.transferIn.admin.dto.LotteryOutDTO;
import com.qihang.controller.transferIn.admin.vo.AdminShopTransferInVO;
import com.qihang.controller.transferIn.admin.vo.ShopOutVO;
import com.qihang.controller.transferOut.app.dto.ChangeOrderDTO;
import com.qihang.domain.ballgame.BallGameDO;
import com.qihang.domain.order.LotteryOrderDO;
import com.qihang.domain.order.LotteryTicketDO;
import com.qihang.domain.permutation.PermutationDO;
import com.qihang.domain.racingball.RacingBallDO;
import com.qihang.domain.transfer.LotteryTransferDO;
import com.qihang.domain.transfer.OrderTransferLogDO;
import com.qihang.domain.transfer.ShopTransferDO;
import com.qihang.enumeration.order.lottery.LotteryOrderStateEnum;
import com.qihang.enumeration.order.lottery.LotteryOrderTypeEnum;
import com.qihang.mapper.ballgame.BallGameMapper;
import com.qihang.mapper.order.LotteryOrderMapper;
import com.qihang.mapper.order.LotteryTicketMapper;
import com.qihang.mapper.permutation.PermutationMapper;
import com.qihang.mapper.racingball.RacingBallMapper;
import com.qihang.mapper.transfer.LotteryTransferMapper;
import com.qihang.mapper.transfer.OrderTransferLogMapper;
import com.qihang.mapper.transfer.ShopTransferMapper;
import com.qihang.service.order.ILotteryOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.qihang.service.transfer.ITransferOutServiceImpl.isSports;

/**
 * @Author: wyong
 * @Date: 2023/11/25 025 21:06
 * @Description:
 **/
@Slf4j
@Service
public class ChangeServiceImpl implements IChangeService {


    @Resource
    ILotteryOrderService lotteryOrderService;
    @Resource
    ShopTransferMapper shopTransferMapper;

    @Resource
    LotteryTransferMapper lotteryTransferMapper;

    @Resource
    IShopTransferService shopTransferService;

    @Resource
    BallGameMapper ballGameMapper;

    @Resource
    LotteryOrderMapper lotteryOrderMapper;

    @Resource
    RacingBallMapper racingBallMapper;

    @Resource
    PermutationMapper permutationMapper;

    @Resource
    OrderTransferLogMapper orderTransferLogMapper;

    @Resource
    LotteryTicketMapper lotteryTicketMapper;


    @TenantIgnore
    @Override
    public BaseVO info(String url) {
        BaseDataVO base = BaseDataVO.builder().build();
        //取出最后一个参数
        int idx = url.lastIndexOf("/");
        if (idx <= 0) {
            base.setSuccess(false);
            base.setErrorMsg("地址异常，请确认地址是否完整");
            return base;
        }
        String security = url.substring(idx + 1);

        String mainUrl = url.substring(0, idx);
        int idx2 = mainUrl.lastIndexOf("/");
        if (idx2 <= 0) {
            base.setSuccess(false);
            base.setErrorMsg("地址异常，请确认地址是否完整");
            return base;
        }
        String key = mainUrl.substring(idx2 + 1);
        String uri = mainUrl.substring(0, idx2);
        log.info(" 请求参数 key : {} security:{} ,url : {}", key, security, uri);
        try {
            String content = HttpReq.postJSON(mainUrl);
            log.info(" url : {} ,返回数据 ： {}", url, content);
            //保存到dict
            //查询上游提供的数据
            Map<String, Object> resultMap = JSON.parseObject(content, Map.class);
            base.setData(resultMap);
            if (resultMap.containsKey("success") && (Boolean) resultMap.get("success")) {
                base.setSuccess(true);

                Map<String, Object> dataMap = (Map<String, Object>) resultMap.get("data");

                ShopVO shopVO = BeanUtil.toBean((Map<String, Object>) dataMap.get("shop"), ShopVO.class);
                List<LotteryTransferDO> lotteryTransferDOS = BeanUtil.copyToList((List<Map<String, Object>>) dataMap.get("lotterys"), LotteryTransferDO.class);
                String postUrl = dataMap.get("url").toString();

                QueryWrapper<ShopTransferDO> queryWrapper = new QueryWrapper<ShopTransferDO>();
                queryWrapper.lambda().eq(ShopTransferDO::getTransferType, TransferEnum.TransferOut.code);
                queryWrapper.and(item -> (item.lambda().eq(ShopTransferDO::getTransferKey, key)).or().eq(ShopTransferDO::getShopName, shopVO.getName()));
                ShopTransferDO dbShopTransferDO = shopTransferMapper.selectOne(queryWrapper);
                if (null == dbShopTransferDO) {
                    //保存数据信息
                    ShopTransferDO shopTransferDO = new ShopTransferDO();
                    shopTransferDO.setShopName(shopVO.getName());
                    shopTransferDO.setShopConcatName("");
                    shopTransferDO.setShopConcatName("");
                    shopTransferDO.setUid(0);
                    shopTransferDO.setTransferKey(key);
                    shopTransferDO.setTransferSecurty(security);
                    shopTransferDO.setTransferType(TransferEnum.TransferOut.code);
                    shopTransferDO.setGolds(BigDecimal.ZERO);
                    shopTransferDO.setMoney(BigDecimal.ZERO);
                    shopTransferDO.setTransferInterface(postUrl);
                    shopTransferDO.setInterfaceState("0");
                    shopTransferDO.setCreateTime(new Date());
                    shopTransferDO.setUpdateTime(new Date());
                    shopTransferMapper.insert(shopTransferDO);
                    dbShopTransferDO = shopTransferDO;
                }
                for (LotteryTransferDO lotteryTransferDO : lotteryTransferDOS) {
                    //全部默认设成 手动
                    lotteryTransferDO.setTransferOutAuto(1);
                    //转单
                    lotteryTransferDO.setTransferFlag(1);
                    //查询是否有重复的
                    lotteryTransferDO.setShopId(dbShopTransferDO.getId());
                    LotteryTransferDO dbLottery = lotteryTransferMapper.selectOne(new QueryWrapper<LotteryTransferDO>().lambda().eq(LotteryTransferDO::getLotteryType, lotteryTransferDO.getLotteryType()).eq(LotteryTransferDO::getShopId, dbShopTransferDO.getId()).eq(LotteryTransferDO::getTransferFlag, TransferEnum.TransferOut.code));
                    if (null == dbLottery) {
                        lotteryTransferMapper.insert(lotteryTransferDO);
                    } else {
                        dbLottery.setCommiss(lotteryTransferDO.getCommiss());
                        dbLottery.setUpdateTime(new Date());
                        lotteryTransferMapper.updateById(dbLottery);
                    }
                }
            } else {
                base.setSuccess(false);
                base.setErrorCode(resultMap.get("errorCode").toString());
                base.setErrorMsg(resultMap.get("errorMsg").toString());
            }
        } catch (Exception e) {
            log.error(" url {} ,网络请求异常 {} ", url, e);
        }
        return base;
    }


    @TenantIgnore
    @Override
    public CommonListVO<AdminShopTransferInVO> list(LotteryOutDTO dto) {
        QueryWrapper<LotteryTransferDO> queryWrapper = new QueryWrapper<LotteryTransferDO>();
        if (ObjectUtil.isNotNull(dto.getLotteryId())) {
            queryWrapper.lambda().eq(LotteryTransferDO::getLotteryType, dto.getLotteryId());
        }
        if (ObjectUtil.isNotNull(dto.getShopId())) {
            queryWrapper.lambda().eq(LotteryTransferDO::getShopId, dto.getShopId());
        }
        if (ObjectUtil.isNotNull(dto.getState())) {
            queryWrapper.lambda().eq(LotteryTransferDO::getStates, dto.getState());
        }
        queryWrapper.lambda().eq(LotteryTransferDO::getTransferFlag, TransferEnum.TransferOut.code);
        //未开通的排最下面
        queryWrapper.lambda().orderByAsc(LotteryTransferDO::getStates);
        queryWrapper.lambda().orderByDesc(LotteryTransferDO::getId);
        //分页
        Page page = new Page<>(dto.getPageNo(), dto.getPageSize());

        Page<LotteryTransferDO> lotteryTransferDOS = lotteryTransferMapper.selectPage(page, queryWrapper);
        List<BallGameDO> ballGameDOS = ballGameMapper.selectList(new QueryWrapper<BallGameDO>().lambda().eq(BallGameDO::getTenantId, 1));
        Map<Integer, BallGameDO> ballGameDOMap = ballGameDOS.stream().collect(Collectors.toMap(BallGameDO::getLotid, item -> item, (a, b) -> a));
        CommonListVO<AdminShopTransferInVO> base = new CommonListVO<>();

        List<ShopTransferDO> shopTransferDOList = shopTransferMapper.selectList(new QueryWrapper<ShopTransferDO>().lambda().eq(ShopTransferDO::getTransferType, TransferEnum.TransferOut.code));
        Map<Integer, String> shopMap = shopTransferDOList.stream().collect(Collectors.toMap(ShopTransferDO::getId, ShopTransferDO::getShopName, (a, b) -> a));
        if (ObjectUtil.isNotNull(lotteryTransferDOS.getRecords())) {
            List<AdminShopTransferInVO> list = BeanUtil.copyToList(lotteryTransferDOS.getRecords(), AdminShopTransferInVO.class);
            for (AdminShopTransferInVO adminShopTransferInVO : list) {
                adminShopTransferInVO.setLotteryName(ballGameDOMap.get(adminShopTransferInVO.getLotteryType()).getName());
                adminShopTransferInVO.setIcon(ballGameDOMap.get(adminShopTransferInVO.getLotteryType()).getUrl());
                adminShopTransferInVO.setShopName(shopMap.get(adminShopTransferInVO.getShopId()));
            }
            base.setVoList(list);
            base.setTotal(page.getTotal());
            base.setTotal(lotteryTransferDOS.getTotal());
            return base;
        }
        return base;
    }

    @Override
    public CommonListVO<ShopOutVO> listShop() {
        List<ShopTransferDO> shopTransferDOList = shopTransferMapper.selectList(new QueryWrapper<ShopTransferDO>().lambda().eq(ShopTransferDO::getTransferType, TransferEnum.TransferOut.code));
        CommonListVO<ShopOutVO> commonListVO = new CommonListVO<>();
        if (ObjectUtil.isNotNull(shopTransferDOList)) {
            List<ShopOutVO> list = BeanUtil.copyToList(shopTransferDOList, ShopOutVO.class);
            commonListVO.setVoList(list);
            commonListVO.setTotal((long) shopTransferDOList.size());
            return commonListVO;
        }
        return commonListVO;
    }

    @Override
    public BaseVO editAutoState(LotteryAutoStateDTO autoStateDTO) {
        LotteryTransferDO lotteryTransferDO = lotteryTransferMapper.selectById(autoStateDTO.getId());
        if (ObjectUtil.isNotNull(lotteryTransferDO)) {
            lotteryTransferDO.setTransferOutAuto(autoStateDTO.getStates());
            lotteryTransferMapper.updateById(lotteryTransferDO);
            return new BaseVO();
        } else {
            return new BaseVO();
        }
    }


    @TenantIgnore
    @Override
    public BaseVO send(Integer id, boolean auto) {
        //查询对应的转单渠道
        LotteryOrderDO orderDO = lotteryOrderMapper.selectById(id);
        if (StringUtils.isNotBlank(orderDO.getTransferOrderNo())) {
            return BaseVO.builder().errorMsg("订单已转出,单号:" + orderDO.getTransferOrderNo()).build();
        }
        String lotteryId = orderDO.getType();
        //北单胜负过关 特殊处理。
        if (LotteryOrderTypeEnum.SIGLE_SFGG.getKey().equals(lotteryId)) {
            lotteryId = LotteryOrderTypeEnum.SINGLE.getKey();
        }
        LotteryTransferDO lotteryTransferDO = lotteryTransferMapper.selectOne(new QueryWrapper<LotteryTransferDO>().lambda()
                .eq(LotteryTransferDO::getStates, 0).eq(LotteryTransferDO::getLotteryType, lotteryId).eq(LotteryTransferDO::getTransferFlag, TransferEnum.TransferOut.code).orderByDesc(LotteryTransferDO::getId).last(" limit 1 "));
        if (null == lotteryTransferDO) {
            return BaseVO.builder().errorMsg("未找到对应的转单渠道").build();
        }
        if (auto) {
            //手动
            if (lotteryTransferDO.getTransferOutAuto() == 1) {
                return BaseVO.builder().errorMsg("当前转单渠道未开启自动转单").build();
            }
        }
        Integer shopId = lotteryTransferDO.getShopId();
        ShopTransferDO shopTransferDO = shopTransferMapper.selectById(shopId);
        if (null == shopTransferDO) {
            return BaseVO.builder().errorMsg("当前转单渠道未设置").build();
        }

        List<RacingBallDO> racingBallDOList = new ArrayList<>();
        List<PermutationDO> permutationDOList = new ArrayList<>();
        if (ITransferOutServiceImpl.isSports(Integer.valueOf(lotteryId))) {
            racingBallDOList = racingBallMapper.selectBatchIds(new ArrayList<>(Arrays.asList(orderDO.getTargetIds().split(","))));
        } else {
            permutationDOList = permutationMapper.selectBatchIds(new ArrayList<>(Arrays.asList(orderDO.getTargetIds().split(","))));
        }

        List<LotteryTicketDO> lotteryTicketDOS = lotteryTicketMapper.selectList(new QueryWrapper<LotteryTicketDO>().lambda().eq(LotteryTicketDO::getOrderId, orderDO.getOrderId()));
        if (CollectionUtil.isEmpty(lotteryTicketDOS)) {
            lotteryTicketDOS = CollectionUtil.newArrayList();
        }
        //底层网络交互
        BaseDataVO baseVO = sendOrder(shopTransferDO, orderDO, racingBallDOList, permutationDOList, lotteryTicketDOS);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("order", orderDO);
        dataMap.put("racing", racingBallDOList);
        dataMap.put("permuta", permutationDOList);
        dataMap.put("ticket", lotteryTicketDOS);
        OrderTransferLogDO logDO = OrderTransferLogDO.builder().shopId(shopId).orderId(orderDO.getOrderId())
                .type("" + TransferEnum.TransferOut.code).content(JSON.toJSONString(dataMap)).createTime(new Date()).receveMsg(JSON.toJSONString(baseVO)).build();
        orderTransferLogMapper.insert(logDO);
        if (baseVO.getSuccess() || (!baseVO.getSuccess() && "1".equals(baseVO.getErrorCode()))) {//重复下单 也是成功
            //修改订单状态
            Map<String, String> dataMap2 = (Map<String, String>) baseVO.getData();
            orderDO.setTransferShopId(shopId);
            orderDO.setTransferTime(new Date());
            orderDO.setTransferOrderNo(dataMap2.get("orderNo"));
            orderDO.setTransferType(1);
            lotteryOrderMapper.updateById(orderDO);
        }
        return baseVO;
    }

    @TenantIgnore
    @Async(value = "threadPoolTaskExecutor")
    @Override
    public BaseVO sendSync(Integer id, boolean auto) {
        return send(id, auto);
    }

    @TenantIgnore
    @Override
    public BaseVO chageState(Integer id) {
        //查询每个转单的上游信息
        List<ShopTransferDO> shopTransferDOS = shopTransferMapper.selectList(new QueryWrapper<ShopTransferDO>()
                .lambda().eq(ShopTransferDO::getTransferType, TransferEnum.TransferOut.code));
        if (CollectionUtil.isEmpty(shopTransferDOS)) {
            return BaseVO.builder().success(false).errorMsg("未找到转单渠道").build();
        }
        Map<Integer, ShopTransferDO> shopTransferDOMap = shopTransferDOS.stream().collect(Collectors.toMap(ShopTransferDO::getId, shopTransferDO -> shopTransferDO));

        List<LotteryOrderDO> lotteryOrderDOS = new ArrayList<>();
        if (null == id) {
            //批量 转单 查询
            lotteryOrderDOS = lotteryOrderMapper.selectList(new QueryWrapper<LotteryOrderDO>().isNotNull("transfer_order_no").lambda()
                    .eq(LotteryOrderDO::getTransferType, 1)
                    .isNull(LotteryOrderDO::getBill).gt(LotteryOrderDO::getCreateTime, DateUtils.addDays(new Date(), -2)).orderByDesc(LotteryOrderDO::getId));
        } else {
            LotteryOrderDO lotteryOrderDO = lotteryOrderMapper.selectById(id);
            if (lotteryOrderDO.getTransferType() == 1 && null == lotteryOrderDO.getBill()) {
                lotteryOrderDOS.add(lotteryOrderDO);
            }
        }
        if (CollectionUtil.isEmpty(lotteryOrderDOS)) {
            return BaseVO.builder().success(true).errorMsg("没有订单需要查询").build();
        }
        Map<String, String> orderNoMap = new HashMap<>();
        Map<Integer, List<LotteryOrderDO>> lotteryOrderDOMap = lotteryOrderDOS.stream().collect(Collectors.groupingBy(LotteryOrderDO::getTransferShopId));
        for (Map.Entry<Integer, List<LotteryOrderDO>> entry : lotteryOrderDOMap.entrySet()) {
            List<LotteryOrderDO> queryList = entry.getValue();
            int size = 400;
            int index = queryList.size() % size == 0 ? queryList.size() / size : ((int) (queryList.size() / size)) + 1;
            ShopTransferDO shopTransferDO = shopTransferDOMap.get(entry.getKey());
            if (shopTransferDO == null) {
                log.error(" 转单渠道[{}]未找到", entry.getKey());
                continue;
            }
            for (int i = 0; i < index; i++) {
                List<String> orderNoList = queryList.stream().skip(i * size).limit(size).map(item -> item.getTransferOrderNo()).collect(Collectors.toList());
                String data = StringUtils.join(orderNoList, ",");
                String result = buildPostQuery("changeState", shopTransferDO, data);
                log.info(" orderId:{},result: {}", data, result);
                if (StringUtils.isBlank(result)) {
                    log.error("<<<< data {},取票样接口异常 {}", data, result);
                } else {
                    //处理返回的数据
                    BaseDataVO baseDataVO = JSON.parseObject(result, BaseDataVO.class);
                    if (baseDataVO.getSuccess()) {
                        Map<String, String> dataMap = (Map<String, String>) baseDataVO.getData();
                        orderNoMap.putAll(dataMap);
                        for (Map.Entry<String, String> orderNo : orderNoMap.entrySet()) {
                            String orderNoVal = orderNo.getValue();
                            if (orderNoVal.startsWith("已出")) {
                                String[] ary = StringUtils.split(orderNoVal, "|");
                                if (ary.length == 3) {
                                    //上游没有传图片也要更新
                                    LotteryOrderDO updateOrderDO = new LotteryOrderDO();
                                    updateOrderDO.setTransferOrderNo(orderNo.getKey());
                                    updateOrderDO.setState(LotteryOrderStateEnum.TO_BE_AWARDED.getKey());
                                    updateOrderDO.setUpdateTime(new Date());
                                    if (StringUtils.isBlank(ary[1])) {
                                        updateOrderDO.setTicketingTime(new Date());
                                    } else {
                                        updateOrderDO.setTicketingTime(DateUtil.parse(ary[1], "yyyy-MM-dd HH:mm:ss"));
                                    }
                                    //图片需要下载到本地
                                    String bill = "";
                                    if (StringUtils.isNotBlank(ary[2])) {
                                        bill = loadPicture(ary[2]);
                                        updateOrderDO.setBill(bill);
                                    }
                                    if (StringUtils.isNotBlank(bill)) {
                                        orderNoMap.put(orderNo.getKey(), ary[0] + "|" + ary[1] + "|" + bill);
                                    }
                                    int c = lotteryOrderMapper.update(updateOrderDO, new LambdaQueryWrapper<LotteryOrderDO>().eq(LotteryOrderDO::getState, LotteryOrderStateEnum.TO_BE_ISSUED.getKey()).eq(LotteryOrderDO::getTransferOrderNo, orderNo.getKey()));
                                    updateOrderDO.setState(null);
                                    c = lotteryOrderMapper.update(updateOrderDO, new LambdaQueryWrapper<LotteryOrderDO>().eq(LotteryOrderDO::getTransferOrderNo, orderNo.getKey()));
                                    if (c > 0) {

                                    }
                                }
                            } else if (orderNoVal.startsWith("退票")) {
                                //退票业务处理
                                LotteryOrderDO lotteryOrderDO = lotteryOrderMapper.selectOne(new LambdaQueryWrapper<LotteryOrderDO>().eq(LotteryOrderDO::getTransferOrderNo, orderNo.getKey()));
                                lotteryOrderService.retreat(lotteryOrderDO.getId());
                            }
                        }
                    }
                }
            }
        }
        return BaseDataVO.builder().success(true).data(orderNoMap).build();
    }


    @Async(value = "threadPoolTaskExecutor")
    @TenantIgnore
    @Override
    public BaseVO chageStateAsync(Integer id) {
        return chageState(id);
    }

    @TenantIgnore
    @Override
    public void scheduleAutoChange() {
        //查询当前需要自动派送的彩种
        List<LotteryTransferDO> lotteryTransferDOS = lotteryTransferMapper.selectList(new QueryWrapper<LotteryTransferDO>().lambda().eq(LotteryTransferDO::getStates, 0).eq(LotteryTransferDO::getTransferFlag, TransferEnum.TransferOut.code).eq(LotteryTransferDO::getTransferOutAuto, 0));
        if (ObjectUtil.isNull(lotteryTransferDOS)) {
            return;
        }
        //查询当前需要自动派送的订单 5分钟以内的
        Date now = DateUtils.addMinutes(new Date(), -15);
        List<String> lotteryListIds = lotteryTransferDOS.stream().map(item -> "" + item.getLotteryType()).collect(Collectors.toList());
        //增加北单胜负过关
        lotteryListIds.add(LotteryOrderTypeEnum.SIGLE_SFGG.getKey());
        List<LotteryOrderDO> lotteryOrderDOS = lotteryOrderMapper.selectList(new QueryWrapper<LotteryOrderDO>().lambda().in(LotteryOrderDO::getType, lotteryListIds)
                .gt(LotteryOrderDO::getCreateTime, now).isNull(LotteryOrderDO::getTransferType).isNull(LotteryOrderDO::getTransferOrderNo).orderByDesc(LotteryOrderDO::getId));
        if (CollectionUtil.isNotEmpty(lotteryOrderDOS)) {
            IChangeService changeService = SpringContextUtils.getBean(ChangeServiceImpl.class);
            for (LotteryOrderDO lotteryOrderDO : lotteryOrderDOS) {
                try {
                    changeService.sendSync(lotteryOrderDO.getId(), true);
                } catch (Exception e) {
                    log.error("自动派送失败", e);
                }
            }
        }
    }

    @Override
    public BaseVO editLotteryTransferDisable(Integer id, Integer state) {
        LotteryTransferDO lotteryTransferDO = new LotteryTransferDO();
        lotteryTransferDO.setId(id);
        lotteryTransferDO.setStates(state);
        lotteryTransferMapper.updateById(lotteryTransferDO);
        return BaseVO.builder().success(true).errorMsg("成功").build();
    }

    public BaseDataVO sendOrder(ShopTransferDO shopTransferDO, LotteryOrderDO lotteryOrderDO, List<RacingBallDO> racingBallDOList, List<PermutationDO> permutationDOS, List<LotteryTicketDO> ticketDOList) {
        ChangeOrderDTO dto = new ChangeOrderDTO();
        dto.setOrderDO(lotteryOrderDO);
        dto.setOrderMoney(lotteryOrderDO.getPrice());
        dto.setLotteryId(Integer.valueOf(lotteryOrderDO.getType()));
        if (isSports(Integer.valueOf(lotteryOrderDO.getType()))) {
            dto.setRacingBallDOList(racingBallDOList);
        } else {
            dto.setPermutationDOList(permutationDOS);
        }
        dto.setTicketDOList(ticketDOList);
        String data = JSON.toJSONString(dto);
        String result = buildPostQuery("createOrder", shopTransferDO, data);
        log.info(" orderId:{},result: {}", lotteryOrderDO.getOrderId(), result);
        if (StringUtils.isBlank(result)) {
            return BaseDataVO.builder().success(false).errorCode("-1").errorMsg("转单接口异常，请联系管理员").build();
        }
        BaseDataVO baseDataVO = JSON.parseObject(result, BaseDataVO.class);
        return baseDataVO;

    }

    public String buildPostQuery(String command, ShopTransferDO shopTransferDO, String data) {
        String url = shopTransferDO.getTransferInterface();
        String key = shopTransferDO.getTransferKey();
        String security = shopTransferDO.getTransferSecurty();

        int timestamp = (int) (System.currentTimeMillis() / 1000);
        String signString = command + key + data + "1.0" + timestamp + security;
        try {
            MD5 md5 = new MD5(shopTransferDO.getTransferSecurty().getBytes("utf-8"));
            String sign = md5.digestHex(signString);
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("sign", sign);
            dataMap.put("key", key);
            dataMap.put("timestamp", "" + timestamp);
            dataMap.put("action", command);
            dataMap.put("data", data);
            dataMap.put("version", "1.0");
            String result = HttpReq.postJSON(url, dataMap);
            log.info(" request:{},result: {}", data, result);
            return result;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }

    @Resource
    LocalUtil localUtil;


    public String loadPicture(String targetPics) {
        if (StringUtils.isBlank(targetPics)) {
            return "";
        }
        String[] pics = StringUtils.split(targetPics, ",");
        List<String> list = new ArrayList<>(Arrays.asList(pics));
        list = list.stream().filter(p -> StringUtils.isNotBlank(p)).collect(Collectors.toList());
        return callFurturn(list);
    }

    private String callFurturn(List<String> arys) {
        // 创建异步任务数组
        CompletableFuture<String>[] futures = new CompletableFuture[arys.size()];
        int i = 0;
        for (String s : arys) {
            futures[i] = CompletableFuture.supplyAsync(() -> makeRequest(s));
            i++;
        }
        // 等待所有任务完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures);
        List<String> results = new ArrayList<>();
        // 同步返回结果数组
        try {
            allFutures.get(); // 等待所有任务完成
            for (int ii = 0; ii < futures.length; ii++) {
                results.add(futures[ii].get()); // 获取每个任务的结果
            }
            // 处理结果数组
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return StringUtils.join(results, ",");
    }

    private String makeRequest(String pic) {
        int idx = localUtil.getFilePath().lastIndexOf("/");
        String filePath = "";
        if (idx > 0) {
            String splitPath = localUtil.getFilePath().substring(idx + 1);
            filePath = pic.substring(pic.indexOf(splitPath));
        } else {
            filePath = pic.substring(pic.indexOf("/"));
        }
        InputStream is = null;
        try {
            is = new URL(pic).openStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return localUtil.saveFile(is, filePath);
    }
}
