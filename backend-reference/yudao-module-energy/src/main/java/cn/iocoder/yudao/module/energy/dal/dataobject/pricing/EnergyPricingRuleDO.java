package cn.iocoder.yudao.module.energy.dal.dataobject.pricing;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 移动储能计费规则 DO。
 */
@TableName("energy_pricing_rule")
@KeySequence("energy_pricing_rule_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyPricingRuleDO extends TenantBaseDO {

    /**
     * 计费规则编号
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 客户编号
     */
    private Long customerId;
    /**
     * 项目编号
     */
    private Long projectId;
    /**
     * 设备编号
     */
    private Long deviceId;
    /**
     * 时间单价
     */
    private BigDecimal timeRate;
    /**
     * 电量单价
     */
    private BigDecimal energyRate;
    /**
     * 生效开始时间
     */
    private LocalDateTime effectiveStart;
    /**
     * 生效结束时间
     */
    private LocalDateTime effectiveEnd;
    /**
     * 状态：0 启用，1 停用
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
