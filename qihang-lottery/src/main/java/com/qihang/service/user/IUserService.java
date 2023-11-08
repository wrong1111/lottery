package com.qihang.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.controller.user.admin.dto.*;
import com.qihang.controller.user.admin.vo.AdminStatisticsVO;
import com.qihang.controller.user.admin.vo.UserQueryVO;
import com.qihang.controller.user.app.dto.*;
import com.qihang.controller.user.app.vo.AgentVO;
import com.qihang.controller.user.app.vo.UserByNickNameDTO;
import com.qihang.controller.user.app.vo.UserVO;
import com.qihang.domain.user.UserDO;

/**
 * @author: bright
 * @description:
 * @time: 2022-06-28 10:09
 */
public interface IUserService extends IService<UserDO> {
    /**
     * 登录
     *
     * @param userLoginDTO
     * @return
     */
    BaseVO login(UserLoginDTO userLoginDTO);

    /**
     * 刷新token
     *
     * @param userRefreshDTO
     * @return
     */
    BaseVO refresh(UserRefreshDTO userRefreshDTO);


    /**
     * 发送验证码
     *
     * @param phoneDTO
     * @param ip
     * @return
     */
    BaseVO send(PhoneDTO phoneDTO, String ip);

    /**
     * 注册
     *
     * @param registerDTO
     * @return
     * @throws Exception
     */
    BaseVO register(RegisterDTO registerDTO) throws Exception;


    /**
     * 修改密码
     *
     * @param changePwdDTO
     * @return
     */
    BaseVO changePwd(ChangePwdDTO changePwdDTO);

    /**
     * 修改用户信息
     *
     * @param changeUserDTO
     * @return
     */
    BaseVO changeUser(ChangeUserDTO changeUserDTO);


    /**
     * 实名认证
     *
     * @param realDTO
     * @param userId
     * @return
     */
    BaseVO real(RealDTO realDTO, Integer userId);

    /**
     * 获取用户信息
     *
     * @param userId
     * @return
     */
    UserVO getUser(Integer userId);


    /**
     * 后台用户列表
     *
     * @param userQueryDTO
     * @return
     */
    CommonListVO<UserQueryVO> pageList(UserQueryDTO userQueryDTO);


    /**
     * 获取下级代理接口
     *
     * @param userId
     * @param userAgent
     * @return
     */
    CommonListVO<AgentVO> agent(UserAgentDTO userAgent, Integer userId);

    /**
     * 后台删除
     *
     * @param id
     * @return
     */
    BaseVO delete(Integer id);

    /**
     * 禁用启用状态
     *
     * @param userUpdate
     * @param id
     * @return
     */
    BaseVO updateStatus(UserUpdateDTO userUpdate, Integer id);

    /**
     * 加减钱
     *
     * @param userRecharge
     * @param id
     * @return
     */
    BaseVO recharge(UserRechargeDTO userRecharge, Integer id);

    /**
     * 后台修改用户密码接口
     *
     * @param adminChangePwd
     * @return
     */
    BaseVO adminChangePwd(AdminChangePwdDTO adminChangePwd);

    /**
     * 用户统计
     *
     * @param userStatistics
     * @return
     */
    AdminStatisticsVO userStatistics(UserStatisticsDTO userStatistics);

    /**
     * 根据昵称获取用户信息
     *
     * @param userByNickName
     * @return
     */
    CommonListVO<UserVO> getUserByNickName(UserByNickNameDTO userByNickName);


    /**
     * 根据用户id获取租户id
     *
     * @param uid
     * @return
     */
    UserVO getTenantId(Integer uid);


    /**
     * 提现账户绑定
     *
     * @param binding
     * @param userId
     * @return
     */
    BaseVO binding(BindingDTO binding, Integer userId);


    /**
     * 校验手机号不能串店铺
     *
     * @param checkPhone
     * @return
     */
    BaseVO checkPhone(CheckPhoneDTO checkPhone);


    /**
     * 校验手机是否已注册
     *
     * @param phone
     * @return
     */
    BaseVO checkPhoneIsExist(PhoneDTO phone);


    /**
     * 后台添加用户
     *
     * @param userAdd
     * @return
     */
    BaseVO addUser(UserAddDTO userAdd);
}
