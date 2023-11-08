package com.qihang.controller.withdrawal.app;


import com.qihang.common.util.log.LogUtil;
import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.controller.withdrawal.app.dto.WithdrawalDTO;
import com.qihang.controller.withdrawal.app.vo.RecordVO;
import com.qihang.service.withdrawal.IWithdrawalService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.validation.Valid;

/**
 * @author bright
 * @since 2022-10-08
 */
@RestController
@RequestMapping("/app/withdrawal")
public class AppWithdrawalController {

    @Resource
    private IWithdrawalService withdrawalService;

    @Resource
    private ServletRequest request;

    @Resource
    private LogUtil logUtil;

    @PostMapping("/add")
    @ApiOperation("提现申请接口")
    public BaseVO add(@RequestBody @Valid WithdrawalDTO withdrawalDTO) {
        logUtil.record("发起【" + withdrawalDTO.getAmount() + "】提现申请");
        return withdrawalService.add(withdrawalDTO, Integer.valueOf(request.getAttribute("User-ID").toString()));
    }

    @GetMapping("/list")
    @ApiOperation("提现记录列表接口")
    public CommonListVO<RecordVO> list() {
        return withdrawalService.list(Integer.valueOf(request.getAttribute("User-ID").toString()));
    }
}
