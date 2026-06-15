package cn.iocoder.yudao.module.energy.controller.admin.telemetry;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.energy.controller.admin.telemetry.vo.EnergyTelemetryDailyStatReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.telemetry.vo.EnergyTelemetryDailyStatRespVO;
import cn.iocoder.yudao.module.energy.controller.admin.telemetry.vo.EnergyTelemetryPageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.telemetry.vo.EnergyTelemetryRespVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.device.EnergyDeviceDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.telemetry.EnergyTelemetryDO;
import cn.iocoder.yudao.module.energy.service.device.EnergyDeviceService;
import cn.iocoder.yudao.module.energy.service.telemetry.EnergyTelemetryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 移动储能实时采集数据")
@RestController
@RequestMapping("/energy/telemetry")
@Validated
public class EnergyTelemetryController {

    @Resource
    private EnergyTelemetryService telemetryService;
    @Resource
    private EnergyDeviceService deviceService;

    @GetMapping("/page")
    @Operation(summary = "获得实时采集数据分页")
    @PreAuthorize("@ss.hasPermission('energy:telemetry:query')")
    public CommonResult<PageResult<EnergyTelemetryRespVO>> getTelemetryPage(
            @Valid EnergyTelemetryPageReqVO pageReqVO) {
        PageResult<EnergyTelemetryDO> pageResult = telemetryService.getTelemetryPage(pageReqVO);
        PageResult<EnergyTelemetryRespVO> respPageResult = BeanUtils.toBean(pageResult, EnergyTelemetryRespVO.class);
        fillDeviceNames(respPageResult.getList());
        return success(respPageResult);
    }

    @GetMapping("/chart")
    @Operation(summary = "获得实时采集曲线数据")
    @PreAuthorize("@ss.hasPermission('energy:telemetry:query')")
    public CommonResult<List<EnergyTelemetryRespVO>> getTelemetryChart(
            @Valid EnergyTelemetryPageReqVO pageReqVO,
            @RequestParam(value = "limit", required = false, defaultValue = "2000") Integer limit) {
        List<EnergyTelemetryRespVO> list = BeanUtils.toBean(telemetryService.getTelemetryList(pageReqVO, limit),
                EnergyTelemetryRespVO.class);
        fillDeviceNames(list);
        return success(list);
    }

    @GetMapping("/daily-stat")
    @Operation(summary = "获得实时采集逐日极值数据")
    @PreAuthorize("@ss.hasPermission('energy:telemetry:query')")
    public CommonResult<List<EnergyTelemetryDailyStatRespVO>> getTelemetryDailyStat(
            @Valid EnergyTelemetryDailyStatReqVO reqVO) {
        return success(telemetryService.getDailyStatList(reqVO));
    }

    private void fillDeviceNames(List<EnergyTelemetryRespVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Map<Long, EnergyDeviceDO> deviceMap = deviceService.getDeviceSimpleList(null, null).stream()
                .collect(Collectors.toMap(EnergyDeviceDO::getId, Function.identity(), (first, second) -> first));
        list.forEach(item -> {
            EnergyDeviceDO device = deviceMap.get(item.getDeviceId());
            if (device != null) {
                item.setDeviceName(device.getDeviceName());
                item.setDeviceNo(device.getDeviceNo());
            }
        });
    }

}
