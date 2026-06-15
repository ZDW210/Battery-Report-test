package cn.iocoder.yudao.module.energy.service.session;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.energy.controller.admin.device.vo.EnergyDeviceControlReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.device.vo.EnergyDeviceControlRespVO;
import cn.iocoder.yudao.module.energy.controller.admin.session.vo.EnergyChargeSessionPageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.session.vo.EnergyChargeSessionStartReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.session.vo.EnergyChargeSessionStopReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.telemetry.vo.EnergyTelemetryPageReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.device.EnergyDeviceDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.pricing.EnergyPricingRuleDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.session.EnergyChargeSessionDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.telemetry.EnergyTelemetryDO;
import cn.iocoder.yudao.module.energy.dal.mysql.session.EnergyChargeSessionMapper;
import cn.iocoder.yudao.module.energy.service.device.EnergyDeviceControlService;
import cn.iocoder.yudao.module.energy.service.device.EnergyDeviceService;
import cn.iocoder.yudao.module.energy.service.pricing.EnergyPricingRuleService;
import cn.iocoder.yudao.module.energy.service.telemetry.EnergyTelemetryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.energy.enums.ErrorCodeConstants.*;

@Service
@Validated
public class EnergyChargeSessionServiceImpl implements EnergyChargeSessionService {

    private static final int STATUS_RUNNING = 0;
    private static final int STATUS_COMPLETED = 1;
    private static final int STATUS_ABNORMAL = 2;
    private static final int STATUS_SETTLED = 3;

    @Resource
    private EnergyChargeSessionMapper chargeSessionMapper;
    @Resource
    private EnergyDeviceService deviceService;
    @Resource
    private EnergyPricingRuleService pricingRuleService;
    @Resource
    private EnergyTelemetryService telemetryService;
    @Resource
    private EnergyDeviceControlService deviceControlService;

    @Override
    public PageResult<EnergyChargeSessionDO> getChargeSessionPage(EnergyChargeSessionPageReqVO pageReqVO) {
        return chargeSessionMapper.selectPage(pageReqVO);
    }

    @Override
    public EnergyChargeSessionDO getChargeSession(Long id) {
        EnergyChargeSessionDO session = chargeSessionMapper.selectById(id);
        if (session == null) {
            throw exception(CHARGE_SESSION_NOT_EXISTS);
        }
        return session;
    }

    @Override
    public EnergyChargeSessionDO startChargeSession(EnergyChargeSessionStartReqVO reqVO, Long operatorUserId) {
        EnergyDeviceDO device = deviceService.validateDeviceExists(reqVO.getDeviceId());
        if (device.getCustomerId() == null) {
            throw exception(CHARGE_SESSION_DEVICE_CUSTOMER_REQUIRED);
        }
        if (chargeSessionMapper.selectRunningByDeviceId(reqVO.getDeviceId()) != null) {
            throw exception(CHARGE_SESSION_RUNNING_EXISTS);
        }
        LocalDateTime now = LocalDateTime.now();
        EnergyPricingRuleDO pricingRule = getPricingRuleForStart(reqVO, device, now);
        if (pricingRule == null) {
            throw exception(CHARGE_SESSION_NO_PRICING_RULE);
        }
        controlSwitchOrThrow(reqVO.getDeviceId(), "1", "开始充放电任务自动合闸", operatorUserId);
        EnergyChargeSessionDO session = EnergyChargeSessionDO.builder()
                .sessionNo(buildSessionNo(now))
                .deviceId(reqVO.getDeviceId())
                .customerId(device.getCustomerId())
                .pricingRuleId(pricingRule.getId())
                .sessionType(reqVO.getSessionType())
                .startTime(now)
                .startEnergy(getLatestEnergy(reqVO.getDeviceId()))
                .status(STATUS_RUNNING)
                .build();
        chargeSessionMapper.insert(session);
        return session;
    }

