package cn.iocoder.yudao.module.energy.dal.dataobject.telemetry;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("energy_telemetry")
@KeySequence("energy_telemetry_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyTelemetryDO extends TenantBaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long deviceId;

    private String gatewaySn;

    private String meterSn;

    private String meterNo;

    private LocalDateTime collectTime;

    private Long timestamp;

    private String source;

    private String state;

    private BigDecimal pa;

    private BigDecimal pb;

    private BigDecimal pc;

    private BigDecimal ua;

    private BigDecimal ub;

    private BigDecimal uc;

    private BigDecimal ia;

    private BigDecimal ib;

    private BigDecimal ic;

    private BigDecimal p;

    private BigDecimal q;

    private BigDecimal pf;

    private BigDecimal epi;

    private BigDecimal epe;

    private BigDecimal soc;

    private BigDecimal soh;

    private BigDecimal battVoltage;

    private BigDecimal battCurrent;

    private BigDecimal battTemp;

    private Integer runMode;

}
