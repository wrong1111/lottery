package com.qihang.common.vo;

import lombok.Data;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
public class BaseDataVO extends BaseVO {
    Object data;
}
