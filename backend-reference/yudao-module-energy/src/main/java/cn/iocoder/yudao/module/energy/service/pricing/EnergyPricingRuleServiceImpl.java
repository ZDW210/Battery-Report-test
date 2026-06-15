package cn.iocoder.yudao.module.energy.service.pricing;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.module.energy.controller.admin.pricing.vo.EnergyPricingRulePageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.pricing.vo.EnergyPricingRuleSaveReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.device.EnergyDeviceDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.pricing.EnergyPricingRuleDO;
import cn.iocoder.yudao.module.energy.dal.mysql.pricing.EnergyPricingRuleMapper;
import cn.iocoder.yudao.module.energy.service.customer.EnergyCustomerService;
import cn.iocoder.yudao.module.energy.service.device.EnergyDeviceService;
import cn.iocoder.yudao.module.energy.service.project.EnergyProjectService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.energy.enums.ErrorCodeConstants.*;

/**
 * 移动储能计费规则 Service 实现类。
 */
@Service
@Validated
public class EnergyPricingRuleServiceImpl implements EnergyPricingRuleService {

    @Resource
    private EnergyPricingRuleMapper pricingRuleMapper;
    @Resource
    private EnergyCustomerService customerService;
    @Resource
    private EnergyProjectService projectService;
    @Resource
    private EnergyDeviceService deviceService;

    @Override
    public Long createPricingRule(EnergyPricingRuleSaveReqVO createReqVO) {
        validatePricingRuleSaveData(createReqVO);
        EnergyPricingRuleDO pricingRule = buildPricingRuleDO(createReqVO);
        pricingRuleMapper.insert(pricingRule);
        return pricingRule.getId();
    }

    @Override
    public void updatePricingRule(EnergyPricingRuleSaveReqVO updateReqVO) {
        validatePricingRuleExists(updateReqVO.getId());
        validatePricingRuleSaveData(updateReqVO);
        pricingRuleMapper.updateById(buildPricingRuleDO(updateReqVO));
    }

    @Override
    public void deletePricingRule(Long id) {
        validatePricingRuleExists(id);
        pricingRuleMapper.deleteById(id);
    }

    @Override
    public EnergyPricingRuleDO getPricingRule(Long id) {
        return pricingRuleMapper.selectById(id);
    }

    @Override
    public PageResult<EnergyPricingRuleDO> getPricingRulePage(EnergyPricingRulePageReqVO pageReqVO) {
        return pricingRuleMapper.selectPage(pageReqVO);
    }

    @Override
    public EnergyPricingRuleDO validatePricingRuleExists(Long id) {
        EnergyPricingRuleDO pricingRule = pricingRuleMapper.selectById(id);
        if (pricingRule == null) {
            throw exception(PRICING_RULE_NOT_EXISTS);
        }
        return pricingRule;
    }

    @Override
    public EnergyPricingRuleDO getMatchedPricingRule(Long deviceId, String billingTime) {
        EnergyDeviceDO device = deviceService.validateDeviceExists(deviceId);
        LocalDateTime actualBillingTime = StrUtil.isBlank(billingTime)
                ? LocalDateTime.now()
                : parseDateTime(billingTime);
        EnergyPricingRuleDO deviceRule = pricingRuleMapper.selectActiveDeviceRule(deviceId, actualBillingTime);
        if (deviceRule != null) {
            return deviceRule;
        }
        if (device.getProjectId() != null) {
            EnergyPricingRuleDO projectRule = pricingRuleMapper.selectActiveProjectRule(device.getProjectId(), actualBillingTime);
            if (projectRule != null) {
                return projectRule;
            }
        }
        if (device.getCustomerId() != null) {
            return pricingRuleMapper.selectActiveCustomerRule(device.getCustomerId(), actualBillingTime);
        }
        return null;
    }

    private void validatePricingRuleSaveData(EnergyPricingRuleSaveReqVO reqVO) {
        int scopeCount = 0;
        if (reqVO.getCustomerId() != null) {
            scopeCount++;
            customerService.validateCustomerExists(reqVO.getCustomerId());
        }
        if (reqVO.getProjectId() != null) {
            scopeCount++;
            projectService.validateProjectExists(reqVO.getProjectId());
        }
        if (reqVO.getDeviceId() != null) {
            scopeCount++;
            deviceService.validateDeviceExists(reqVO.getDeviceId());
        }
        if (scopeCount != 1) {
            throw exception(PRICING_RULE_TARGET_REQUIRED);
        }
        LocalDateTime effectiveStart = parseDateTime(reqVO.getEffectiveStart());
        LocalDateTime effectiveEnd = parseDateTime(reqVO.getEffectiveEnd());
        if (effectiveEnd != null && effectiveEnd.isBefore(effectiveStart)) {
            throw exception(PRICING_RULE_TIME_INVALID);
        }
    }

    private EnergyPricingRuleDO buildPricingRuleDO(EnergyPricingRuleSaveReqVO reqVO) {
        return EnergyPricingRuleDO.builder()
                .id(reqVO.getId())
                .customerId(reqVO.getCustomerId())
                .projectId(reqVO.getProjectId())
                .deviceId(reqVO.getDeviceId())
                .timeRate(reqVO.getTimeRate())
                .energyRate(reqVO.getEnergyRate())
                .effectiveStart(parseDateTime(reqVO.getEffectiveStart()))
                .effectiveEnd(parseDateTime(reqVO.getEffectiveEnd()))
                .status(reqVO.getStatus())
                .remark(reqVO.getRemark())
                .build();
    }

    private LocalDateTime parseDateTime(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return LocalDateTimeUtil.parse(value, DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND);
        } catch (RuntimeException ex) {
            throw exception(PRICING_RULE_TIME_INVALID);
        }
    }

}
