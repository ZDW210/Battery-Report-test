package cn.iocoder.yudao.module.energy.controller.app.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.energy.controller.app.device.vo.AppEnergyDeviceListReqVO;
import cn.iocoder.yudao.module.energy.controller.app.device.vo.AppEnergyDeviceRespVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.alarm.EnergyAlarmDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.device.EnergyDeviceDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.telemetry.EnergyTelemetryDO;
import cn.iocoder.yudao.module.energy.service.app.EnergyAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "User App - Energy device compatibility")
@RestController
@RequestMapping("/energy/devices")
@Validated
public class AppEnergyDeviceCompatController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private EnergyAppService energyAppService;

    @GetMapping("/{id}")
    @Operation(summary = "Get authorized device detail")
    public CommonResult<Map<String, Object>> getDevice(@PathVariable("id") Long id) {
        EnergyDeviceDO device = energyAppService.getAuthorizedDevice(id);
        EnergyTelemetryDO latest = energyAppService.getLatestTelemetry(id);
        List<EnergyAlarmDO> alarms = energyAppService.getAlarmList(id, 5);
        Map<String, Object> result = buildDeviceMap(device, latest);
        result.put("recentAlarms", alarms.stream().map(alarm -> buildAlarmMap(alarm, device)).toList());
        return success(result);
    }

    @GetMapping("/{id}/latest")
    @Operation(summary = "Get latest authorized device telemetry")
    public CommonResult<Map<String, Object>> getDeviceLatest(@PathVariable("id") Long id) {
        EnergyTelemetryDO latest = energyAppService.getLatestTelemetry(id);
        return success(latest == null ? null : buildTelemetryMap(latest));
    }

    @GetMapping("/{id}/readings")
    @Operation(summary = "Get authorized device telemetry readings")
    public CommonResult<Map<String, Object>> getDeviceReadings(@PathVariable("id") Long id, String from, String to,
                                                              Integer limit) {
        List<EnergyTelemetryDO> readings = energyAppService.getTelemetryList(id, parseDateTime(from), parseDateTime(to), limit);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("data", readings.stream().map(this::buildTelemetryMap).toList());
        result.put("total", readings.size());
        return success(result);
    }

    @GetMapping("/energy-delta")
    @Operation(summary = "Get authorized device energy delta summary")
    public CommonResult<Map<String, Object>> getEnergyDelta(String from, String to) {
        LocalDateTime fromTime = parseDateTime(from);
        LocalDateTime toTime = parseDateTime(to);
        List<AppEnergyDeviceRespVO> devices = energyAppService.getDeviceList(new AppEnergyDeviceListReqVO());
        List<Map<String, Object>> rows = devices.stream()
                .map(device -> buildDeviceDelta(device, fromTime, toTime))
                .toList();
        BigDecimal totalCharge = rows.stream()
                .map(row -> (BigDecimal) row.get("chargeEnergy"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCharge", totalCharge);
        summary.put("totalNet", totalCharge);
        summary.put("deviceCount", devices.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("from", from != null ? from : LocalDate.now().toString());
        result.put("to", to != null ? to : LocalDate.now().toString());
        result.put("devices", rows);
        result.put("summary", summary);
        return success(result);
    }

    private Map<String, Object> buildDeviceMap(EnergyDeviceDO device, EnergyTelemetryDO latest) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", String.valueOf(device.getId()));
        row.put("deviceName", device.getDeviceName());
        row.put("name", device.getDeviceName());
        row.put("deviceNo", device.getDeviceNo());
        row.put("deviceType", device.getDeviceType());
        row.put("gatewaySn", device.getGatewaySn());
        row.put("meterSn", device.getMeterSn());
        row.put("meterNo", device.getMeterNo());
        row.put("customerId", device.getCustomerId() == null ? null : String.valueOf(device.getCustomerId()));
        row.put("projectId", device.getProjectId() == null ? null : String.valueOf(device.getProjectId()));
        row.put("status", device.getStatus());
        row.put("runMode", device.getRunMode());
        row.put("latitude", device.getLatitude());
        row.put("longitude", device.getLongitude());
        row.put("lastPower", device.getLastPower());
        row.put("lastReadingTime", device.getLastReadingTime());
        if (latest != null) {
            row.put("lastUa", latest.getUa());
            row.put("lastUb", latest.getUb());
            row.put("lastUc", latest.getUc());
            row.put("lastIa", latest.getIa());
            row.put("lastIb", latest.getIb());
            row.put("lastIc", latest.getIc());
            row.put("lastP", latest.getP());
            row.put("lastPf", latest.getPf());
            row.put("lastEpi", latest.getEpi());
            row.put("lastReadingTime", latest.getCollectTime());
        }
        return row;
    }

    private Map<String, Object> buildDeviceDelta(AppEnergyDeviceRespVO device, LocalDateTime from, LocalDateTime to) {
        BigDecimal chargeEnergy = calculateEpiDelta(device.getId(), from, to);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("deviceId", String.valueOf(device.getId()));
        row.put("deviceName", device.getDeviceName());
        row.put("meterNo", device.getDeviceNo());
        row.put("chargeEnergy", chargeEnergy);
        row.put("netEnergy", chargeEnergy);
        return row;
    }

    private BigDecimal calculateEpiDelta(Long deviceId, LocalDateTime from, LocalDateTime to) {
        List<EnergyTelemetryDO> readings = energyAppService.getTelemetryList(deviceId, from, to, 2000);
        BigDecimal start = null;
        BigDecimal end = null;
        for (EnergyTelemetryDO reading : readings) {
            if (reading.getEpi() == null) {
                continue;
            }
            if (start == null) {
                start = reading.getEpi();
            }
            end = reading.getEpi();
        }
        if (start == null || end == null || end.compareTo(start) < 0) {
            return BigDecimal.ZERO;
        }
        return end.subtract(start);
    }

    private Map<String, Object> buildTelemetryMap(EnergyTelemetryDO telemetry) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", String.valueOf(telemetry.getId()));
        row.put("deviceId", String.valueOf(telemetry.getDeviceId()));
        row.put("gatewaySn", telemetry.getGatewaySn());
        row.put("meterSn", telemetry.getMeterSn());
        row.put("meterNo", telemetry.getMeterNo());
        row.put("create_time", telemetry.getCollectTime());
        row.put("collectTime", telemetry.getCollectTime());
        row.put("ts", telemetry.getTimestamp());
        row.put("source", telemetry.getSource());
        row.put("state", telemetry.getState());
        row.put("pa", telemetry.getPa());
        row.put("pb", telemetry.getPb());
        row.put("pc", telemetry.getPc());
        row.put("ua", telemetry.getUa());
        row.put("ub", telemetry.getUb());
        row.put("uc", telemetry.getUc());
        row.put("ia", telemetry.getIa());
        row.put("ib", telemetry.getIb());
        row.put("ic", telemetry.getIc());
        row.put("p", telemetry.getP());
        row.put("pf", telemetry.getPf());
        row.put("epi", telemetry.getEpi());
        return row;
    }

    private Map<String, Object> buildAlarmMap(EnergyAlarmDO alarm, EnergyDeviceDO device) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", String.valueOf(alarm.getId()));
        row.put("alarm_no", alarm.getAlarmNo());
        row.put("device_id", String.valueOf(alarm.getDeviceId()));
        row.put("meter_no", device == null ? null : device.getMeterNo());
        row.put("code", alarm.getCode());
        row.put("level", alarm.getLevel() == null ? null : String.valueOf(alarm.getLevel()));
        row.put("status", alarm.getStatus());
        row.put("title_zh", alarm.getTitle());
        row.put("message_zh", alarm.getContent());
        row.put("created_at", alarm.getOccurTime());
        return row;
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.contains("T") && (trimmed.endsWith("Z") || trimmed.matches(".*[+-]\\d{2}:\\d{2}$"))) {
            try {
                return OffsetDateTime.parse(trimmed)
                        .atZoneSameInstant(ZoneId.systemDefault())
                        .toLocalDateTime();
            } catch (DateTimeParseException ignored) {
                // Fallback to the local datetime parser below for legacy values.
            }
        }
        String normalized = trimmed.replace("T", " ");
        int dotIndex = normalized.indexOf('.');
        if (dotIndex > 0) {
            normalized = normalized.substring(0, dotIndex);
        }
        if (normalized.endsWith("Z")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.length() == 10) {
            normalized = normalized + " 00:00:00";
        }
        return LocalDateTime.parse(normalized, DATE_TIME_FORMATTER);
    }

}
