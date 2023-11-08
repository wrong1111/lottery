package com.qihang.controller.user.app;


import cn.hutool.extra.servlet.ServletUtil;
import com.qihang.common.util.log.LogUtil;
import com.qihang.common.vo.BaseVO;
import com.qihang.controller.user.app.dto.*;
import com.qihang.controller.user.app.vo.UserByNickNameDTO;
import com.qihang.controller.user.app.vo.UserVO;
import com.qihang.service.user.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * @author: bright
 * @description:
 * @time: 2022-06-28 10:12
 */
@RestController
@Api(tags = "APP - 用户")
@RequestMapping("/app/user")
public class AppUserController {

    @Resource
    private IUserService userService;

    @Resource
    private HttpServletRequest request;

    @Resource
    private LogUtil logUtil;

    @PostMapping("/refresh")
    @ApiOperation("刷新token接口")
    public BaseVO refresh(@RequestBody @Valid UserRefreshDTO refreshDTO) {
        return userService.refresh(refreshDTO);
    }

    @PostMapping("/send")
    @ApiOperation("发送短信接口")
    public BaseVO send(@RequestBody @Valid PhoneDTO phoneDTO) {
        String ip = ServletUtil.getClientIP(request);
        return userService.send(phoneDTO, ip);
    }

    @PostMapping("/login")
    @ApiOperation("登录接口")
    public BaseVO login(@RequestBody @Valid UserLoginDTO userLoginDTO) {
        return userService.login(userLoginDTO);
    }

    @PostMapping("/register")
    @ApiOperation("注册接口")
    public BaseVO register(@RequestBody @Valid RegisterDTO registerDTO) throws Exception {
        return userService.register(registerDTO);
    }

    @PutMapping("/change/pwd")
    @ApiOperation("修改密码接口")
    public BaseVO changePwd(@RequestBody @Valid ChangePwdDTO changePwdDTO) {
        logUtil.record("修改密码", changePwdDTO.getPhone());
        return userService.changePwd(changePwdDTO);
    }

    @PutMapping("/change/info")
    @ApiOperation("修改用户信息接口")
    public BaseVO changeUser(@RequestBody @Valid ChangeUserDTO changeUserDTO) {
        logUtil.record("修改个人资料");
        return userService.changeUser(changeUserDTO);
    }

    @PutMapping("/real")
    @ApiOperation("实名认证接口")
    public BaseVO real(@RequestBody @Valid RealDTO realDTO) {
        logUtil.record("实名认证");
        return userService.real(realDTO, Integer.valueOf(request.getAttribute("User-ID").toString()));
    }

    @GetMapping("/get")
    @ApiOperation("获取用户信息接口")
    public BaseVO getUser() {
        return userService.getUser(Integer.valueOf(request.getAttribute("User-ID").toString()));
    }


    @PostMapping("/agent")
    @ApiOperation("获取下级代理接口")
    public BaseVO agent(@RequestBody @Valid UserAgentDTO userAgent) {
        return userService.agent(userAgent, Integer.valueOf(request.getAttribute("User-ID").toString()));
    }

    @PostMapping("/get/nickname")
    @ApiOperation("根据昵称模糊查询")
    public BaseVO getUserByNickName(@RequestBody UserByNickNameDTO userByNickName) {
        return userService.getUserByNickName(userByNickName);
    }

    @GetMapping("/get/tenant/{uid}")
    @ApiOperation("根据用户uid获取租户id")
    public UserVO getTenantId(@PathVariable("uid") Integer uid) {
        return userService.getTenantId(uid);
    }


    @PostMapping("/binding")
    @ApiOperation("用户提现账户绑定")
    public BaseVO binding(@RequestBody BindingDTO binding) {
        logUtil.record("提现账户绑定");
        return userService.binding(binding, Integer.valueOf(request.getAttribute("User-ID").toString()));
    }

    @PostMapping("/check/phone")
    @ApiOperation("防止串客其它店铺")
    public BaseVO checkPhone(@RequestBody @Valid CheckPhoneDTO checkPhone) {
        return userService.checkPhone(checkPhone);
    }

    @PostMapping("/phone/exist")
    @ApiOperation("校验手机号是否注册")
    public BaseVO checkPhoneIsExist(@RequestBody @Valid PhoneDTO phone) {
        return userService.checkPhoneIsExist(phone);
    }
}
