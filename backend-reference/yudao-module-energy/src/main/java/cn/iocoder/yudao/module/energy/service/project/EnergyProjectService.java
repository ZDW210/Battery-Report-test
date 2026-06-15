package cn.iocoder.yudao.module.energy.service.project;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.energy.controller.admin.project.vo.EnergyProjectPageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.project.vo.EnergyProjectSaveReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.project.EnergyProjectDO;

import java.util.List;

/**
 * 移动储能项目 Service 接口。
 */
public interface EnergyProjectService {

    Long createProject(EnergyProjectSaveReqVO createReqVO);

    void updateProject(EnergyProjectSaveReqVO updateReqVO);

    void deleteProject(Long id);

    EnergyProjectDO getProject(Long id);

    PageResult<EnergyProjectDO> getProjectPage(EnergyProjectPageReqVO pageReqVO);

    List<EnergyProjectDO> getProjectSimpleList(Long customerId);

    EnergyProjectDO validateProjectExists(Long id);

}
