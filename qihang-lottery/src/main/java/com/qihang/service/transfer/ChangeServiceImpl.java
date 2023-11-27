package com.qihang.service.transfer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qihang.annotation.TenantIgnore;
import com.qihang.common.util.http.HttpReq;
import com.qihang.common.vo.BaseDataVO;
import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.constant.TransferEnum;
import com.qihang.controller.shop.app.vo.ShopVO;
import com.qihang.controller.transferIn.admin.dto.LotteryAutoStateDTO;
import com.qihang.controller.transferIn.admin.dto.LotteryOutDTO;
import com.qihang.controller.transferIn.admin.vo.AdminShopTransferInVO;
import com.qihang.controller.transferIn.admin.vo.ShopOutVO;
import com.qihang.domain.ballgame.BallGameDO;
import com.qihang.domain.transfer.LotteryTransferDO;
import com.qihang.domain.transfer.ShopTransferDO;
import com.qihang.mapper.ballgame.BallGameMapper;
import com.qihang.mapper.transfer.LotteryTransferMapper;
import com.qihang.mapper.transfer.ShopTransferMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: wyong
 * @Date: 2023/11/25 025 21:06
 * @Description:
 **/
@Slf4j
@Service
public class ChangeServiceImpl implements IChangeService {


    @Resource
    ShopTransferMapper shopTransferMapper;

    @Resource
    LotteryTransferMapper lotteryTransferMapper;

    @Resource
    IShopTransferService shopTransferService;

    @Resource
    BallGameMapper ballGameMapper;

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
                    shopTransferDO.setUid(0L);
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
}
