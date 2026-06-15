package cn.iocoder.yudao.module.energy.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.energy.controller.admin.device.vo.EnergyDeviceControlReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.device.vo.EnergyDeviceControlRespVO;
import cn.iocoder.yudao.module.energy.controller.admin.device.vo.EnergyDevicePageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.device.vo.EnergyDeviceRespVO;
import cn.iocoder.yudao.module.energy.controller.admin.device.vo.EnergyDeviceSaveReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.customer.EnergyCustomerDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.device.EnergyDeviceDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.project.EnergyProjectDO;
import cn.iocoder.yudao.module.energy.service.customer.EnergyCustomerService;
import cn.iocoder.yudao.module.energy.service.device.EnergyDeviceControlService;
import cn.iocoder.yudao.module.energy.service.device.EnergyDeviceService;
import cn.iocoder.yudao.module.energy.service.project.EnergyProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 移动储能设备")
@RestController
@RequestMapping("/energy/device")
@Validated
public class EnergyDeviceController {

    @Resource
    private EnergyDeviceService deviceService;
    @Resource
    private EnergyDeviceControlService deviceControlService;
    @Resource
    private EnergyCustomerService customerService;
    @Resource
    private EnergyProjectService projectService;

    @PostMapping("/create")
    @Operation(summary = "创建设备")
    @PreAuthorize("@ss.hasPermission('energy:device:create')")
    public CommonResult<Long> createDevice(@Valid @RequestBody EnergyDeviceSaveReqVO createReqVO) {
        return success(deviceService.createDevice(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备")
    @PreAuthorize("@ss.hasPermission('energy:device:update')")
    public CommonResult<Boolean> updateDevice(@Valid @RequestBody EnergyDeviceSaveReqVO updateReqVO) {
        deviceService.updateDevice(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除设备")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('energy:device:delete')")
    public CommonResult<Boolean> deleteDevice(@RequestParam("id") Long id) {
        deviceService.deleteDevice(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得设备")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('energy:device:query')")
    public CommonResult<EnergyDeviceRespVO> getDevice(@RequestParam("id") Long id) {
        EnergyDeviceDO device = deviceService.getDevice(id);
        EnergyDeviceRespVO respVO = BeanUtils.toBean(device, EnergyDeviceRespVO.class);
        fillCustomerAndProjectNames(Collections.singletonList(respVO));
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得设备分页")
    @PreAuthorize("@ss.hasPermission('energy:device:query')")
    public CommonResult<PageResult<EnergyDeviceRespVO>> getDevicePage(@Valid EnergyDevicePageReqVO pageReqVO) {
        PageResult<EnergyDeviceDO> pageResult = deviceService.getDevicePage(pageReqVO);
        PageResult<EnergyDeviceRespVO> respPageResult = BeanUtils.toBean(pageResult, EnergyDeviceRespVO.class);
        fillCustomerAndProjectNames(respPageResult.getList());
        return success(respPageResult);
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得设备精简列表", description = "主要用于前端下拉")
    @PreAuthorize("@ss.hasPermission('energy:device:query')")
    public CommonResult<List<EnergyDeviceRespVO>> getDeviceSimpleList(
            @RequestParam(value = "customerId", required = false) Long customerId,
            @RequestParam(value = "projectId", required = false) Long projectId) {
        List<EnergyDeviceDO> list = deviceService.getDeviceSimpleList(customerId, projectId);
        return success(BeanUtils.toBean(list, EnergyDeviceRespVO.class));
    }

    @PostMapping("/control")
    @Operation(summary = "Control device")
    @PreAuthorize("@ss.hasPermission('energy:device:control')")
    public CommonResult<EnergyDeviceControlRespVO> controlDevice(
            @Valid @RequestBody EnergyDeviceControlReqVO reqVO) {
        return success(deviceControlService.controlDevice(reqVO, getLoginUserId()));
    }

    private void fillCustomerAndProjectNames(List<EnergyDeviceRespVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Map<Long, EnergyCustomerDO> customerMap = customerService.getCustomerSimpleList().stream()
                .collect(Collectors.toMap(EnergyCustomerDO::getId, Function.identity(), (first, second) -> first));
        Map<Long, EnergyProjectDO> projectMap = projectService.getProjectSimpleList(null).stream()
                .collect(Collectors.toMap(EnergyProjectDO::getId, Function.identity(), (first, second) -> first));
        list.forEach(device -> {
            EnergyCustomerDO customer = customerMap.get(device.getCustomerId());
            if (customer != null) {
                device.setCustomerName(customer.getName());
            }
            EnergyProjectDO project = projectMap.get(device.getProjectId());
            if (project != null) {
                device.setProjectName(project.getName());
            }
        });
    }

}
