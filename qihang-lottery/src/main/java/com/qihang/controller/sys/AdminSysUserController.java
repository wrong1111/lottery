package com.qihang.controller.sys;


import com.qihang.common.vo.BaseVO;
import com.qihang.controller.sys.dto.*;
import com.qihang.service.user.ISysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.validation.Valid;

/**
 * @author: bright
 * @description:
 * @time: 2022-06-28 10:12
 */
@RestController
@Api(tags = "后台 - 用户")
@RequestMapping("/sys/user")
public class AdminSysUserController {
    @Resource
    private ISysUserService userService;

    @Resource
    private ServletRequest request;

    @PostMapping("/login")
    @ApiOperation("登录接口")
    public BaseVO login(@RequestBody @Valid AdminUserLoginDTO userLoginDTO) {
        return userService.login(userLoginDTO);
    }


    @GetMapping("/token/{username}")
    @ApiOperation("根据用户名获取token接口")
    public BaseVO getTokenByUserName(@PathVariable("username") String username) {
        return userService.getTokenByUserName(username);
    }

    @GetMapping("/get/{tenantId}")
    @ApiOperation("根据租户id查询对应的系统账户列表")
    public BaseVO queryUserByTenantId(@PathVariable("tenantId") Integer tenantId) {
        return userService.queryUserByTenantId(tenantId);
    }

    @PostMapping("/verify")
    @ApiOperation("校验支付密码")
    public BaseVO verifyPayPwd(@RequestBody @Valid VerifyPayPwdDTO verifyPayPwdDTO) {
        return userService.verifyPayPwd(verifyPayPwdDTO);
    }

    @PutMapping("/update")
    @ApiOperation("修改")
    public BaseVO update(@RequestBody @Valid UpdateUserDTO updateUserDTO) {
        return userService.update(updateUserDTO);
    }


    @PostMapping("/list")
    @ApiOperation("用户列表")
    public BaseVO pageList(@RequestBody @Valid AdminUserQueryDTO adminUserQuery) {
        return userService.pageList(adminUserQuery);
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除用户")
    public BaseVO delete(@PathVariable("id") Integer id) {
        return userService.delete(id);
    }

    @PostMapping("/add")
    @ApiOperation("添加用户")
    public BaseVO add(@RequestBody @Valid AdminUserAddDTO adminUserAdd) {
        return userService.add(adminUserAdd, Integer.valueOf(request.getAttribute("User-ID").toString()));
    }
}
