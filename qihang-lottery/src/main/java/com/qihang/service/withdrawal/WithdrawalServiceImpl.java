package com.qihang.service.withdrawal;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qihang.common.util.order.OrderNumberGenerationUtil;
import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.controller.withdrawal.admin.dto.ExamineDTO;
import com.qihang.controller.withdrawal.admin.dto.WithdrawalQueryDTO;
import com.qihang.controller.withdrawal.admin.vo.WithdrawalQueryVO;
import com.qihang.controller.withdrawal.app.vo.RecordVO;
import com.qihang.controller.withdrawal.app.dto.WithdrawalDTO;
import com.qihang.domain.order.PayOrderDO;
import com.qihang.domain.user.UserDO;
import com.qihang.domain.withdrawal.WithdrawalDO;
import com.qihang.enumeration.error.ErrorCodeEnum;
import com.qihang.enumeration.order.pay.PayOrderStateEnum;
import com.qihang.enumeration.order.pay.PayOrderTypeEnum;
import com.qihang.enumeration.order.pay.PayTypeEnum;
import com.qihang.enumeration.withdrawal.ExamineEnum;
import com.qihang.mapper.order.PayOrderMapper;
import com.qihang.mapper.user.UserMapper;
import com.qihang.mapper.withdrawal.WithdrawalMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author bright
 * @since 2022-10-08
 */
@Service
public class WithdrawalServiceImpl implements IWithdrawalService {

    @Resource
    private WithdrawalMapper withdrawalMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private PayOrderMapper payOrderMapper;


    @Override
    public BaseVO add(WithdrawalDTO withdrawalDTO, Integer userId) {
        int hour = DateUtil.hour(new Date(), true);
        if (!(hour >= 10 && hour <= 17)) {
            return new BaseVO(false, ErrorCodeEnum.E091.getKey(), ErrorCodeEnum.E091.getValue());
        }
        if (withdrawalDTO.getAmount().compareTo(BigDecimal.valueOf(10)) == -1) {
            return new BaseVO(false, ErrorCodeEnum.E093.getKey(), ErrorCodeEnum.E093.getValue());
        }
        UserDO user = userMapper.selectById(userId);
        if (withdrawalDTO.getAmount().compareTo(user.getPrice()) == 1) {
            return new BaseVO(false, ErrorCodeEnum.E0762.getKey(), ErrorCodeEnum.E0762.getValue());
        }

        WithdrawalDO withdrawal = new WithdrawalDO();
        BeanUtils.copyProperties(withdrawalDTO, withdrawal);
        withdrawal.setUserId(userId);
        withdrawal.setCreateTime(new Date());
        withdrawal.setUpdateTime(new Date());
        withdrawalMapper.insert(withdrawal);
        user.setPrice(user.getPrice().subtract(withdrawalDTO.getAmount()));
        userMapper.updateById(user);
        return new BaseVO();
    }

    @Override
    public BaseVO examine(ExamineDTO examineDTO) {
        WithdrawalDO withdrawal = new WithdrawalDO();
        BeanUtils.copyProperties(examineDTO, withdrawal);
        withdrawalMapper.updateById(withdrawal);
        //如果是拒绝的将金额返回给用户
        WithdrawalDO withdrawalDO = withdrawalMapper.selectById(examineDTO.getId());
        if (ObjectUtil.equal(examineDTO.getState(), ExamineEnum.REFUSE.getKey())) {
            UserDO user = userMapper.selectById(withdrawalDO.getUserId());
            user.setPrice(user.getPrice().add(withdrawalDO.getAmount()));
            userMapper.updateById(user);
            return new BaseVO();
        }
        //添加钱包消费记录
        PayOrderDO payOrder = new PayOrderDO();
        payOrder.setType(PayOrderTypeEnum.PAYMENT.getKey());
        payOrder.setOrderId(OrderNumberGenerationUtil.getOrderId());
        payOrder.setState(PayOrderStateEnum.PAYMENT.getKey());
        payOrder.setCreateTime(new Date());
        payOrder.setUpdateTime(new Date());
        payOrder.setPayType(PayTypeEnum.APP.getKey());
        payOrder.setUserId(withdrawalDO.getUserId());
        payOrder.setPrice(withdrawalDO.getAmount());
        payOrderMapper.insert(payOrder);
        return new BaseVO();
    }

    @Override
    public CommonListVO<RecordVO> list(Integer userId) {
        CommonListVO<RecordVO> commonList = new CommonListVO<>();
        List<WithdrawalDO> withdrawalList = withdrawalMapper.selectList(new QueryWrapper<WithdrawalDO>().lambda().eq(WithdrawalDO::getUserId, userId));
        List<RecordVO> list = BeanUtil.copyToList(withdrawalList, RecordVO.class);
        commonList.setVoList(list);
        return commonList;
    }

    @Override
    public CommonListVO<WithdrawalQueryVO> pageList(WithdrawalQueryDTO withdrawalQuery) {
        CommonListVO<WithdrawalQueryVO> commonList = new CommonListVO<>();
        Integer userId = null;
        UserDO user = userMapper.selectOne(new QueryWrapper<UserDO>().lambda().eq(UserDO::getPhone, withdrawalQuery.getPhone()));
        if (ObjectUtil.isNotNull(user)) {
            userId = user.getId();
        } else {
            if (StrUtil.isNotBlank(withdrawalQuery.getPhone())) {
                return commonList;
            }
        }
        //分页
        Page<WithdrawalDO> page = new Page<>(withdrawalQuery.getPageNo(), withdrawalQuery.getPageSize());
        LambdaQueryWrapper<WithdrawalDO> qw = new QueryWrapper<WithdrawalDO>().lambda();
        //动态拼接查询条件
        qw.eq(ObjectUtil.isNotNull(userId), WithdrawalDO::getUserId, userId);
        qw.eq(StrUtil.isNotBlank(withdrawalQuery.getType()), WithdrawalDO::getType, withdrawalQuery.getType());
        qw.eq(StrUtil.isNotBlank(withdrawalQuery.getState()), WithdrawalDO::getState, withdrawalQuery.getState());
        qw.orderByDesc(WithdrawalDO::getCreateTime, WithdrawalDO::getState);
        Page<WithdrawalDO> withdrawalPage = withdrawalMapper.selectPage(page, qw);
        List<WithdrawalQueryVO> withdrawalQueryList = new ArrayList<>();
        withdrawalPage.getRecords().forEach(withdrawal -> {
            WithdrawalQueryVO withdrawalQueryVO = new WithdrawalQueryVO();
            BeanUtils.copyProperties(withdrawal, withdrawalQueryVO);
            //查找用户
            UserDO userDO = userMapper.selectById(withdrawal.getUserId());
            withdrawalQueryVO.setNickname(userDO.getNickname());
            withdrawalQueryVO.setName(userDO.getName());
            withdrawalQueryVO.setIdCard(userDO.getIdCard());
            withdrawalQueryVO.setPhone(userDO.getPhone());
            withdrawalQueryVO.setBankName(userDO.getBankName());
            withdrawalQueryVO.setBankNumber(userDO.getBankNumber());
            withdrawalQueryVO.setZfbNumber(userDO.getZfbNumber());
            withdrawalQueryList.add(withdrawalQueryVO);
        });
        commonList.setVoList(withdrawalQueryList);
        commonList.setTotal(withdrawalPage.getTotal());
        return commonList;
    }
}
