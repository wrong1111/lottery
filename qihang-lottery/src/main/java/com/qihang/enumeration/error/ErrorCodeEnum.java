package com.qihang.enumeration.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: bright
 * @description:
 * @time: 2022-06-28 10:27
 */
@Getter
@AllArgsConstructor
public enum ErrorCodeEnum {
    E0751("E0751", "账户或密码不正确"),
    E0752("E0752", "token过期"),
    E0753("E0753", "token无效"),
    E0754("E0754", "令牌应该包含3个部分"),

    E0755("E0755", "该手机号已注册"),
    E0756("E0756", "用户名不存在"),
    E0757("E0757", "该账号违规已被封禁"),
    E0758("E0758", "未登录或者用户token无效或已过期"),
    E0759("E0759", "登录失败"),
    E0760("E0760", "验证码错误"),
    E0761("E0761", "请勿频繁发送"),
    E0762("E0762", "提现额度不能大于当前余额"),
    E0763("E0763", "账户余额不足"),

    E0764("E0764", "自己不能设置自己为代理"),

    E075("E075", "该订单未中奖"),

    E076("E076", "未有待出票的订单"),
    E077("E077", "未有待中奖的订单"),

    E078("E078", "该订单已退票"),

    E079("E079", "自己不能关注自己"),

    E080("E080", "自己不能跟自己的单"),

    E081("E081", "余额不足，请联系店主进行充值"),

    E082("E082", "晚上20~22点是截止下注时间"),

    E083("E083", "该订单已出奖，不支持退票"),

    E084("E084", "当前比赛已截止"),

    E085("E085", "该店铺已下架,请联系原店主解决资金问题"),

    E086("E086", "支付密码不正确"),

    E087("E087", "原密码不正确"),
    E088("E088", "原支付密码不正确"),
    E089("E089", "该手机号已注册其它店铺，请更换新手机号注册"),

    E090("E090", "用户名已存在"),

    E091("E091", "上午10点到下午17点为提现时间"),

    E093("E093", "提现金额最低10起提"),
    E094("E094", "该订单奖金未确认");
    private String key;
    private String value;
}
