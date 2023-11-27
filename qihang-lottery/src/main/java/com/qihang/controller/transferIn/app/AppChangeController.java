package com.qihang.controller.transferIn.app;


import com.qihang.common.vo.BaseVO;
import com.qihang.service.transfer.IChangeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;


@RequestMapping("/app/change")
@Resource
public class AppChangeController {


    @Resource
    IChangeService changeService;

    @PostMapping("/send")
    public BaseVO send(@RequestParam Integer id) {
        return changeService.send(id, false);
    }
}
