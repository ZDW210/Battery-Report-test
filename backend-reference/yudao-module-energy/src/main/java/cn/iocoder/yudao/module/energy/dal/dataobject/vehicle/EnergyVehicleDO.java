package cn.iocoder.yudao.module.energy.dal.dataobject.vehicle;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Vehicle bound to an energy storage device.
 */
@TableName("energy_vehicle")
@KeySequence("energy_vehicle_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyVehicleDO extends TenantBaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String vehicleNo;

    private String plateNo;

    private String qrCode;

    private Long deviceId;

    private Long customerId;

    private Long projectId;

    /**
     * 0 enabled, 1 disabled.
     */
    private Integer status;

    private String remark;

}
