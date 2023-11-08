package com.qihang.service.documentary;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qihang.common.vo.BaseVO;
import com.qihang.common.vo.CommonListVO;
import com.qihang.controller.documentary.app.dto.CreateDocumentaryDTO;
import com.qihang.controller.documentary.app.dto.CreateDocumentaryUserDTO;
import com.qihang.controller.documentary.app.vo.DocumentaryByIdVO;
import com.qihang.controller.documentary.app.vo.DocumentaryByTypeVO;
import com.qihang.controller.documentary.app.vo.DocumentaryDetailsVO;
import com.qihang.controller.documentary.app.vo.DocumentarySagaVO;
import com.qihang.domain.documentary.DocumentaryDO;

/**
 * @author bright
 * @since 2022-11-10
 */
public interface IDocumentaryService extends IService<DocumentaryDO> {

    /**
     * 创建发单
     *
     * @param createDocumentary
     * @param userId
     * @return
     */
    BaseVO create(CreateDocumentaryDTO createDocumentary, Integer userId);

    /**
     * 跟单排行榜列表
     *
     * @return
     */
    DocumentarySagaVO ranking();

    /**
     * 根据用户id查询跟单信息
     *
     * @param userId   当前登录的用户id
     * @param targetId 需要查询的用户id
     * @return
     */
    DocumentaryDetailsVO documentaryDetails(Integer userId, Integer targetId);


    /**
     * 根据类型查询对应的跟单数据
     *
     * @param type   0 人气跟单 1 跟单总额 2 我的关注
     * @param userId
     * @return
     */
    CommonListVO<DocumentaryByTypeVO> queryDocumentaryByType(String type, Integer userId);

    /**
     * 根据跟单id和目標用户id查询跟单信息
     *
     * @param id
     * @param userId
     * @param targetId
     * @return
     */
    DocumentaryByIdVO queryDocumentaryById(Integer id, Integer userId, Integer targetId);


    /**
     * 创建用户跟单
     *
     * @param createDocumentaryUser
     * @param userId
     * @return
     */
    BaseVO createDocumentaryUser(CreateDocumentaryUserDTO createDocumentaryUser, Integer userId);
}
