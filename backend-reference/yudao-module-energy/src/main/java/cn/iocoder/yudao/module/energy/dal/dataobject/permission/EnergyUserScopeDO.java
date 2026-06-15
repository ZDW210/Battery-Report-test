package cn.iocoder.yudao.module.energy.dal.dataobject.permission;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("energy_user_scope")
@KeySequence("energy_user_scope_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyUserScopeDO extends TenantBaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Integer userType;

    private Long customerId;

    private Long projectId;

    private Long deviceId;

    private Integer status;

    private String remark;

}
