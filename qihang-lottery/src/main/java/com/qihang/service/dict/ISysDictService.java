package com.qihang.service.dict;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.controller.dict.dto.SysDictDTO;
import com.qihang.controller.dict.vo.SysDictQueryVO;
import com.qihang.domain.dict.SysDictDO;

/**
 * @author bright
 * @since 2022-11-14
 */
public interface ISysDictService extends IService<SysDictDO> {
    /**
     * 添加
     *
     * @param sysDict
     * @return
     */
    BaseVO insert(SysDictDTO sysDict);

    /**
     * 根据租户id查询数据字典
     *
     * @param tenantId
     * @return
     */
    CommonListVO<SysDictQueryVO> queryDictByTenantId(Integer tenantId);


}
