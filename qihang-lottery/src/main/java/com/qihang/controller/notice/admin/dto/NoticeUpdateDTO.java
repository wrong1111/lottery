package com.qihang.controller.notice.admin.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author bright
 * @since 2022-10-08
 */
@Data
@Accessors(chain = true)
public class NoticeUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "公告")
    @NotBlank(message = "公告不能为空")
    private String msg;

}
