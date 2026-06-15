package cn.iocoder.yudao.module.energy.service.project;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.energy.controller.admin.project.vo.EnergyProjectPageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.project.vo.EnergyProjectSaveReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.project.EnergyProjectDO;
import cn.iocoder.yudao.module.energy.dal.mysql.project.EnergyProjectMapper;
import cn.iocoder.yudao.module.energy.service.customer.EnergyCustomerService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.energy.enums.ErrorCodeConstants.*;

/**
 * 移动储能项目 Service 实现类。
 */
@Service
@Validated
public class EnergyProjectServiceImpl implements EnergyProjectService {

    @Resource
    private EnergyProjectMapper projectMapper;
    @Resource
    private EnergyCustomerService customerService;

    @Override
    public Long createProject(EnergyProjectSaveReqVO createReqVO) {
        validateProjectSaveData(createReqVO);
        EnergyProjectDO project = BeanUtils.toBean(createReqVO, EnergyProjectDO.class);
        projectMapper.insert(project);
        return project.getId();
    }

    @Override
    public void updateProject(EnergyProjectSaveReqVO updateReqVO) {
        validateProjectExists(updateReqVO.getId());
        validateProjectSaveData(updateReqVO);
        EnergyProjectDO updateObj = BeanUtils.toBean(updateReqVO, EnergyProjectDO.class);
        projectMapper.updateById(updateObj);
    }

    private void validateProjectSaveData(EnergyProjectSaveReqVO reqVO) {
        customerService.validateCustomerExists(reqVO.getCustomerId());
        EnergyProjectDO project = projectMapper.selectByCode(reqVO.getCode());
        if (project != null && ObjUtil.notEqual(project.getId(), reqVO.getId())) {
            throw exception(PROJECT_CODE_DUPLICATE);
        }
    }

    @Override
    public void deleteProject(Long id) {
        validateProjectExists(id);
        projectMapper.deleteById(id);
    }

    @Override
    public EnergyProjectDO getProject(Long id) {
        return projectMapper.selectById(id);
    }

    @Override
    public PageResult<EnergyProjectDO> getProjectPage(EnergyProjectPageReqVO pageReqVO) {
        return projectMapper.selectPage(pageReqVO);
    }

    @Override
    public List<EnergyProjectDO> getProjectSimpleList(Long customerId) {
        return projectMapper.selectSimpleList(customerId);
    }

    @Override
    public EnergyProjectDO validateProjectExists(Long id) {
        EnergyProjectDO project = projectMapper.selectById(id);
        if (project == null) {
            throw exception(PROJECT_NOT_EXISTS);
        }
        return project;
    }

}
