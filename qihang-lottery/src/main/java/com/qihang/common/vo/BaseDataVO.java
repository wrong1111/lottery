package com.qihang.common.vo;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class BaseDataVO extends BaseVO {
    Object data;
}
