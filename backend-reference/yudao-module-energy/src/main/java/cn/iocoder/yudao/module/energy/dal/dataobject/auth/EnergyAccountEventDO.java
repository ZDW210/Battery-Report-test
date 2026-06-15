package cn.iocoder.yudao.module.energy.dal.dataobject.auth;

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
 * Mobile energy account identification event.
 */
@TableName("energy_account_event")
@KeySequence("energy_account_event_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyAccountEventDO extends TenantBaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String eventScene;

    private String authType;

    private String scanText;

    private String cardNo;

    private Boolean accountKnown;

    private Long accountId;

    private String accountName;

    private String accountMobile;

    private Long deviceId;

    private String deviceNo;

    private String deviceName;

    private String meterNo;

    private String gatewaySn;

    private String meterSn;

    private Long customerId;

    private Long projectId;

    private String resultMessage;

}
