package com.qihang.constant;

/**
 * @author: bright
 * @description:
 * @time: 2022-07-13 11:22
 */
public class Constant {
    /**
     * token密钥
     */
    public static String SECRET = "byte-bright";

    /**
     * 刷新token密钥
     */
    public static String REFRESH_SECRET = "byte-bright-refresh-token";

    /**
     * token失效时间 7天
     */
    public static final int JWT_TTL = 604800;

    /**
     * 验证码的redis key
     */
    public static String REDIS_CODE = "code-key";


    /**
     * ip的redis key
     */
    public static String IP_KEY = "ip-key";
    /**
     * 一分钟请求次数
     */
    public static Integer FREQUENCY = 1;

    public static final String FORMAT_YEAR_MONTH_DAY = "yyyy-MM-dd";

    /**
     * 支付宝pid前缀
     */
    public static final String ALIPAY_APPID_KAY = "alipay_appid_kay";
    /**
     * 支付宝公钥前缀
     */
    public static final String ALIPAY_PUBLIC_KAY = "alipay_public_kay";
    /**
     * 支付宝私钥前缀
     */
    public static final String ALIPAY_PRIVATE_KAY = "alipay_private_kay";
}
