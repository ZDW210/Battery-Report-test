package cn.iocoder.yudao.module.energy.controller.app.session;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.energy.controller.admin.session.vo.EnergyChargeSessionRespVO;
import cn.iocoder.yudao.module.energy.controller.app.device.vo.AppEnergyDeviceListReqVO;
import cn.iocoder.yudao.module.energy.controller.app.session.vo.AppEnergyScanDeviceReqVO;
import cn.iocoder.yudao.module.energy.controller.app.session.vo.AppEnergyScanDeviceRespVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.customer.EnergyCustomerDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.device.EnergyDeviceDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.pricing.EnergyPricingRuleDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.session.EnergyChargeSessionDO;
import cn.iocoder.yudao.module.energy.dal.mysql.customer.EnergyCustomerMapper;
import cn.iocoder.yudao.module.energy.dal.mysql.pricing.EnergyPricingRuleMapper;
import cn.iocoder.yudao.module.energy.service.app.EnergyAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "User App - Energy charging session")
@RestController
@RequestMapping("/energy/charging-sessions")
@Validated
public class AppEnergyChargingSessionController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private EnergyAppService energyAppService;
    @Resource
    private EnergyCustomerMapper customerMapper;
    @Resource
    private EnergyPricingRuleMapper pricingRuleMapper;

    @GetMapping
    @Operation(summary = "Get charging session list")
    public CommonResult<List<Map<String, Object>>> getChargingSessionList(Long deviceId, Long customerId,
                                                                          String status, String from, String to,
                                                                          Integer limit) {
        List<EnergyChargeSessionDO> sessions = energyAppService.getChargeSessionList(deviceId, customerId,
                parseStatuses(status), parseDateTime(from), parseDateTime(to), limit);
        return success(sessions.stream().map(this::buildSessionMap).toList());
    }

    @PostMapping("/scan/verify")
    @Operation(summary = "Verify scanned vehicle/device for current WeChat user")
    public CommonResult<AppEnergyScanDeviceRespVO> verifyScanDevice(
            @Valid @RequestBody AppEnergyScanDeviceReqVO reqVO) {
        return success(energyAppService.verifyScanDevice(reqVO.getScanText(), reqVO.getAuthType(), reqVO.getCardNo()));
    }

    @PostMapping("/scan/discharge")
    @Operation(summary = "Start discharge session by scanned vehicle/device")
    public CommonResult<EnergyChargeSessionRespVO> startDischargeByScan(
            @Valid @RequestBody AppEnergyScanDeviceReqVO reqVO) {
        return success(BeanUtils.toBean(energyAppService.startDischargeByScan(reqVO.getScanText(), reqVO.getAuthType(), reqVO.getCardNo()),
                EnergyChargeSessionRespVO.class));
    }

    @GetMapping("/revenue-overview")
    @Operation(summary = "Get revenue overview")
    public CommonResult<Map<String, Object>> getRevenueOverview() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        List<EnergyChargeSessionDO> todaySessions = energyAppService.getChargeSessionList(null, null,
                parseStatuses("COMPLETED"), todayStart, now, 500);
        List<EnergyChargeSessionDO> monthSessions = energyAppService.getChargeSessionList(null, null,
                parseStatuses("COMPLETED"), monthStart, now, 500);
        List<EnergyChargeSessionDO> activeSessions = energyAppService.getChargeSessionList(null, null,
                parseStatuses("ACTIVE"), null, null, 500);
        long onlineDevices = energyAppService.getDeviceList(new AppEnergyDeviceListReqVO()).stream()
                .filter(device -> Integer.valueOf(0).equals(device.getStatus()))
                .count();
        BigDecimal rentalRate = onlineDevices == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(activeSessions.size())
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(onlineDevices), 0, RoundingMode.HALF_UP);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("todayIncome", sumFee(todaySessions));
        result.put("monthIncome", sumFee(monthSessions));
        result.put("activeSessions", activeSessions.size());
        result.put("rentalRate", rentalRate);
        return success(result);
    }

    @GetMapping("/today-energy")
    @Operation(summary = "Get today's charge and discharge energy")
    public CommonResult<Map<String, Object>> getTodayEnergy() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        List<EnergyChargeSessionDO> sessions = energyAppService.getChargeSessionList(null, null,
                parseStatuses("COMPLETED"), todayStart, now, 500);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("todayCharge", sumEnergy(sessions, 0));
        result.put("todayDischarge", sumEnergy(sessions, 1));
        return success(result);
    }

    private Map<String, Object> buildSessionMap(EnergyChargeSessionDO session) {
        EnergyDeviceDO device = energyAppService.getAuthorizedDevice(session.getDeviceId());
        EnergyCustomerDO customer = session.getCustomerId() == null ? null : customerMapper.selectById(session.getCustomerId());
        EnergyPricingRuleDO rule = session.getPricingRuleId() == null ? null : pricingRuleMapper.selectById(session.getPricingRuleId());
        boolean discharge = Integer.valueOf(1).equals(session.getSessionType());
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", String.valueOf(session.getId()));
        row.put("sessionNo", session.getSessionNo());
        row.put("deviceId", String.valueOf(session.getDeviceId()));
        row.put("customerId", session.getCustomerId() == null ? null : String.valueOf(session.getCustomerId()));
        row.put("pricingRuleId", session.getPricingRuleId() == null ? null : String.valueOf(session.getPricingRuleId()));
        row.put("sessionType", session.getSessionType());
        row.put("startTime", session.getStartTime());
        row.put("endTime", session.getEndTime());
        row.put("startEpi", discharge ? null : defaultZero(session.getStartEnergy()));
        row.put("startEpe", null);
        row.put("endEpi", discharge ? null : session.getEndEnergy());
        row.put("endEpe", null);
        row.put("totalEnergy", session.getTotalEnergy());
        row.put("totalTimeFee", session.getTimeFee());
        row.put("totalEnergyFee", session.getEnergyFee());
        row.put("totalFee", session.getTotalFee());
        row.put("durationMinutes", session.getDurationMinutes());
        row.put("status", toAppStatus(session.getStatus()));
        row.put("device", buildDeviceMap(device));
        row.put("customer", buildCustomerMap(customer));
        row.put("rule", buildRuleMap(rule));
        return row;
    }

    private Map<String, Object> buildDeviceMap(EnergyDeviceDO device) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", String.valueOf(device.getId()));
        row.put("name", device.getDeviceName());
        row.put("deviceNo", device.getDeviceNo());
        row.put("meterNo", device.getMeterNo());
        return row;
    }

    private Map<String, Object> buildCustomerMap(EnergyCustomerDO customer) {
        if (customer == null) {
            return null;
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", String.valueOf(customer.getId()));
        row.put("name", customer.getName());
        return row;
    }

    private Map<String, Object> buildRuleMap(EnergyPricingRuleDO rule) {
        if (rule == null) {
            return null;
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("timeRate", rule.getTimeRate());
        row.put("energyRate", rule.getEnergyRate());
        return row;
    }

    private Collection<Integer> parseStatuses(String status) {
        if (status == null || status.isBlank()) {
            return List.of();
        }
        return switch (status.trim().toUpperCase()) {
            case "ACTIVE", "RUNNING" -> List.of(0);
            case "COMPLETED" -> List.of(1, 3);
            case "ABNORMAL" -> List.of(2);
            case "SETTLED" -> List.of(3);
            default -> {
                try {
                    yield List.of(Integer.valueOf(status));
                } catch (NumberFormatException ignored) {
                    yield List.of();
                }
            }
        };
    }

    private String toAppStatus(Integer status) {
        if (status == null) {
            return "UNKNOWN";
        }
        return switch (status) {
            case 0 -> "ACTIVE";
            case 1 -> "COMPLETED";
            case 2 -> "ABNORMAL";
            case 3 -> "SETTLED";
            default -> String.valueOf(status);
        };
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

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal sumFee(List<EnergyChargeSessionDO> sessions) {
        return sessions.stream()
                .map(EnergyChargeSessionDO::getTotalFee)
                .map(this::defaultZero)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal sumEnergy(List<EnergyChargeSessionDO> sessions, Integer sessionType) {
        return sessions.stream()
                .filter(session -> sessionType.equals(session.getSessionType()))
                .map(EnergyChargeSessionDO::getTotalEnergy)
                .map(this::defaultZero)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(3, RoundingMode.HALF_UP);
    }

}
