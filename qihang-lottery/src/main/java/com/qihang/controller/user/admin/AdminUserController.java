package com.qihang.controller.user.admin;


import com.qihang.common.vo.BaseVO;
import com.qihang.controller.user.admin.dto.*;
import com.qihang.service.user.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author: bright
 * @description:
 * @time: 2022-06-28 10:12
 */
@RestController
@Api(tags = "后台 - 用户")
@RequestMapping("/admin/user")
public class AdminUserController {
    @Resource
    private IUserService userService;

    @PostMapping("/list")
    @ApiOperation("用户列表")
    public BaseVO login(@RequestBody @Valid UserQueryDTO userQueryDTO) {
        return userService.pageList(userQueryDTO);
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除用户接口")
    public BaseVO delete(@PathVariable("id") Integer id) {
        return userService.delete(id);
    }

    @PutMapping("/update/{id}")
    @ApiOperation("修改接口")
    public BaseVO updateStatus(@RequestBody @Valid UserUpdateDTO userUpdate, @PathVariable("id") Integer id) {
        return userService.updateStatus(userUpdate, id);
    }

    @PutMapping("/recharge/{id}")
    @ApiOperation("充值接口")
    public BaseVO recharge(@RequestBody @Valid UserRechargeDTO userRecharge, @PathVariable("id") Integer id) {
        return userService.recharge(userRecharge, id);
    }

    @PutMapping("/change/pwd")
    @ApiOperation("修改密码")
    public BaseVO adminChangePwd(@RequestBody @Valid AdminChangePwdDTO changePwd) {
        return userService.adminChangePwd(changePwd);
    }

    @PostMapping("/statistics")
    @ApiOperation("用户统计")
    public BaseVO userStatistics(@RequestBody @Valid UserStatisticsDTO userStatistics) {
        return userService.userStatistics(userStatistics);
    }

    @PostMapping("/add")
    @ApiOperation("添加用户")
    public BaseVO addUser(@RequestBody @Valid UserAddDTO userAdd) {
        return userService.addUser(userAdd);
    }

}
