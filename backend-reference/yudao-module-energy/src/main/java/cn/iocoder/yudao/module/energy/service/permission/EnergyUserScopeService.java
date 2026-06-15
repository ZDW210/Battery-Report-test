package cn.iocoder.yudao.module.energy.service.permission;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.energy.controller.admin.permission.vo.EnergyUserScopePageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.permission.vo.EnergyUserScopeSaveReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.permission.EnergyUserScopeDO;

public interface EnergyUserScopeService {

    Long createUserScope(EnergyUserScopeSaveReqVO createReqVO);

    void updateUserScope(EnergyUserScopeSaveReqVO updateReqVO);

    void deleteUserScope(Long id);

    EnergyUserScopeDO getUserScope(Long id);

    PageResult<EnergyUserScopeDO> getUserScopePage(EnergyUserScopePageReqVO pageReqVO);

}
