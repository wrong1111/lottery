package com.qihang.service.dict;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qihang.annotation.TenantIgnore;
import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.constant.Constant;
import com.qihang.controller.dict.dto.SysDictDTO;
import com.qihang.controller.dict.vo.SysDictQueryVO;
import com.qihang.controller.sys.dto.VerifyPayPwdDTO;
import com.qihang.domain.dict.SysDictDO;
import com.qihang.mapper.dict.SysDictMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author bright
 * @since 2022-11-14
 */
@Service
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDictDO> implements ISysDictService {

    @Resource
    private SysDictMapper sysDictMapper;

    @Override
    @TenantIgnore
    @Transactional(rollbackFor = Exception.class)
    public BaseVO insert(SysDictDTO sysDict) {

        sysDictMapper.delete(new QueryWrapper<SysDictDO>().lambda().eq(SysDictDO::getTenantId, sysDict.getTenantId()));

        SysDictDO sysDictDO = new SysDictDO();
        sysDictDO.setCode(Constant.ALIPAY_APPID_KAY);
        sysDictDO.setValue(sysDict.getAliPayAppId());
        sysDictDO.setTenantId(sysDict.getTenantId());
        sysDictDO.setDictDesc("支付宝appid");
        sysDictDO.setCreateTime(new Date());
        sysDictDO.setUpdateTime(new Date());
        sysDictMapper.insert(sysDictDO);

        sysDictDO = new SysDictDO();
        sysDictDO.setCode(Constant.ALIPAY_PUBLIC_KAY);
        sysDictDO.setValue(sysDict.getAlipayPublicKey());
        sysDictDO.setTenantId(sysDict.getTenantId());
        sysDictDO.setDictDesc("支付宝公钥");
        sysDictDO.setCreateTime(new Date());
        sysDictDO.setUpdateTime(new Date());
        sysDictMapper.insert(sysDictDO);

        sysDictDO = new SysDictDO();
        sysDictDO.setCode(Constant.ALIPAY_PRIVATE_KAY);
        sysDictDO.setValue(sysDict.getAlipayPrivateKey());
        sysDictDO.setTenantId(sysDict.getTenantId());
        sysDictDO.setDictDesc("支付宝私钥");
        sysDictDO.setCreateTime(new Date());
        sysDictDO.setUpdateTime(new Date());
        sysDictMapper.insert(sysDictDO);
        return new BaseVO();
    }

    @Override
    @TenantIgnore
    public CommonListVO<SysDictQueryVO> queryDictByTenantId(Integer tenantId) {
        CommonListVO<SysDictQueryVO> commonList = new CommonListVO<>();
        List<SysDictDO> dictList = sysDictMapper.selectList(new QueryWrapper<SysDictDO>().lambda().eq(SysDictDO::getTenantId, tenantId));
        List<SysDictQueryVO> list = BeanUtil.copyToList(dictList, SysDictQueryVO.class);
        commonList.setVoList(list);
        return commonList;
    }

}
