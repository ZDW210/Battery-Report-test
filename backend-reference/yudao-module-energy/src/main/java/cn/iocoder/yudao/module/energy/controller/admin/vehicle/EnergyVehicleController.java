package cn.iocoder.yudao.module.energy.controller.admin.vehicle;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.energy.controller.admin.vehicle.vo.EnergyVehiclePageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.vehicle.vo.EnergyVehicleRespVO;
import cn.iocoder.yudao.module.energy.controller.admin.vehicle.vo.EnergyVehicleSaveReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.customer.EnergyCustomerDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.device.EnergyDeviceDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.project.EnergyProjectDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.vehicle.EnergyVehicleDO;
import cn.iocoder.yudao.module.energy.service.customer.EnergyCustomerService;
import cn.iocoder.yudao.module.energy.service.device.EnergyDeviceService;
import cn.iocoder.yudao.module.energy.service.project.EnergyProjectService;
import cn.iocoder.yudao.module.energy.service.vehicle.EnergyVehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin - Energy vehicle")
@RestController
@RequestMapping("/energy/vehicle")
@Validated
public class EnergyVehicleController {

    @Resource
    private EnergyVehicleService vehicleService;
    @Resource
    private EnergyDeviceService deviceService;
    @Resource
    private EnergyCustomerService customerService;
    @Resource
    private EnergyProjectService projectService;

    @PostMapping("/create")
    @Operation(summary = "Create vehicle")
    @PreAuthorize("@ss.hasPermission('energy:vehicle:create')")
    public CommonResult<Long> createVehicle(@Valid @RequestBody EnergyVehicleSaveReqVO createReqVO) {
        return success(vehicleService.createVehicle(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "Update vehicle")
    @PreAuthorize("@ss.hasPermission('energy:vehicle:update')")
    public CommonResult<Boolean> updateVehicle(@Valid @RequestBody EnergyVehicleSaveReqVO updateReqVO) {
        vehicleService.updateVehicle(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete vehicle")
    @Parameter(name = "id", description = "ID", required = true)
    @PreAuthorize("@ss.hasPermission('energy:vehicle:delete')")
    public CommonResult<Boolean> deleteVehicle(@RequestParam("id") Long id) {
        vehicleService.deleteVehicle(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "Get vehicle")
    @PreAuthorize("@ss.hasPermission('energy:vehicle:query')")
    public CommonResult<EnergyVehicleRespVO> getVehicle(@RequestParam("id") Long id) {
        EnergyVehicleRespVO respVO = BeanUtils.toBean(vehicleService.getVehicle(id), EnergyVehicleRespVO.class);
        fillResourceNames(Collections.singletonList(respVO));
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "Get vehicle page")
    @PreAuthorize("@ss.hasPermission('energy:vehicle:query')")
    public CommonResult<PageResult<EnergyVehicleRespVO>> getVehiclePage(@Valid EnergyVehiclePageReqVO pageReqVO) {
        PageResult<EnergyVehicleDO> pageResult = vehicleService.getVehiclePage(pageReqVO);
        PageResult<EnergyVehicleRespVO> respPageResult = BeanUtils.toBean(pageResult, EnergyVehicleRespVO.class);
        fillResourceNames(respPageResult.getList());
        return success(respPageResult);
    }

    private void fillResourceNames(List<EnergyVehicleRespVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Map<Long, EnergyDeviceDO> deviceMap = deviceService.getDeviceSimpleList(null, null).stream()
                .collect(Collectors.toMap(EnergyDeviceDO::getId, Function.identity(), (first, second) -> first));
        Map<Long, EnergyCustomerDO> customerMap = customerService.getCustomerSimpleList().stream()
                .collect(Collectors.toMap(EnergyCustomerDO::getId, Function.identity(), (first, second) -> first));
        Map<Long, EnergyProjectDO> projectMap = projectService.getProjectSimpleList(null).stream()
                .collect(Collectors.toMap(EnergyProjectDO::getId, Function.identity(), (first, second) -> first));

        list.forEach(vehicle -> {
            EnergyDeviceDO device = deviceMap.get(vehicle.getDeviceId());
            if (device != null) {
                vehicle.setDeviceName(device.getDeviceName());
                vehicle.setDeviceNo(device.getDeviceNo());
                vehicle.setGatewaySn(device.getGatewaySn());
                vehicle.setMeterNo(device.getMeterNo());
            }
            EnergyCustomerDO customer = customerMap.get(vehicle.getCustomerId());
            if (customer != null) {
                vehicle.setCustomerName(customer.getName());
            }
            EnergyProjectDO project = projectMap.get(vehicle.getProjectId());
            if (project != null) {
                vehicle.setProjectName(project.getName());
            }
        });
    }

}
