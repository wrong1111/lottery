package com.qihang.controller.transferIn.admin;

import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.controller.transferIn.admin.dto.ChangeDTO;
import com.qihang.controller.transferIn.admin.dto.LotteryAutoStateDTO;
import com.qihang.controller.transferIn.admin.dto.LotteryOutDTO;
import com.qihang.service.transfer.IChangeService;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/list")
    public CommonListVO list(@RequestBody LotteryOutDTO lotteryOutDTO) {
        return changeService.list(lotteryOutDTO);
    }

    @GetMapping("/shopall")
    public CommonListVO shopList() {
        return changeService.listShop();
    }

    @PostMapping("/editAutoState")
    public BaseVO editAutoState(@Valid @RequestBody LotteryAutoStateDTO lotteryOutDTO) {
        return changeService.editAutoState(lotteryOutDTO);
    }

    @PostMapping("/send")
    public BaseVO send(@RequestParam Integer id) {
        return changeService.send(id, false);
    }
}
