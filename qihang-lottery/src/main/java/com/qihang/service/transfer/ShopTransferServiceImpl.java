package com.qihang.service.transfer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.MD5;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qihang.annotation.TenantIgnore;
import com.qihang.common.vo.BaseDataVO;
import com.qihang.common.vo.BaseVO;
import com.qihang.constant.TransferEnum;
import com.qihang.controller.transferIn.admin.dto.AdminPlatDTO;
import com.qihang.controller.transferIn.admin.dto.AdminShopTransferInDTO;
import com.qihang.controller.transferIn.admin.vo.AdminShopTransferInVO;
import com.qihang.controller.user.admin.dto.UserAddDTO;
import com.qihang.domain.ballgame.BallGameDO;
import com.qihang.domain.transfer.LotteryTransferDO;
import com.qihang.domain.transfer.ShopTransferDO;
import com.qihang.domain.user.UserDO;
import com.qihang.mapper.ballgame.BallGameMapper;
import com.qihang.mapper.transfer.ShopTransferMapper;
import com.qihang.mapper.user.UserMapper;
import com.qihang.service.user.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ShopTransferServiceImpl extends ServiceImpl<ShopTransferMapper, ShopTransferDO> implements IShopTransferService {
    @Resource
    private ShopTransferMapper shopTransferMapper;

    @Resource
    ILotteryTransferService lotteryTransferService;

    @Resource
    private BallGameMapper ballGameMapper;

    @Value("${config.domain}")
    String configDomain;


    @Resource
    UserMapper userMapper;

    @Resource
    IUserService userService;


    @TenantIgnore
    @Override
    public BaseVO editShopTransfer(AdminShopTransferInDTO vo) {
        if (vo.getId() == null) {
            QueryWrapper<ShopTransferDO> queryWrapper = new QueryWrapper<ShopTransferDO>();
            queryWrapper.lambda().eq(ShopTransferDO::getTransferType, TransferEnum.TransferIn.code);
            queryWrapper.and(item -> (item.lambda().eq(ShopTransferDO::getShopName, vo.getShopName())).or().eq(ShopTransferDO::getShopConcatPhone, vo.getShopConcatPhone()));
            List<ShopTransferDO> shopTransferDOS = shopTransferMapper.selectList(queryWrapper);
            if (!CollectionUtils.isEmpty(shopTransferDOS)) {
                BaseVO baseVO = new BaseVO();
                baseVO.setSuccess(false);
                baseVO.setErrorMsg("店名/手机号已经存在");
                return baseVO;
            }
            //查询手机号对应的会员账户是否存在
            UserDO userDO = userMapper.selectOne(new QueryWrapper<UserDO>().lambda().eq(UserDO::getPhone, vo.getShopConcatPhone()));
            if (null != userDO) {
                BaseVO baseVO = new BaseVO();
                baseVO.setSuccess(false);
                baseVO.setErrorMsg("手机号已经存在,请确认此手机号用于会员账户");
                return baseVO;
            }
            UserAddDTO userAddDTO = new UserAddDTO();
            userAddDTO.setPassword(MD5.create().digestHex("123ABCabc"));
            userAddDTO.setPhone(vo.getShopConcatPhone());
            BaseDataVO returnBo = (BaseDataVO) userService.addUser(userAddDTO);
            if (!returnBo.getSuccess()) {
                return returnBo;
            }
            ShopTransferDO shopTransferDO = new ShopTransferDO();
            BeanUtil.copyProperties(vo, shopTransferDO);
            shopTransferDO.setCreateTime(new Date());
            shopTransferDO.setUpdateTime(new Date());
            shopTransferDO.setUid(Long.valueOf(((Map<String, String>) returnBo.getData()).get("id")));

            if (StringUtils.isBlank(vo.getTransferKey())) {
                shopTransferDO.setTransferKey(RandomStringUtils.randomAlphanumeric(5));
            }
            if (StringUtils.isBlank(vo.getTransferSecurty())) {
                shopTransferDO.setTransferSecurty(RandomStringUtils.randomAlphanumeric(32));
            }
            if (StringUtils.isBlank(vo.getTransferInterface())) {
                shopTransferDO.setTransferInterface(configDomain + "/app/transfer/create");
            }
            shopTransferDO.setTransferType(TransferEnum.TransferIn.code);
            shopTransferMapper.insert(shopTransferDO);
            TransferServiceImpl.SHOP_TRANSFER_MAP.put(shopTransferDO.getTransferKey(), shopTransferDO);
            return new BaseVO();

        } else {
            ShopTransferDO transferDO = shopTransferMapper.selectById(vo.getId());
            if (ObjectUtil.isNotNull(transferDO)) {
                boolean update = false;
                if (StringUtils.isNotBlank(vo.getTransferInterface())) {
                    transferDO.setTransferInterface(vo.getTransferInterface());
                    update = true;
                }
                if (StringUtils.isNotBlank(vo.getTransferSecurty())) {
                    transferDO.setTransferSecurty(vo.getTransferSecurty());
                    update = true;
                }
                if (StringUtils.isNotBlank(vo.getShopConcatName())) {
                    transferDO.setShopConcatName(vo.getShopConcatName());
                    update = true;
                }
                if (StringUtils.isNotBlank(vo.getShopConcatPhone())) {
                    transferDO.setShopConcatPhone(vo.getShopConcatPhone());
                    update = true;
                }
                if (StringUtils.isNotBlank(vo.getInterfaceState())) {
                    transferDO.setInterfaceState(vo.getInterfaceState());
                    update = true;
                }

                if (update) {
                    TransferServiceImpl.SHOP_TRANSFER_MAP.put(transferDO.getTransferKey(), transferDO);
                    shopTransferMapper.updateById(transferDO);
                }
            }
        }
        return new BaseVO();
    }

    @TenantIgnore
    @Override
    public BaseVO listShopTransfer(TransferEnum transferEnum) {
        List<ShopTransferDO> shopTransferDOList = shopTransferMapper.selectList(new QueryWrapper<ShopTransferDO>().lambda().eq(ShopTransferDO::getTransferType, transferEnum.code));
        if (CollectionUtils.isEmpty(shopTransferDOList)) {
            return BaseDataVO.builder().data(Collections.EMPTY_LIST).build();
        } else {
            List<AdminPlatDTO> list = BeanUtil.copyToList(shopTransferDOList, AdminPlatDTO.class);
            for (AdminPlatDTO adminPlatDTO : list) {
                adminPlatDTO.setGateinfo(configDomain + "/app/transfer/info/" + adminPlatDTO.getTransferKey() + "/" + adminPlatDTO.getTransferSecurty());
            }
            return BaseDataVO.builder().data(list).build();
        }
    }

    @TenantIgnore
    @Override
    public BaseVO listLotterTransfer(TransferEnum transferEnum) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("transfer", "0");
        List<LotteryTransferDO> lotteryTransferDOList = lotteryTransferService.list(new QueryWrapper<LotteryTransferDO>().lambda().eq(LotteryTransferDO::getTransferFlag, transferEnum.code).orderByDesc(LotteryTransferDO::getCreateTime));
        if (CollectionUtils.isEmpty(lotteryTransferDOList)) {
            dataMap.put("lotterys", Collections.EMPTY_LIST);
        } else {
            dataMap.put("transfer", "1");
            List<AdminShopTransferInVO> list = BeanUtil.copyToList(lotteryTransferDOList, AdminShopTransferInVO.class);
            List<BallGameDO> ballGameDOS = ballGameMapper.selectList(new QueryWrapper<BallGameDO>().lambda().eq(BallGameDO::getTenantId, 1));
            Map<Integer, BallGameDO> ballGameDOMap = ballGameDOS.stream().collect(Collectors.toMap(BallGameDO::getLotid, a -> a, (b, c) -> b));
            for (AdminShopTransferInVO transfer : list) {
                BallGameDO gameDO = ballGameDOMap.get(transfer.getLotteryType());
                if (null != gameDO) {
                    transfer.setIcon(gameDO.getUrl());
                    transfer.setLotteryName(gameDO.getName());
                }
            }
            dataMap.put("lotterys", list);
        }
        return BaseDataVO.builder().data(dataMap).build();
    }

    @Override
    public BaseVO listOutShopTransfer(Integer shopId) {
        return null;
    }

    @TenantIgnore
    @Override
    public ShopTransferDO findShopTransfer(String key) {
        ShopTransferDO shopTransferDO = TransferServiceImpl.SHOP_TRANSFER_MAP.get(key);
        if (null == shopTransferDO) {
            shopTransferDO = getOne(new QueryWrapper<ShopTransferDO>().lambda().eq(ShopTransferDO::getTransferKey, key).eq(ShopTransferDO::getTransferType, TransferEnum.TransferIn.code));
            TransferServiceImpl.SHOP_TRANSFER_MAP.put(key, shopTransferDO);
            return shopTransferDO;
        }
        return shopTransferDO;
    }


}
