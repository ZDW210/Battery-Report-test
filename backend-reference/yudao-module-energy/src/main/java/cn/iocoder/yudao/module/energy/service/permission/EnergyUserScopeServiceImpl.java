package cn.iocoder.yudao.module.energy.service.permission;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.energy.controller.admin.permission.vo.EnergyUserScopePageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.permission.vo.EnergyUserScopeSaveReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.permission.EnergyUserScopeDO;
import cn.iocoder.yudao.module.energy.dal.mysql.permission.EnergyUserScopeMapper;
import cn.iocoder.yudao.module.energy.service.customer.EnergyCustomerService;
import cn.iocoder.yudao.module.energy.service.device.EnergyDeviceService;
import cn.iocoder.yudao.module.energy.service.project.EnergyProjectService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.energy.enums.ErrorCodeConstants.*;

@Service
@Validated
public class EnergyUserScopeServiceImpl implements EnergyUserScopeService {

    @Resource
    private EnergyUserScopeMapper userScopeMapper;
    @Resource
    private EnergyCustomerService customerService;
    @Resource
    private EnergyProjectService projectService;
    @Resource
    private EnergyDeviceService deviceService;

    @Override
    public Long createUserScope(EnergyUserScopeSaveReqVO createReqVO) {
        validateUserScopeSaveData(null, createReqVO);
        EnergyUserScopeDO userScope = BeanUtils.toBean(createReqVO, EnergyUserScopeDO.class);
        userScopeMapper.insert(userScope);
        return userScope.getId();
    }

    @Override
    public void updateUserScope(EnergyUserScopeSaveReqVO updateReqVO) {
        validateUserScopeExists(updateReqVO.getId());
        validateUserScopeSaveData(updateReqVO.getId(), updateReqVO);
        userScopeMapper.updateById(BeanUtils.toBean(updateReqVO, EnergyUserScopeDO.class));
    }

    @Override
    public void deleteUserScope(Long id) {
        validateUserScopeExists(id);
        userScopeMapper.deleteById(id);
    }

    @Override
    public EnergyUserScopeDO getUserScope(Long id) {
        return userScopeMapper.selectById(id);
    }

    @Override
    public PageResult<EnergyUserScopeDO> getUserScopePage(EnergyUserScopePageReqVO pageReqVO) {
        return userScopeMapper.selectPage(pageReqVO);
    }

    private void validateUserScopeSaveData(Long id, EnergyUserScopeSaveReqVO reqVO) {
        validateScopeTarget(reqVO);
        if (reqVO.getCustomerId() != null) {
            customerService.validateCustomerExists(reqVO.getCustomerId());
        }
        if (reqVO.getProjectId() != null) {
            projectService.validateProjectExists(reqVO.getProjectId());
        }
        if (reqVO.getDeviceId() != null) {
            deviceService.validateDeviceExists(reqVO.getDeviceId());
        }
        EnergyUserScopeDO exists = userScopeMapper.selectByScope(reqVO.getUserId(), reqVO.getUserType(),
                reqVO.getCustomerId(), reqVO.getProjectId(), reqVO.getDeviceId());
        if (exists != null && ObjUtil.notEqual(exists.getId(), id)) {
            throw exception(USER_SCOPE_DUPLICATE);
        }
    }

    private void validateScopeTarget(EnergyUserScopeSaveReqVO reqVO) {
        int targetCount = 0;
        if (reqVO.getCustomerId() != null) {
            targetCount++;
        }
        if (reqVO.getProjectId() != null) {
            targetCount++;
        }
        if (reqVO.getDeviceId() != null) {
            targetCount++;
        }
        if (targetCount != 1) {
            throw exception(USER_SCOPE_TARGET_REQUIRED);
        }
    }

    private void validateUserScopeExists(Long id) {
        if (id == null || userScopeMapper.selectById(id) == null) {
            throw exception(USER_SCOPE_NOT_EXISTS);
        }
    }

}
