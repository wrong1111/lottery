package com.qihang.service.user;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qihang.annotation.TenantIgnore;
import com.qihang.common.util.area.AreaUtil;
import com.qihang.common.util.code.QrCodeUtil;
import com.qihang.common.util.jwt.JWTUtil;
import com.qihang.common.util.log.LogUtil;
import com.qihang.common.util.nickname.NameUtil;
import com.qihang.common.util.order.OrderNumberGenerationUtil;
import com.qihang.common.util.security.BcryptPasswordUtil;
import com.qihang.common.util.security.SecurityFrameworkUtils;
import com.qihang.common.util.sms.SmsUtil;
import com.qihang.common.util.upload.S3Util;
import com.qihang.common.vo.BaseDataVO;
import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.constant.Constant;
import com.qihang.controller.user.admin.dto.*;
import com.qihang.controller.user.admin.vo.AdminStatisticsVO;
import com.qihang.controller.user.admin.vo.UserQueryVO;
import com.qihang.controller.user.admin.vo.UserStatisticsVO;
import com.qihang.controller.user.app.dto.*;
import com.qihang.controller.user.app.vo.AgentVO;
import com.qihang.controller.user.app.vo.UserByNickNameDTO;
import com.qihang.controller.user.app.vo.UserTokenVO;
import com.qihang.controller.user.app.vo.UserVO;
import com.qihang.domain.documentary.DocumentaryDO;
import com.qihang.domain.documentary.DocumentaryUserDO;
import com.qihang.domain.follow.FollowDO;
import com.qihang.domain.log.LogDO;
import com.qihang.domain.order.LotteryOrderDO;
import com.qihang.domain.order.PayOrderDO;
import com.qihang.domain.permutation.PermutationDO;
import com.qihang.domain.racingball.RacingBallDO;
import com.qihang.domain.realm.SysDomainDO;
import com.qihang.domain.user.UserDO;
import com.qihang.domain.withdrawal.WithdrawalDO;
import com.qihang.enumeration.error.ErrorCodeEnum;
import com.qihang.enumeration.order.lottery.LotteryOrderStateEnum;
import com.qihang.enumeration.order.pay.PayOrderStateEnum;
import com.qihang.enumeration.order.pay.PayOrderTypeEnum;
import com.qihang.enumeration.order.pay.PayTypeEnum;
import com.qihang.enumeration.user.SexEnum;
import com.qihang.enumeration.withdrawal.ExamineEnum;
import com.qihang.mapper.documentary.DocumentaryMapper;
import com.qihang.mapper.documentary.DocumentaryUserMapper;
import com.qihang.mapper.follow.FollowMapper;
import com.qihang.mapper.log.LogMapper;
import com.qihang.mapper.order.LotteryOrderMapper;
import com.qihang.mapper.order.PayOrderMapper;
import com.qihang.mapper.permutation.PermutationMapper;
import com.qihang.mapper.racingball.RacingBallMapper;
import com.qihang.mapper.realm.SysDomainMapper;
import com.qihang.mapper.user.UserMapper;
import com.qihang.mapper.withdrawal.WithdrawalMapper;
import com.qihang.service.upload.IUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author: bright
 * @description:
 * @time: 2022-06-28 10:09
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements IUserService {

    @Resource
    private SmsUtil smsUtil;
    @Resource
    private UserMapper userMapper;

    @Resource
    private S3Util s3Util;

    @Resource
    private LotteryOrderMapper lotteryOrderMapper;

    @Resource
    private DocumentaryUserMapper documentaryUserMapper;

    @Resource
    private FollowMapper followMapper;

    @Resource
    private LogMapper logMapper;

    @Resource
    private PayOrderMapper payOrderMapper;

    @Resource
    private RacingBallMapper racingBallMapper;


    @Resource
    private WithdrawalMapper withdrawalMapper;

    @Resource
    private PermutationMapper permutationMapper;

    @Resource
    private LogUtil logUtil;

    @Resource
    private DocumentaryMapper documentaryMapper;


    @Resource
    private SysDomainMapper sysDomainMapper;

    /**
     * 延迟加载，因为存在相互依赖的问题
     */
    @Lazy
    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private RedisTemplate redisTemplate;


    @Resource
    private AreaUtil areaUtil;


    @Override
    public BaseVO login(UserLoginDTO userLoginDTO) {
        Authentication authentication;
        try {
            // 调用 Spring Security 的 AuthenticationManager#authenticate(...) 方法，使用账号密码进行认证
            // 在其内部，会调用到 loadUserByUsername 方法，获取 User 信息
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userLoginDTO.getPhone(), userLoginDTO.getPassword()));
        } catch (BadCredentialsException badCredentialsException) {
            return new BaseVO(false, ErrorCodeEnum.E0751.getKey(), ErrorCodeEnum.E0751.getValue());
        } catch (DisabledException disabledException) {
            return new BaseVO(false, ErrorCodeEnum.E0757.getKey(), ErrorCodeEnum.E0757.getValue());
        } catch (UsernameNotFoundException usernameNotFoundException) {
            return new BaseVO(false, ErrorCodeEnum.E0756.getKey(), ErrorCodeEnum.E0756.getValue());
        } catch (AuthenticationException authenticationException) {
            return new BaseVO(false, ErrorCodeEnum.E0759.getKey(), ErrorCodeEnum.E0759.getValue());
        }
        UserDO user = (UserDO) authentication.getPrincipal();
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        //生成令牌
        String token = JWTUtil.createToken(user.getId(), DigestUtil.md5Hex(user.getPassword()));
        String refreshToken = JWTUtil.createRefreshToken(user.getId(), DigestUtil.md5Hex(user.getPassword()));
        //写入到vo
        UserTokenVO userTokenVO = new UserTokenVO(token, refreshToken, userVO);
        //更新登录ip，地区 时间
        user.setLastLoginTime(new Date());
        String ip = areaUtil.getIp();
        user.setIp(ip);
        user.setArea(areaUtil.getAreaByIp(ip));
        userMapper.updateById(user);
        logUtil.record("APP登录", userLoginDTO.getPhone());
        return userTokenVO;
    }

    @Override
    public BaseVO refresh(UserRefreshDTO userRefreshDTO) {
        Map<String, Object> map = JWTUtil.decodeToken(userRefreshDTO.getToken(), Constant.REFRESH_SECRET);
        Integer userId = Integer.valueOf(map.get("userId").toString());
        String pwd = map.get("pwd").toString();
        //生成令牌
        String token = JWTUtil.createToken(userId, pwd);
        String refreshToken = JWTUtil.createRefreshToken(userId, pwd);
        UserTokenVO userTokenVO = new UserTokenVO();
        userTokenVO.setToken(token);
        userTokenVO.setRefreshToken(refreshToken);
        return userTokenVO;
    }

    @Override
    public BaseVO send(PhoneDTO phoneDTO, String ip) {
        //根据ip防止短信被刷 可以结合图形验证码一起使用
        Object obj = redisTemplate.opsForValue().get(Constant.IP_KEY + "_" + ip);
        if (ObjectUtil.isNotEmpty(obj)) {
            Integer frequency = Integer.valueOf(obj.toString());
            if (ObjectUtil.equals(frequency, Constant.FREQUENCY)) {
                return new BaseVO(false, ErrorCodeEnum.E0761.getKey(), ErrorCodeEnum.E0761.getValue());
            }
        }
        int code = (int) ((Math.random() * 9 + 1) * 100000);
        //腾讯云发送短信
        smsUtil.send(phoneDTO.getPhone(), String.valueOf(code));
        redisTemplate.opsForValue().set(Constant.REDIS_CODE + "-" + phoneDTO.getPhone(), code, 3, TimeUnit.MINUTES);
        //根据ip进行计数
        redisTemplate.opsForValue().increment(Constant.IP_KEY + "_" + ip);
        redisTemplate.expire(Constant.IP_KEY + "_" + ip, 1, TimeUnit.MINUTES);
        return new BaseVO();
    }

    @Override
    public BaseVO register(RegisterDTO registerDTO) {
        String code = String.valueOf(redisTemplate.opsForValue().get(Constant.REDIS_CODE + "-" + registerDTO.getPhone()));
        //验证验证码
        if (null == code || !registerDTO.getCode().equals(code)) {
            return new BaseVO(false, ErrorCodeEnum.E0760.getKey(), ErrorCodeEnum.E0760.getValue());
        }
        //九位数的uid
        int uid = (int) (Math.random() * (400000000 - 100000000)) + 100000000;
        //如果是邀请进来的查询下pid
        Integer userId = null;
        if (ObjectUtil.isNotNull(registerDTO.getUid())) {
            UserDO userDO = userMapper.selectOne(new QueryWrapper<UserDO>().lambda().eq(UserDO::getUid, registerDTO.getUid()));
            userId = userDO.getId();
        }
        UserDO userDO = new UserDO();
        //如果是选择的店铺，或者默认  1 前端的uid是 tenant_id
        //会员主键
        userDO.setUid(uid);
        //设置上级pid
        userDO.setPid(StrUtil.isBlank(registerDTO.getWhether()) ? userId : null);
        userDO.setPhone(registerDTO.getPhone());
        userDO.setPassword(BcryptPasswordUtil.createBCryptPassword(registerDTO.getPassword()));
        userDO.setSex(SexEnum.UNKNOWN.getKey());
        userDO.setNickname(NameUtil.getName());
        userDO.setGold(new BigDecimal(0));
        userDO.setPrice(new BigDecimal(0));
        userDO.setCreateTime(new Date());
        userDO.setUpdateTime(new Date());
        SysDomainDO sysDomain = sysDomainMapper.selectOne(null);
        //wyong edit    推广二维码需要 生成出来。
//        InputStream is = QrCodeUtil.generate(sysDomain.getAppUrl() + "/#/pages/user/register?uid=" + uid);
//        String codeUrl = s3Util.upload(is);
        userDO.setQrCode("");
        userDO.setAvatar("https://ppm-pics-resource.s3.us-east-1.amazonaws.com/cms/QQ截图20230324020824.png");
        userMapper.insert(userDO);
        UserTokenVO userTokenVO = new UserTokenVO();
        userTokenVO.setToken(JWTUtil.createToken(userDO.getId(), DigestUtil.md5Hex(userDO.getPassword())));
        userTokenVO.setRefreshToken(JWTUtil.createRefreshToken(userDO.getId(), DigestUtil.md5Hex(userDO.getPassword())));
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userDO, userVO);
        userTokenVO.setUser(userVO);
        logUtil.record("注册", registerDTO.getPhone());
        return userTokenVO;
    }


    @Override
    public BaseVO changePwd(ChangePwdDTO changePwdDTO) {
        String code = String.valueOf(redisTemplate.opsForValue().get(Constant.REDIS_CODE + "-" + changePwdDTO.getPhone()));
        //验证验证码
        if (null == code || !changePwdDTO.getCode().equals(code)) {
            return new BaseVO(false, ErrorCodeEnum.E0760.getKey(), ErrorCodeEnum.E0760.getValue());
        }
        UserDO user = new UserDO();
        user.setPassword(BcryptPasswordUtil.createBCryptPassword(changePwdDTO.getPassword()));
        userMapper.update(user, new QueryWrapper<UserDO>().lambda().eq(UserDO::getPhone, changePwdDTO.getPhone()));
        return new BaseVO();
    }

    @Override
    public BaseVO changeUser(ChangeUserDTO changeUserDTO) {
        UserDO user = new UserDO();
        BeanUtils.copyProperties(changeUserDTO, user);
        Integer loginUserId = SecurityFrameworkUtils.getLoginUserId();
        user.setId(loginUserId);
        userMapper.updateById(user);
        return new BaseVO();
    }

    @Override
    public BaseVO real(RealDTO realDTO, Integer userId) {
        UserDO user = new UserDO();
        user.setId(userId);
        user.setIsReal("1");
        BeanUtils.copyProperties(realDTO, user);
        userMapper.updateById(user);
        return new BaseVO();
    }

    @Override
    public UserVO getUser(Integer userId) {
        UserVO userVO = new UserVO();
        UserDO user = userMapper.selectById(userId);
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public CommonListVO<UserQueryVO> pageList(UserQueryDTO userQueryDTO) {
        CommonListVO<UserQueryVO> commonList = new CommonListVO<>();
        //分页
        Page<UserDO> page = new Page<>(userQueryDTO.getPageNo(), userQueryDTO.getPageSize());

        LambdaQueryWrapper<UserDO> qw = new QueryWrapper<UserDO>().lambda();
        //动态拼接查询条件
        qw.like(StrUtil.isNotBlank(userQueryDTO.getNickname()), UserDO::getNickname, userQueryDTO.getNickname());
        qw.eq(StrUtil.isNotBlank(userQueryDTO.getPhone()), UserDO::getPhone, userQueryDTO.getPhone());
        qw.orderByDesc(UserDO::getCreateTime);
        Page<UserDO> userPage = userMapper.selectPage(page, qw);
        List<UserDO> records = userPage.getRecords();
        List<UserQueryVO> list = new ArrayList<>();
        for (UserDO user : records) {
            UserQueryVO userQuery = new UserQueryVO();
            BeanUtils.copyProperties(user, userQuery);
            if (ObjectUtil.isNotNull(user.getPid())) {
                UserDO userDO = new UserDO();
                userDO.setId(user.getPid());
                userQuery.setParentName(userMapper.selectById(userDO).getNickname());
            }
            list.add(userQuery);
        }
        commonList.setVoList(list);
        commonList.setTotal(userPage.getTotal());
        return commonList;
    }

    @Override
    public CommonListVO<AgentVO> agent(UserAgentDTO userAgent, Integer userId) {
        CommonListVO<AgentVO> commonList = new CommonListVO<>();
        LambdaQueryWrapper<UserDO> qw = new QueryWrapper<UserDO>().lambda();
        //动态拼接查询条件
        if (Validator.isMobile(userAgent.getParam())) {
            qw.eq(StrUtil.isNotBlank(userAgent.getParam()), UserDO::getPhone, userAgent.getParam());
        } else {
            qw.like(StrUtil.isNotBlank(userAgent.getParam()), UserDO::getNickname, userAgent.getParam());
        }
        qw.eq(UserDO::getPid, userId);
        qw.orderByDesc(UserDO::getCreateTime);
        List<UserDO> records = userMapper.selectList(qw);
        List<AgentVO> agentUserList = new ArrayList<>();
        for (UserDO user : records) {
            AgentVO agentUser = new AgentVO();
            BeanUtils.copyProperties(user, agentUser);
            Date todayStartTime = DateUtil.parse(DateUtil.today() + " 00:00:00");
            Date todayEndTime = DateUtil.parse(DateUtil.today() + " 23:59:59");

            Date startTime = null;
            Date endTime = null;

            if (ObjectUtil.isNull(userAgent.getStartTime())) {
                startTime = DateUtil.parse(DateUtil.today() + " 00:00:00");
            } else {
                startTime = DateUtil.parse(new SimpleDateFormat("yyyy-MM-dd").format(userAgent.getStartTime()) + " 00:00:00");
            }
            if (ObjectUtil.isNull(userAgent.getEndTime())) {
                endTime = DateUtil.parse(DateUtil.today() + " 23:59:59");
            } else {
                endTime = DateUtil.parse(new SimpleDateFormat("yyyy-MM-dd").format(userAgent.getEndTime()) + " 23:59:59");
            }
            //统计用户投注金额
            QueryWrapper<LotteryOrderDO> orderQueryWrapper = new QueryWrapper<>();
            orderQueryWrapper.select("sum(price) as totalPrice");
            orderQueryWrapper.lambda().ge(LotteryOrderDO::getCreateTime, startTime);
            orderQueryWrapper.lambda().le(LotteryOrderDO::getCreateTime, endTime);
            orderQueryWrapper.lambda().eq(LotteryOrderDO::getUserId, user.getId());
            orderQueryWrapper.lambda().ne(LotteryOrderDO::getState, LotteryOrderStateEnum.REFUSE.getKey());
            orderQueryWrapper.lambda().ne(LotteryOrderDO::getState, LotteryOrderStateEnum.REFUND.getKey());
            List<Object> sumPrice = lotteryOrderMapper.selectObjs(orderQueryWrapper);
            //总投注
            agentUser.setTotalBetting(ObjectUtil.isNotNull(sumPrice.get(0)) ? new BigDecimal(sumPrice.get(0).toString()) : new BigDecimal(0));

            //今日投注
            orderQueryWrapper = new QueryWrapper<>();
            orderQueryWrapper.select("sum(price) as totalPrice");
            orderQueryWrapper.lambda().ge(LotteryOrderDO::getCreateTime, todayStartTime);
            orderQueryWrapper.lambda().le(LotteryOrderDO::getCreateTime, todayEndTime);
            orderQueryWrapper.lambda().eq(LotteryOrderDO::getUserId, user.getId());
            orderQueryWrapper.lambda().ne(LotteryOrderDO::getState, LotteryOrderStateEnum.REFUSE.getKey());
            orderQueryWrapper.lambda().ne(LotteryOrderDO::getState, LotteryOrderStateEnum.REFUND.getKey());
            sumPrice = lotteryOrderMapper.selectObjs(orderQueryWrapper);
            agentUser.setTodayBetting(ObjectUtil.isNotNull(sumPrice.get(0)) ? new BigDecimal(sumPrice.get(0).toString()) : new BigDecimal(0));

            QueryWrapper<PayOrderDO> payOrderQueryWrapper = new QueryWrapper<>();
            payOrderQueryWrapper.select("sum(price) as totalPrice");
            payOrderQueryWrapper.lambda().ge(PayOrderDO::getCreateTime, startTime);
            payOrderQueryWrapper.lambda().le(PayOrderDO::getCreateTime, endTime);
            payOrderQueryWrapper.lambda().eq(PayOrderDO::getUserId, user.getId());
            payOrderQueryWrapper.lambda().eq(PayOrderDO::getType, PayOrderTypeEnum.RECHARGE.getKey());
            payOrderQueryWrapper.lambda().eq(PayOrderDO::getState, PayOrderStateEnum.PAID.getKey());
            //总充值
            sumPrice = payOrderMapper.selectObjs(payOrderQueryWrapper);
            agentUser.setTotalRecharge(ObjectUtil.isNotNull(sumPrice.get(0)) ? new BigDecimal(sumPrice.get(0).toString()) : new BigDecimal(0));
            //今日充值
            payOrderQueryWrapper = new QueryWrapper<>();
            payOrderQueryWrapper.select("sum(price) as totalPrice");
            payOrderQueryWrapper.lambda().ge(PayOrderDO::getCreateTime, todayStartTime);
            payOrderQueryWrapper.lambda().le(PayOrderDO::getCreateTime, todayEndTime);
            payOrderQueryWrapper.lambda().eq(PayOrderDO::getUserId, user.getId());
            payOrderQueryWrapper.lambda().eq(PayOrderDO::getState, PayOrderStateEnum.PAID.getKey());
            payOrderQueryWrapper.lambda().eq(PayOrderDO::getType, PayOrderTypeEnum.RECHARGE.getKey());
            sumPrice = payOrderMapper.selectObjs(payOrderQueryWrapper);
            agentUser.setTodayRecharge(ObjectUtil.isNotNull(sumPrice.get(0)) ? new BigDecimal(sumPrice.get(0).toString()) : new BigDecimal(0));

            QueryWrapper<WithdrawalDO> withdrawalQueryWrapper = new QueryWrapper<>();
            withdrawalQueryWrapper.select("sum(amount) as totalPrice");
            withdrawalQueryWrapper.lambda().ge(WithdrawalDO::getUpdateTime, startTime);
            withdrawalQueryWrapper.lambda().le(WithdrawalDO::getUpdateTime, endTime);
            withdrawalQueryWrapper.lambda().eq(WithdrawalDO::getUserId, user.getId());
            withdrawalQueryWrapper.lambda().eq(WithdrawalDO::getState, ExamineEnum.PAID.getKey());
            //总提现
            sumPrice = withdrawalMapper.selectObjs(withdrawalQueryWrapper);
            agentUser.setTotalWithdrawal(ObjectUtil.isNotNull(sumPrice.get(0)) ? new BigDecimal(sumPrice.get(0).toString()) : new BigDecimal(0));
            //今日提现
            withdrawalQueryWrapper = new QueryWrapper<>();
            withdrawalQueryWrapper.select("sum(amount) as totalPrice");
            withdrawalQueryWrapper.lambda().ge(WithdrawalDO::getUpdateTime, todayStartTime);
            withdrawalQueryWrapper.lambda().le(WithdrawalDO::getUpdateTime, todayEndTime);
            withdrawalQueryWrapper.lambda().eq(WithdrawalDO::getUserId, user.getId());
            withdrawalQueryWrapper.lambda().eq(WithdrawalDO::getState, ExamineEnum.PAID.getKey());
            sumPrice = withdrawalMapper.selectObjs(withdrawalQueryWrapper);
            agentUser.setTodayWithdrawal(ObjectUtil.isNotNull(sumPrice.get(0)) ? new BigDecimal(sumPrice.get(0).toString()) : new BigDecimal(0));

            QueryWrapper<LotteryOrderDO> awardQueryWrapper = new QueryWrapper<>();
            awardQueryWrapper.select("sum(win_price) as totalPrice");
            awardQueryWrapper.lambda().ge(LotteryOrderDO::getUpdateTime, startTime);
            awardQueryWrapper.lambda().le(LotteryOrderDO::getUpdateTime, endTime);
            awardQueryWrapper.lambda().eq(LotteryOrderDO::getUserId, user.getId());
            awardQueryWrapper.lambda().eq(LotteryOrderDO::getState, LotteryOrderStateEnum.ALREADY_AWARD.getKey());
            //总派奖
            sumPrice = lotteryOrderMapper.selectObjs(awardQueryWrapper);
            agentUser.setTotalAward(ObjectUtil.isNotNull(sumPrice.get(0)) ? new BigDecimal(sumPrice.get(0).toString()) : new BigDecimal(0));
            //今日派奖
            awardQueryWrapper = new QueryWrapper<>();
            awardQueryWrapper.select("sum(win_price) as totalPrice");
            awardQueryWrapper.lambda().ge(LotteryOrderDO::getUpdateTime, todayStartTime);
            awardQueryWrapper.lambda().le(LotteryOrderDO::getUpdateTime, todayEndTime);
            awardQueryWrapper.lambda().eq(LotteryOrderDO::getUserId, user.getId());
            awardQueryWrapper.lambda().eq(LotteryOrderDO::getState, LotteryOrderStateEnum.ALREADY_AWARD.getKey());
            sumPrice = lotteryOrderMapper.selectObjs(awardQueryWrapper);
            agentUser.setTodayAward(ObjectUtil.isNotNull(sumPrice.get(0)) ? new BigDecimal(sumPrice.get(0).toString()) : new BigDecimal(0));
            agentUserList.add(agentUser);
        }
        commonList.setVoList(agentUserList);
        return commonList;
    }

    @Override
    @TenantIgnore
    public BaseVO delete(Integer id) {
        userMapper.deleteById(id);
        documentaryUserMapper.delete(new QueryWrapper<DocumentaryUserDO>().lambda().eq(DocumentaryUserDO::getUserId, id));
        documentaryMapper.delete(new QueryWrapper<DocumentaryDO>().lambda().eq(DocumentaryDO::getUserId, id));
        followMapper.delete(new QueryWrapper<FollowDO>().lambda().eq(FollowDO::getUserId, id).or().eq(FollowDO::getTargetId, id));
        logMapper.delete(new QueryWrapper<LogDO>().lambda().eq(LogDO::getUserId, id));
        lotteryOrderMapper.delete(new QueryWrapper<LotteryOrderDO>().lambda().eq(LotteryOrderDO::getUserId, id));
        payOrderMapper.delete(new QueryWrapper<PayOrderDO>().lambda().eq(PayOrderDO::getUserId, id));
        permutationMapper.delete(new QueryWrapper<PermutationDO>().lambda().eq(PermutationDO::getUserId, id));
        racingBallMapper.delete(new QueryWrapper<RacingBallDO>().lambda().eq(RacingBallDO::getUserId, id));
        withdrawalMapper.delete(new QueryWrapper<WithdrawalDO>().lambda().eq(WithdrawalDO::getUserId, id));
        return new BaseVO();
    }

    @Override
    public BaseVO updateStatus(UserUpdateDTO userUpdate, Integer id) {
        UserDO user = new UserDO();
        if (StrUtil.isNotBlank(userUpdate.getStatus())) {
            user.setStatus(userUpdate.getStatus());
        }
        if (ObjectUtil.isNotNull(userUpdate.getIsAgent())) {
            user.setIsAgent(userUpdate.getIsAgent());
        }
        user.setId(id);
        userMapper.updateById(user);
        return new BaseVO();
    }

    @Override
    public BaseVO recharge(UserRechargeDTO userRecharge, Integer id) {
        UserDO user = userMapper.selectById(id);
        //添加订单流水记录
        PayOrderDO payOrder = new PayOrderDO();
        if (StrUtil.equals(userRecharge.getType(), "0")) {
            user.setGold(user.getGold().add(userRecharge.getPrice()));
            payOrder.setType(PayOrderTypeEnum.RECHARGE.getKey());
            payOrder.setState(PayOrderStateEnum.PAID.getKey());
            userMapper.updateById(user);
        } else if (StrUtil.equals(userRecharge.getType(), "1")) {
            //计算用户的账号是否充足
            if (userRecharge.getPrice().compareTo(user.getPrice()) == 1) {
                if (userRecharge.getPrice().compareTo(user.getGold().add(user.getPrice())) == 1) {
                    return new BaseVO(false, ErrorCodeEnum.E0763.getKey(), ErrorCodeEnum.E0763.getValue());
                }
            }
            if (userRecharge.getPrice().compareTo(user.getPrice()) == 1) {
                //直接扣除奖金
                BigDecimal remainingPrice = userRecharge.getPrice().subtract(user.getPrice());
                //奖金直接设置为0
                user.setPrice(new BigDecimal(0));
                //剩下的扣除彩金余额
                user.setGold(user.getGold().subtract(remainingPrice));
                userMapper.updateById(user);
            } else {
                //直接扣除奖金
                user.setPrice(user.getPrice().subtract(userRecharge.getPrice()));
                userMapper.updateById(user);
            }
            payOrder.setType(PayOrderTypeEnum.PAYMENT.getKey());
            payOrder.setState(PayOrderStateEnum.PAYMENT.getKey());
            WithdrawalDO withdrawal = new WithdrawalDO();
            withdrawal.setUserId(id);
            withdrawal.setCreateTime(new Date());
            withdrawal.setUpdateTime(new Date());
            withdrawal.setAmount(userRecharge.getPrice());
            withdrawal.setState(ExamineEnum.PAID.getKey());
            withdrawal.setType(PayTypeEnum.ALIPAY.getKey());
            withdrawalMapper.insert(withdrawal);
        }
        String orderId = OrderNumberGenerationUtil.getOrderId();
        payOrder.setOrderId(orderId);
        payOrder.setCreateTime(new Date());
        payOrder.setPrice(userRecharge.getPrice());
        payOrder.setPayType(PayTypeEnum.APP.getKey());
        payOrder.setUserId(user.getId());
        payOrderMapper.insert(payOrder);
        return new BaseVO();
    }

    @Override
    public BaseVO adminChangePwd(AdminChangePwdDTO adminChangePwd) {
        UserDO user = new UserDO();
        user.setPassword(BcryptPasswordUtil.createBCryptPassword(adminChangePwd.getPassword()));
        userMapper.update(user, new QueryWrapper<UserDO>().lambda().eq(UserDO::getPhone, adminChangePwd.getPhone()));
        return new BaseVO();
    }

    @Override
    public AdminStatisticsVO userStatistics(UserStatisticsDTO userStatistics) {
        AdminStatisticsVO adminStatistics = new AdminStatisticsVO();
        //分页
        Page<UserDO> page = new Page<>(userStatistics.getPageNo(), userStatistics.getPageSize());

        LambdaQueryWrapper<UserDO> qw = new QueryWrapper<UserDO>().lambda();
        //动态拼接查询条件
        qw.eq(StrUtil.isNotBlank(userStatistics.getPid()), UserDO::getPid, userStatistics.getPid());
        qw.orderByDesc(UserDO::getIsAgent, UserDO::getCreateTime);
        Page<UserDO> userPage = userMapper.selectPage(page, qw);
        //如果起始时间和结束时间为空就默认是查当天
        Date startTime = null;
        Date endTime = null;
        if (ObjectUtil.isNull(userStatistics.getStartTime())) {
            startTime = DateUtil.parse(DateUtil.today() + " 00:00:00");
        } else {
            startTime = userStatistics.getStartTime();
        }
        if (ObjectUtil.isNull(userStatistics.getEndTime())) {
            endTime = DateUtil.parse(DateUtil.today() + " 23:59:59");
        } else {
            endTime = userStatistics.getEndTime();
        }
        List<UserDO> records = userPage.getRecords();
        Map<String, Object> map = statistics(records, startTime, endTime);
        adminStatistics.setVoList((List<UserStatisticsVO>) map.get("list"));
        //统计所有用户的总金额
        List<UserDO> userList = null;
        if (StrUtil.isBlank(userStatistics.getPid())) {
            //pid为空就查全部
            userList = userMapper.selectList(null);
        } else {
            //不为空就查出pid下的用户
            userList = userMapper.selectList(new QueryWrapper<UserDO>().lambda().eq(UserDO::getPid, userStatistics.getPid()));
        }
        map = statistics(userList, startTime, endTime);
        adminStatistics.setTotal(userPage.getTotal());
        adminStatistics.setTotalRechargePrice(new BigDecimal(map.get("totalRechargePrice").toString()));
        adminStatistics.setTotalBettingPrice(new BigDecimal(map.get("totalBettingPrice").toString()));
        adminStatistics.setTotalWithdrawalPrice(new BigDecimal(map.get("totalWithdrawalPrice").toString()));
        adminStatistics.setTotalGold(new BigDecimal(map.get("totalGold").toString()));
        adminStatistics.setTotalPrice(new BigDecimal(map.get("totalPrice").toString()));
        return adminStatistics;
    }

    @Override
    @TenantIgnore
    public CommonListVO<UserVO> getUserByNickName(UserByNickNameDTO userByNickName) {
        CommonListVO<UserVO> commonList = new CommonListVO<>();
        List<UserDO> userList = userMapper.selectList(new QueryWrapper<UserDO>().lambda().like(UserDO::getNickname, userByNickName.getNickname()));
        List<UserVO> list = BeanUtil.copyToList(userList, UserVO.class);
        commonList.setVoList(StrUtil.isNotBlank(userByNickName.getNickname()) ? list : new ArrayList<>());
        return commonList;
    }

    @Override
    @TenantIgnore
    public UserVO getTenantId(Integer uid) {
        UserDO userDO = userMapper.selectOne(new QueryWrapper<UserDO>().lambda().eq(UserDO::getUid, uid));
        UserVO user = new UserVO();
        BeanUtils.copyProperties(userDO, user);
        return user;
    }

    @Override
    public BaseVO binding(BindingDTO binding, Integer userId) {
        UserDO user = new UserDO();
        user.setBankName(binding.getBankName());
        user.setBankNumber(binding.getBankNumber());
        user.setZfbNumber(binding.getZfbNumber());
        user.setId(userId);
        userMapper.updateById(user);
        return new BaseVO();
    }

    @Override
    @TenantIgnore
    public BaseVO checkPhone(CheckPhoneDTO checkPhone) {
        UserDO user = userMapper.selectOne(new QueryWrapper<UserDO>().lambda().eq(UserDO::getPhone, checkPhone.getPhone()));
        if (ObjectUtil.isNotNull(user)) {
            user = userMapper.selectOne(new QueryWrapper<UserDO>().lambda().eq(UserDO::getPhone, checkPhone.getPhone()).eq(UserDO::getTenantId, checkPhone.getTenantId()));
            if (ObjectUtil.isNull(user)) {
                return new BaseVO(false, ErrorCodeEnum.E089.getKey(), ErrorCodeEnum.E089.getValue());
            }
        }
        return new BaseVO();
    }

    @Override
    public BaseVO checkPhoneIsExist(PhoneDTO phone) {
        UserDO user = userMapper.selectOne(new QueryWrapper<UserDO>().lambda().eq(UserDO::getPhone, phone.getPhone()));
        if (null != user) {
            return new BaseVO(false, ErrorCodeEnum.E0755.getKey(), ErrorCodeEnum.E0755.getValue());
        }
        return new BaseVO();
    }

    @Resource
    IUploadService uploadService;

    @Override
    public BaseVO addUser(UserAddDTO userAdd) {
        UserDO user = userMapper.selectOne(new QueryWrapper<UserDO>().lambda().eq(UserDO::getPhone, userAdd.getPhone()));
        if (null != user) {
            return new BaseVO(false, ErrorCodeEnum.E0755.getKey(), ErrorCodeEnum.E0755.getValue());
        }
        //九位数的uid
        int uid = (int) (Math.random() * (400000000 - 100000000)) + 100000000;
        UserDO userDO = new UserDO();
        userDO.setUid(uid);
        //设置上级pid
        userDO.setPid(null);
        userDO.setPhone(userAdd.getPhone());
        userDO.setPassword(BcryptPasswordUtil.createBCryptPassword(userAdd.getPassword()));
        userDO.setSex(SexEnum.UNKNOWN.getKey());
        userDO.setNickname(NameUtil.getName());
        userDO.setGold(new BigDecimal(0));
        userDO.setPrice(new BigDecimal(0));
        userDO.setCreateTime(new Date());
        userDO.setUpdateTime(new Date());
        SysDomainDO sysDomain = sysDomainMapper.selectOne(null);
        InputStream is = QrCodeUtil.generate(sysDomain.getAppUrl() + "/#/pages/user/register?uid=" + uid);
        String codeUrl = uploadService.upload(is);
        userDO.setQrCode(codeUrl);
        userDO.setAvatar(sysDomain.getAppUrl() + "/static/default1.jpg");
        userMapper.insert(userDO);
        Map<String, String> userMap = new HashMap<>();
        userMap.put("id", "" + userDO.getId());
        userMap.put("uid", "" + userDO.getUid());
        return BaseDataVO.builder().data(userMap).build();

    }

    private Map<String, Object> statistics(List<UserDO> records, Date startTime, Date endTime) {
        BigDecimal totalWithdrawalPrice = new BigDecimal(0);
        BigDecimal totalRechargePrice = new BigDecimal(0);
        BigDecimal totalBettingPrice = new BigDecimal(0);
        Map<String, Object> map = new HashMap<>(4);
        List<UserStatisticsVO> list = new ArrayList<>();
        BigDecimal totalPrice = new BigDecimal(0);
        BigDecimal totalGold = new BigDecimal(0);
        for (UserDO record : records) {
            UserStatisticsVO us = new UserStatisticsVO();
            BeanUtils.copyProperties(record, us);
            UserDO userDO = userMapper.selectById(record.getPid());
            if (ObjectUtil.isNotNull(userDO)) {
                us.setParentName(userDO.getNickname());
            }
            totalPrice = totalPrice.add(record.getPrice());
            totalGold = totalGold.add(record.getGold());
            us.setGold(record.getGold());
            us.setPrice(record.getPrice());
            //根据时间的起始查询用户提现多少金额
            List<Map<String, Object>> withdrawalMapList = withdrawalMapper.selectMaps(new QueryWrapper<WithdrawalDO>().select("sum(amount) as totalPrice").lambda().ge(WithdrawalDO::getCreateTime, startTime).le(WithdrawalDO::getCreateTime, endTime).eq(WithdrawalDO::getUserId, record.getId()).eq(WithdrawalDO::getState, ExamineEnum.PAID.getKey()));
            BigDecimal withdrawalPrice = new BigDecimal(ObjectUtil.isNull(withdrawalMapList.get(0)) ? "0" : withdrawalMapList.get(0).get("totalPrice").toString());
            us.setWithdrawalPrice(withdrawalPrice);
            totalWithdrawalPrice = totalWithdrawalPrice.add(withdrawalPrice);
            //查询用户充值了多少金额
            List<Map<String, Object>> mapList = payOrderMapper.selectMaps(new QueryWrapper<PayOrderDO>().select("sum(price) as totalPrice").lambda().ge(PayOrderDO::getCreateTime, startTime).le(PayOrderDO::getCreateTime, endTime).eq(PayOrderDO::getUserId, record.getId()).eq(PayOrderDO::getState, PayOrderStateEnum.PAID.getKey()).eq(PayOrderDO::getType, PayOrderTypeEnum.RECHARGE.getKey()));

            BigDecimal rechargePrice = new BigDecimal(ObjectUtil.isNull(mapList.get(0)) ? "0" : mapList.get(0).get("totalPrice").toString());
            us.setRechargePrice(rechargePrice);
            totalRechargePrice = totalRechargePrice.add(rechargePrice);
            //查询用户投注多少金额
            List<Map<String, Object>> lotteryOrderMapList = lotteryOrderMapper.selectMaps(new QueryWrapper<LotteryOrderDO>().select("sum(price) as totalPrice").lambda().ge(LotteryOrderDO::getCreateTime, startTime).le(LotteryOrderDO::getCreateTime, endTime).eq(LotteryOrderDO::getUserId, record.getId()).ne(LotteryOrderDO::getState, LotteryOrderStateEnum.REFUSE.getKey()).ne(LotteryOrderDO::getState, LotteryOrderStateEnum.REFUND.getKey()));
            BigDecimal bettingPrice = new BigDecimal(ObjectUtil.isNull(lotteryOrderMapList.get(0)) ? "0" : lotteryOrderMapList.get(0).get("totalPrice").toString());
            us.setBettingPrice(bettingPrice);
            totalBettingPrice = totalBettingPrice.add(bettingPrice);
            list.add(us);
        }
        map.put("list", list);
        map.put("totalWithdrawalPrice", totalWithdrawalPrice);
        map.put("totalRechargePrice", totalRechargePrice);
        map.put("totalBettingPrice", totalBettingPrice);
        map.put("totalGold", totalGold);
        map.put("totalPrice", totalPrice);
        return map;
    }
}
