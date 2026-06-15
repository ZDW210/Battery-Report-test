package cn.iocoder.yudao.module.energy.service.pricing;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.energy.controller.admin.pricing.vo.EnergyPricingRulePageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.pricing.vo.EnergyPricingRuleSaveReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.pricing.EnergyPricingRuleDO;

/**
 * 移动储能计费规则 Service 接口。
 */
public interface EnergyPricingRuleService {

    Long createPricingRule(EnergyPricingRuleSaveReqVO createReqVO);

    void updatePricingRule(EnergyPricingRuleSaveReqVO updateReqVO);

    void deletePricingRule(Long id);

    EnergyPricingRuleDO getPricingRule(Long id);

    PageResult<EnergyPricingRuleDO> getPricingRulePage(EnergyPricingRulePageReqVO pageReqVO);

    EnergyPricingRuleDO validatePricingRuleExists(Long id);

    EnergyPricingRuleDO getMatchedPricingRule(Long deviceId, String billingTime);

}
