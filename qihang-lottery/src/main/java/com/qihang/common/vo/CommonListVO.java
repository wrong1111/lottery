package com.qihang.common.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

/**
 * @author: bright
 * @description:
 * @time: 2022-07-31 16:34
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class CommonListVO<T> extends BaseVO {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "数据总个数")
    private Long total;

    @ApiModelProperty(value = "数据集合")
    List<T> voList;
}
