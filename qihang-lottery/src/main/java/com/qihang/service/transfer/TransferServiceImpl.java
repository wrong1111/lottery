package com.qihang.service.transfer;


import cn.hutool.crypto.digest.MD5;
import com.amazonaws.util.Md5Utils;
import com.qihang.common.vo.BaseVO;
import com.qihang.controller.transferOut.app.dto.TransferDTO;
import com.qihang.domain.transfer.ShopTransferDO;
import com.qihang.domain.user.UserDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TransferServiceImpl implements ITransferService {

    public static Map<String, UserDO> RECEVE_INFO_MAP = new HashMap<>();

    public static Map<String, ShopTransferDO> SHOP_TRANSFER_MAP = new HashMap<>();

    @Resource
    IShopTransferService shopTransferService;


    @Override
    public BaseVO validate(TransferDTO dto) {
        String key = dto.getKey();
        String data = dto.getData();
        String action = dto.getAction();
        String sign = dto.getSign();

        BaseVO baseVO = new BaseVO();
        ShopTransferDO shopTransferDO = shopTransferService.findShopTransfer(key);
        if (null == shopTransferDO) {
            baseVO.setSuccess(false);
            baseVO.setErrorMsg("未开通，请联系商家");
            return baseVO;
        }
        if ("1".equals(shopTransferDO.getTransferInterface())) {
            baseVO.setSuccess(false);
            baseVO.setErrorMsg("暂停收单，请联系商家");
            return baseVO;
        }
        String signString = action + key + (StringUtils.isNotBlank(data) ? data : "") + shopTransferDO.getTransferSecurty();
        MD5 md5 = null;
        try {
            md5 = new MD5(shopTransferDO.getTransferSecurty().getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        if (!sign.equals(md5.digestHex(signString))) {
            baseVO.setSuccess(false);
            baseVO.setErrorMsg("签名错误");
            return baseVO;
        }
        return baseVO;
    }
}
