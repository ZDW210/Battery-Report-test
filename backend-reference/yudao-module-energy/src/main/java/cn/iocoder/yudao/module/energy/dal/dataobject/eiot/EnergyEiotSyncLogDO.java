package cn.iocoder.yudao.module.energy.dal.dataobject.eiot;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 移动储能 EIOT 同步日志 DO。
 */
@TableName("energy_eiot_sync_log")
@KeySequence("energy_eiot_sync_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyEiotSyncLogDO extends TenantBaseDO {

    /**
     * 同步日志编号
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 同步类型：realtime/alarm
     */
    private String syncType;
    /**
     * 请求编号
     */
    private String requestId;
    /**
     * 网关序列号
     */
    private String gatewaySn;
    /**
     * 电表序列号
     */
    private String meterSn;
    /**
     * 原始报文归档地址
     */
    private String payloadUrl;
    /**
     * 状态：0 成功，1 失败
     */
    private Integer status;
    /**
     * 错误信息
     */
    private String errorMsg;

}
