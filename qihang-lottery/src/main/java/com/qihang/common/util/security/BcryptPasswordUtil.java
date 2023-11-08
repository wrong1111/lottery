package com.qihang.common.util.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author: bright
 * @description: 密码加密工具类
 * @time: 2022-07-13 15:03
 */
public class BcryptPasswordUtil {
    public static String createBCryptPassword(String password) {
        return new BCryptPasswordEncoder().encode(password);
    }

    public static void main(String[] args) {
        System.out.println(createBCryptPassword("123456"));
    }
}
