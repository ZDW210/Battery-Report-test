package cn.iocoder.yudao.module.energy.dal.dataobject.device;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 移动储能设备 DO。
 */
@TableName("energy_device")
@KeySequence("energy_device_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyDeviceDO extends TenantBaseDO {

    /**
     * 设备编号
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 设备编码
     */
    private String deviceNo;
    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 设备类型
     */
    private Integer deviceType;
    /**
     * 网关序列号
     */
    private String gatewaySn;
    /**
     * 电表序列号
     */
    private String meterSn;
    /**
     * 仪表编号
     */
    private String meterNo;
    /**
     * 客户编号
     */
    private Long customerId;
    /**
     * 项目编号
     */
    private Long projectId;
    /**
     * 设备状态：0 在线，1 离线，2 故障，3 维护
     */
    private Integer status;
    /**
     * 运行模式：0 充电，1 放电，2 待机，3 故障
     */
    private Integer runMode;
    /**
     * 纬度
     */
    private BigDecimal latitude;
    /**
     * 经度
     */
    private BigDecimal longitude;
    /**
     * 最新 SOC
     */
    private BigDecimal lastSoc;
    /**
     * 最新 SOH
     */
    private BigDecimal lastSoh;
    /**
     * 最新功率
     */
    private BigDecimal lastPower;
    /**
     * 最新电压
     */
    private BigDecimal lastVoltage;
    /**
     * 最新电流
     */
    private BigDecimal lastCurrent;
    /**
     * 最新温度
     */
    private BigDecimal lastTemp;
    /**
     * 最新采集时间
     */
    private LocalDateTime lastReadingTime;
    /**
     * 备注
     */
    private String remark;

}
