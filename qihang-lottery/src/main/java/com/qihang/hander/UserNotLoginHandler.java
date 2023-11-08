package com.qihang.hander;

import com.qihang.common.util.response.ResponseUtil;
import com.qihang.enumeration.error.ErrorCodeEnum;
import com.qihang.common.vo.BaseVO;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @description: 用户未登录处理类
 * @Author: bright
 * @Date: 2020/4/28
 **/
@Component
public class UserNotLoginHandler implements AuthenticationEntryPoint {
    /**
     * 用户未登录返回结果
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
        System.out.println(exception);
        ResponseUtil.ResponseMeg(response, new BaseVO(false, ErrorCodeEnum.E0758.getKey(), ErrorCodeEnum.E0758.getValue()));
    }
}