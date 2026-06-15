package cn.iocoder.yudao.module.energy.dal.dataobject.session;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 移动储能充放电会话 DO。
 */
@TableName("energy_charge_session")
@KeySequence("energy_charge_session_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyChargeSessionDO extends TenantBaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String sessionNo;

    private Long deviceId;

    private Long customerId;

    private Long pricingRuleId;

    /**
     * 会话类型：0 充电，1 放电。
     */
    private Integer sessionType;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal startEnergy;

    private BigDecimal endEnergy;

    private BigDecimal totalEnergy;

    private Integer durationMinutes;

    private BigDecimal energyFee;

    private BigDecimal timeFee;

    private BigDecimal totalFee;

    /**
     * 状态：0 进行中，1 已结束，2 异常，3 已结算。
     */
    private Integer status;

}
