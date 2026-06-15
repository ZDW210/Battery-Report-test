package cn.iocoder.yudao.module.energy.dal.dataobject.alarm;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 移动储能告警处理记录 DO。
 */
@TableName("energy_alarm_handle_record")
@KeySequence("energy_alarm_handle_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyAlarmHandleRecordDO extends TenantBaseDO {

    /**
     * 告警处理记录编号
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 告警编号
     */
    private Long alarmId;
    /**
     * 处理动作：ack/close
     */
    private String action;
    /**
     * 处理备注
     */
    private String remark;
    /**
     * 操作人
     */
    private Long operatorUserId;
    /**
     * 操作时间
     */
    private LocalDateTime operateTime;

}
