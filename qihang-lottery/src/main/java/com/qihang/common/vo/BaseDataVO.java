package com.qihang.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class BaseDataVO extends BaseVO {
    Object data;
}
