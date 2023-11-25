package com.qihang.service.transfer;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qihang.annotation.TenantIgnore;
import com.qihang.common.util.http.HttpReq;
import com.qihang.common.vo.BaseDataVO;
import com.qihang.common.vo.BaseVO;
import com.qihang.constant.TransferEnum;
import com.qihang.controller.shop.app.vo.ShopVO;
import com.qihang.domain.transfer.LotteryTransferDO;
import com.qihang.domain.transfer.ShopTransferDO;
import com.qihang.mapper.transfer.LotteryTransferMapper;
import com.qihang.mapper.transfer.ShopTransferMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
}
