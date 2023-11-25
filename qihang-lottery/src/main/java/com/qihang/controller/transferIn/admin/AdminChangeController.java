package com.qihang.controller.transferIn.admin;

import com.qihang.common.vo.BaseVO;
import com.qihang.controller.transferIn.admin.dto.ChangeDTO;
import com.qihang.service.transfer.IChangeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @Author: wyong
 * @Date: 2023/11/25 025 20:59
 * @Description:
 **/
@RestController
@RequestMapping("/admin/transfer/change")
public class AdminChangeController {


    @Resource
    IChangeService changeService;

    @PostMapping("/info")
    public BaseVO info(@Valid @RequestBody ChangeDTO changeDTO) {
        String url = changeDTO.getUrl();
        return changeService.info(url);
    }
}
