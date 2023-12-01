package com.qihang.domain.beidan;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/*
 北单胜负过关赛事
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_beidan_sfgg_match")
public class BeiDanSFGGMatchDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 编号
     */
    private String number;

    /**
     * 颜色
     */
    private String color;

    /**
     * 赛事
     */
    @TableField(value = "`match`")
    private String match;

    String unionMatch;
    /**
     * 分析链接
     */
    private String analysis;

    /**
     * 下注状态 0 不可下注 1 可下注
     */
    private String state;

    /**
     * 主胜赔率
     */
    private String hostWinOdds;

    /**
     * 客胜赔率
     */
    private String visitWinOdds;


    /**
     * 主队
     */
    private String homeTeam;

    /**
     * 客队
     */
    private String visitingTeam;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 下注截止时间
     */
    private Date deadline;

    /**
     * 让球
     */
    private String letBall;

    /**
     * 球赛开始时间
     */
    private String startTime;

    /**
     * 开奖
     */
    private String award;

    /**
     * 半全场结果
     */
    private String halfFullCourt;

    /**
     * 出奖赔率
     */
    private String bonusOdds;

    /**
     *
     */
    String issueNo;

    String gameNo;
}