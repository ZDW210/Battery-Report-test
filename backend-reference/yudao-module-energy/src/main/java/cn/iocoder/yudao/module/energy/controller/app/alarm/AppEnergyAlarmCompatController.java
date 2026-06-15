package cn.iocoder.yudao.module.energy.controller.app.alarm;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.energy.dal.dataobject.alarm.EnergyAlarmDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.device.EnergyDeviceDO;
import cn.iocoder.yudao.module.energy.service.app.EnergyAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "User App - Energy alarm compatibility")
@RestController
@RequestMapping("/energy/alarms")
@Validated
public class AppEnergyAlarmCompatController {

    @Resource
    private EnergyAppService energyAppService;

    @GetMapping
    @Operation(summary = "Get authorized alarm list")
    public CommonResult<Map<String, Object>> getAlarms(Long deviceId, String meterNo, Integer limit) {
        List<Map<String, Object>> rows = energyAppService.getAlarmList(resolveDeviceId(deviceId, meterNo), limit).stream()
                .map(this::buildAlarmMap)
                .toList();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", rows);
        result.put("total", rows.size());
        return success(result);
    }

    private Long resolveDeviceId(Long deviceId, String meterNo) {
        if (deviceId != null) {
            return deviceId;
        }
        if (meterNo != null && meterNo.matches("\\d+")) {
            return Long.valueOf(meterNo);
        }
        return null;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get authorized alarm detail")
    public CommonResult<Map<String, Object>> getAlarm(@PathVariable("id") Long id) {
        return success(buildAlarmMap(energyAppService.getAlarm(id)));
    }

    private Map<String, Object> buildAlarmMap(EnergyAlarmDO alarm) {
        EnergyDeviceDO device = energyAppService.getAuthorizedDevice(alarm.getDeviceId());
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", String.valueOf(alarm.getId()));
        row.put("alarm_no", alarm.getAlarmNo());
        row.put("device_id", String.valueOf(alarm.getDeviceId()));
        row.put("meter_no", device.getMeterNo());
        row.put("project_name", device.getProjectId() == null ? null : String.valueOf(device.getProjectId()));
        row.put("code", alarm.getCode());
        row.put("level", alarm.getLevel() == null ? null : String.valueOf(alarm.getLevel()));
        row.put("status", alarm.getStatus());
        row.put("title_zh", alarm.getTitle());
        row.put("message_zh", alarm.getContent());
        row.put("created_at", alarm.getOccurTime());
        return row;
    }

}
