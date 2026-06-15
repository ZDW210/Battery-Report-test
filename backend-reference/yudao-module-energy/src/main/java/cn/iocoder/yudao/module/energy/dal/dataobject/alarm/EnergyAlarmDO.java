package cn.iocoder.yudao.module.energy.dal.dataobject.alarm;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 移动储能告警 DO。
 */
@TableName("energy_alarm")
@KeySequence("energy_alarm_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyAlarmDO extends TenantBaseDO {

    /**
     * 告警编号
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 告警业务编号
     */
    private String alarmNo;
    /**
     * 设备编号
     */
    private Long deviceId;
    /**
     * 告警代码
     */
    private String code;
    /**
     * 告警等级：0 提示，1 一般，2 严重，3 紧急
     */
    private Integer level;
    /**
     * 告警标题
     */
    private String title;
    /**
     * 告警内容
     */
    private String content;
    /**
     * 状态：0 未确认，1 已确认，2 已关闭
     */
    private Integer status;
    /**
     * 发生时间
     */
    private LocalDateTime occurTime;
    /**
     * 确认人
     */
    private Long ackUserId;
    /**
     * 确认时间
     */
    private LocalDateTime ackTime;
    /**
     * 关闭时间
     */
    private LocalDateTime closeTime;

}
