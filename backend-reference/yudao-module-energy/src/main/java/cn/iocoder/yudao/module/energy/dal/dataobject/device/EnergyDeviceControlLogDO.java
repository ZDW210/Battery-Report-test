package cn.iocoder.yudao.module.energy.dal.dataobject.device;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

@TableName("energy_device_control_log")
@KeySequence("energy_device_control_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyDeviceControlLogDO extends TenantBaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long deviceId;
    private String deviceNo;
    private String gatewaySn;
    private String meterSn;
    private String meterNo;
    private String method;
    private String value;
    private String requestPayload;
    private String responsePayload;
    private String eiotCode;
    private Integer status;
    private String errorMsg;
    private Long operatorUserId;
    private LocalDateTime operateTime;

}