    @Override
    public EnergyChargeSessionDO stopChargeSession(EnergyChargeSessionStopReqVO reqVO, Long operatorUserId) {
        EnergyChargeSessionDO session = getChargeSession(reqVO.getSessionId());
        if (!STATUS_RUNNING_EQUALS(session)) {
            throw exception(CHARGE_SESSION_NOT_RUNNING);
        }
        LocalDateTime now = LocalDateTime.now();
        BigDecimal startEnergy = defaultZero(session.getStartEnergy());
        BigDecimal endEnergy = reqVO.getEndEnergy() != null ? reqVO.getEndEnergy() : getLatestEnergy(session.getDeviceId());
        endEnergy = defaultZero(endEnergy);
        if (endEnergy.compareTo(startEnergy) < 0) {
            throw exception(CHARGE_SESSION_ENERGY_INVALID);
        }
        BigDecimal totalEnergy = endEnergy.subtract(startEnergy).setScale(3, RoundingMode.HALF_UP);
        int durationMinutes = Math.toIntExact(Math.max(0L, Duration.between(session.getStartTime(), now).toMinutes()));
        EnergyPricingRuleDO pricingRule = pricingRuleService.validatePricingRuleExists(session.getPricingRuleId());
        BigDecimal energyFee = totalEnergy.multiply(defaultZero(pricingRule.getEnergyRate())).setScale(2, RoundingMode.HALF_UP);
        BigDecimal timeFee = BigDecimal.valueOf(durationMinutes)
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP)
                .multiply(defaultZero(pricingRule.getTimeRate()))
                .setScale(2, RoundingMode.HALF_UP);
        controlSwitchOrThrow(session.getDeviceId(), "0", "结束充放电任务自动开闸", operatorUserId);
        EnergyChargeSessionDO updateObj = EnergyChargeSessionDO.builder()
                .id(session.getId())
                .endTime(now)
                .endEnergy(endEnergy)
                .totalEnergy(totalEnergy)
                .durationMinutes(durationMinutes)
                .energyFee(energyFee)
                .timeFee(timeFee)
                .totalFee(energyFee.add(timeFee).setScale(2, RoundingMode.HALF_UP))
                .status(STATUS_COMPLETED)
                .build();
        chargeSessionMapper.updateById(updateObj);
        return getChargeSession(session.getId());
    }

    @Override
    public EnergyChargeSessionDO settleChargeSession(Long sessionId) {
        EnergyChargeSessionDO session = getChargeSession(sessionId);
        if (session.getStatus() == null || (session.getStatus() != STATUS_COMPLETED && session.getStatus() != STATUS_ABNORMAL)) {
            throw exception(CHARGE_SESSION_NOT_COMPLETED);
        }
        chargeSessionMapper.updateById(EnergyChargeSessionDO.builder()
                .id(sessionId)
                .status(STATUS_SETTLED)
                .build());
        return getChargeSession(sessionId);
    }

    private EnergyPricingRuleDO getPricingRuleForStart(EnergyChargeSessionStartReqVO reqVO, EnergyDeviceDO device,
                                                       LocalDateTime now) {
        if (reqVO.getPricingRuleId() == null) {
            return pricingRuleService.getMatchedPricingRule(reqVO.getDeviceId(), formatDateTime(now));
        }
        EnergyPricingRuleDO pricingRule = pricingRuleService.validatePricingRuleExists(reqVO.getPricingRuleId());
        validatePricingRuleMatchesDevice(pricingRule, device);
        return pricingRule;
    }

    private void validatePricingRuleMatchesDevice(EnergyPricingRuleDO pricingRule, EnergyDeviceDO device) {
        if (Objects.equals(pricingRule.getDeviceId(), device.getId())) {
            return;
        }
        if (pricingRule.getProjectId() != null && Objects.equals(pricingRule.getProjectId(), device.getProjectId())) {
            return;
        }
        if (pricingRule.getCustomerId() != null && Objects.equals(pricingRule.getCustomerId(), device.getCustomerId())) {
            return;
        }
        throw exception(CHARGE_SESSION_PRICING_RULE_NOT_MATCH);
    }

    private BigDecimal getLatestEnergy(Long deviceId) {
        EnergyTelemetryPageReqVO reqVO = new EnergyTelemetryPageReqVO();
        reqVO.setDeviceId(deviceId);
        PageResult<EnergyTelemetryDO> page = telemetryService.getTelemetryPage(reqVO);
        if (page.getList() == null || page.getList().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return defaultZero(page.getList().get(0).getEpi());
    }

    private void controlSwitchOrThrow(Long deviceId, String switchValue, String remark, Long operatorUserId) {
        EnergyDeviceControlReqVO controlReqVO = new EnergyDeviceControlReqVO();
        controlReqVO.setDeviceId(deviceId);
        controlReqVO.setMethod("SWITCH");
        controlReqVO.setValue(Map.of("Switch", switchValue));
        controlReqVO.setRemark(remark);
        EnergyDeviceControlRespVO controlRespVO = deviceControlService.controlDevice(controlReqVO, operatorUserId);
        if (!Boolean.TRUE.equals(controlRespVO.getSuccess())) {
            throw exception(CHARGE_SESSION_DEVICE_CONTROL_FAILED, controlRespVO.getMessage());
        }
    }

    private String buildSessionNo(LocalDateTime now) {
        return "SES" + DateUtil.format(now, "yyyyMMddHHmmssSSS");
    }

    private String formatDateTime(LocalDateTime time) {
        return DateUtil.format(time, DatePattern.NORM_DATETIME_PATTERN);
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private boolean STATUS_RUNNING_EQUALS(EnergyChargeSessionDO session) {
        return session.getStatus() != null && session.getStatus() == STATUS_RUNNING;
    }

}
