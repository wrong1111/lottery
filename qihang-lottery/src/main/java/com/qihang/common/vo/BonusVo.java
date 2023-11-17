package com.qihang.common.vo;


import lombok.Data;

@Data
public class BonusVo {
    Boolean award;
    Double money;
    String level;

    String awardContent;
    int awardNotes;
}
