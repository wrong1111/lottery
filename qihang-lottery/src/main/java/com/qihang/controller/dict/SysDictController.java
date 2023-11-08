package com.qihang.controller.dict;


import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.controller.dict.dto.SysDictDTO;
import com.qihang.controller.dict.vo.SysDictQueryVO;
import com.qihang.service.dict.ISysDictService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;

/**
 * @author bright
 * @since 2022-11-14
 */
@RestController
@RequestMapping("/admin/dict")
public class SysDictController {
    @Resource
    private ServletRequest request;
    @Resource
    private ISysDictService sysDictService;

    @PutMapping("/insert")
    @ApiOperation("添加数据字段")
    public BaseVO insert(@RequestBody SysDictDTO sysDict) {
        return sysDictService.insert(sysDict);
    }

    @GetMapping("/get/{tenantId}")
    @ApiOperation("根据租户id查询数据字典")
    public CommonListVO<SysDictQueryVO> queryDictByTenantId(@PathVariable("tenantId") Integer tenantId) {
        return sysDictService.queryDictByTenantId(tenantId);
    }
}
