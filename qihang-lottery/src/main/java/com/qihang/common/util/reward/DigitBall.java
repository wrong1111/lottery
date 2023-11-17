package com.qihang.common.util.reward;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@AllArgsConstructor
@NoArgsConstructor

@Data
public class DigitBall implements Serializable {

    Boolean award;
    String stageNumber;
    String content;
    String mode;
    String money;
    String awardContent;
    String level;


}
