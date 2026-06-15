package cn.iocoder.yudao.module.energy.controller.admin.alarm;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.energy.controller.admin.alarm.vo.EnergyAlarmHandleReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.alarm.vo.EnergyAlarmPageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.alarm.vo.EnergyAlarmRespVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.alarm.EnergyAlarmDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.device.EnergyDeviceDO;
import cn.iocoder.yudao.module.energy.service.alarm.EnergyAlarmService;
import cn.iocoder.yudao.module.energy.service.device.EnergyDeviceService;
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
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 移动储能告警")
@RestController
@RequestMapping("/energy/alarm")
@Validated
public class EnergyAlarmController {

    @Resource
    private EnergyAlarmService alarmService;
    @Resource
    private EnergyDeviceService deviceService;

    @GetMapping("/get")
    @Operation(summary = "获得告警")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('energy:alarm:query')")
    public CommonResult<EnergyAlarmRespVO> getAlarm(@RequestParam("id") Long id) {
        EnergyAlarmDO alarm = alarmService.getAlarm(id);
        EnergyAlarmRespVO respVO = BeanUtils.toBean(alarm, EnergyAlarmRespVO.class);
        fillDeviceNames(Collections.singletonList(respVO));
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得告警分页")
    @PreAuthorize("@ss.hasPermission('energy:alarm:query')")
    public CommonResult<PageResult<EnergyAlarmRespVO>> getAlarmPage(@Valid EnergyAlarmPageReqVO pageReqVO) {
        PageResult<EnergyAlarmDO> pageResult = alarmService.getAlarmPage(pageReqVO);
        PageResult<EnergyAlarmRespVO> respPageResult = BeanUtils.toBean(pageResult, EnergyAlarmRespVO.class);
        fillDeviceNames(respPageResult.getList());
        return success(respPageResult);
    }

    @PutMapping("/ack")
    @Operation(summary = "确认告警")
    @PreAuthorize("@ss.hasPermission('energy:alarm:ack')")
    public CommonResult<Boolean> ackAlarm(@Valid @RequestBody EnergyAlarmHandleReqVO reqVO) {
        alarmService.ackAlarm(reqVO.getId(), getLoginUserId(), reqVO.getRemark());
        return success(true);
    }

    @PutMapping("/close")
    @Operation(summary = "关闭告警")
    @PreAuthorize("@ss.hasPermission('energy:alarm:close')")
    public CommonResult<Boolean> closeAlarm(@Valid @RequestBody EnergyAlarmHandleReqVO reqVO) {
        alarmService.closeAlarm(reqVO.getId(), getLoginUserId(), reqVO.getRemark());
        return success(true);
    }

    private void fillDeviceNames(List<EnergyAlarmRespVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Long> deviceIds = list.stream()
                .map(EnergyAlarmRespVO::getDeviceId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        for (Long deviceId : deviceIds) {
            EnergyDeviceDO device = deviceService.getDevice(deviceId);
            if (device == null) {
                continue;
            }
            list.stream()
                    .filter(alarm -> Objects.equals(alarm.getDeviceId(), deviceId))
                    .forEach(alarm -> alarm.setDeviceName(device.getDeviceName()));
        }
    }

}
