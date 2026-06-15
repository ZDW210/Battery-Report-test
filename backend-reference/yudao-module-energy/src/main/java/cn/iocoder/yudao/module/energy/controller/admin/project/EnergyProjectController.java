package cn.iocoder.yudao.module.energy.controller.admin.project;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.energy.controller.admin.project.vo.EnergyProjectPageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.project.vo.EnergyProjectRespVO;
import cn.iocoder.yudao.module.energy.controller.admin.project.vo.EnergyProjectSaveReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.project.EnergyProjectDO;
import cn.iocoder.yudao.module.energy.service.project.EnergyProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 移动储能项目")
@RestController
@RequestMapping("/energy/project")
@Validated
public class EnergyProjectController {

    @Resource
    private EnergyProjectService projectService;

    @PostMapping("/create")
    @Operation(summary = "创建项目")
    @PreAuthorize("@ss.hasPermission('energy:project:create')")
    public CommonResult<Long> createProject(@Valid @RequestBody EnergyProjectSaveReqVO createReqVO) {
        return success(projectService.createProject(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新项目")
    @PreAuthorize("@ss.hasPermission('energy:project:update')")
    public CommonResult<Boolean> updateProject(@Valid @RequestBody EnergyProjectSaveReqVO updateReqVO) {
        projectService.updateProject(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除项目")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('energy:project:delete')")
    public CommonResult<Boolean> deleteProject(@RequestParam("id") Long id) {
        projectService.deleteProject(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得项目")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('energy:project:query')")
    public CommonResult<EnergyProjectRespVO> getProject(@RequestParam("id") Long id) {
        EnergyProjectDO project = projectService.getProject(id);
        return success(BeanUtils.toBean(project, EnergyProjectRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得项目分页")
    @PreAuthorize("@ss.hasPermission('energy:project:query')")
    public CommonResult<PageResult<EnergyProjectRespVO>> getProjectPage(@Valid EnergyProjectPageReqVO pageReqVO) {
        PageResult<EnergyProjectDO> pageResult = projectService.getProjectPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, EnergyProjectRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得项目精简列表", description = "主要用于前端下拉")
    @PreAuthorize("@ss.hasPermission('energy:project:query')")
    public CommonResult<List<EnergyProjectRespVO>> getProjectSimpleList(@RequestParam(value = "customerId", required = false) Long customerId) {
        List<EnergyProjectDO> list = projectService.getProjectSimpleList(customerId);
        return success(BeanUtils.toBean(list, EnergyProjectRespVO.class));
    }

}
