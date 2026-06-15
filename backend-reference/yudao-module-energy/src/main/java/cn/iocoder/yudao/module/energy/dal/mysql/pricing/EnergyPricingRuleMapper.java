package cn.iocoder.yudao.module.energy.dal.mysql.pricing;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.energy.controller.admin.pricing.vo.EnergyPricingRulePageReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.pricing.EnergyPricingRuleDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

/**
 * 移动储能计费规则 Mapper。
 */
@Mapper
public interface EnergyPricingRuleMapper extends BaseMapperX<EnergyPricingRuleDO> {

    default PageResult<EnergyPricingRuleDO> selectPage(EnergyPricingRulePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EnergyPricingRuleDO>()
                .eqIfPresent(EnergyPricingRuleDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(EnergyPricingRuleDO::getProjectId, reqVO.getProjectId())
                .eqIfPresent(EnergyPricingRuleDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(EnergyPricingRuleDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(EnergyPricingRuleDO::getEffectiveStart, reqVO.getEffectiveStart())
                .orderByDesc(EnergyPricingRuleDO::getEffectiveStart)
                .orderByDesc(EnergyPricingRuleDO::getId));
    }

    default EnergyPricingRuleDO selectActiveDeviceRule(Long deviceId, LocalDateTime billingTime) {
        return selectActiveRule(new LambdaQueryWrapper<EnergyPricingRuleDO>()
                .eq(EnergyPricingRuleDO::getDeviceId, deviceId), billingTime);
    }

    default EnergyPricingRuleDO selectActiveProjectRule(Long projectId, LocalDateTime billingTime) {
        return selectActiveRule(new LambdaQueryWrapper<EnergyPricingRuleDO>()
                .eq(EnergyPricingRuleDO::getProjectId, projectId), billingTime);
    }

    default EnergyPricingRuleDO selectActiveCustomerRule(Long customerId, LocalDateTime billingTime) {
        return selectActiveRule(new LambdaQueryWrapper<EnergyPricingRuleDO>()
                .eq(EnergyPricingRuleDO::getCustomerId, customerId), billingTime);
    }

    private EnergyPricingRuleDO selectActiveRule(LambdaQueryWrapper<EnergyPricingRuleDO> wrapper,
                                                LocalDateTime billingTime) {
        wrapper.eq(EnergyPricingRuleDO::getStatus, 0)
                .le(EnergyPricingRuleDO::getEffectiveStart, billingTime)
                .and(query -> query
                        .isNull(EnergyPricingRuleDO::getEffectiveEnd)
                        .or()
                        .ge(EnergyPricingRuleDO::getEffectiveEnd, billingTime))
                .orderByDesc(EnergyPricingRuleDO::getEffectiveStart)
                .orderByDesc(EnergyPricingRuleDO::getId)
                .last("LIMIT 1");
        return selectOne(wrapper);
    }

}
