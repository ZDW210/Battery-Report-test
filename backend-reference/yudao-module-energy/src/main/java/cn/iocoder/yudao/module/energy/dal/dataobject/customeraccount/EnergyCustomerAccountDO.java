package cn.iocoder.yudao.module.energy.dal.dataobject.customeraccount;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * Fleet owner web account bound to an energy customer.
 */
@TableName("energy_customer_account")
@KeySequence("energy_customer_account_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyCustomerAccountDO extends TenantBaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * Energy customer id.
     */
    private Long customerId;

    /**
     * Back-office login user id in system_user.
     */
    private Long systemUserId;

    /**
     * Dedicated role id in system_role.
     */
    private Long roleId;

    private String username;

    private String nickname;

    private String mobile;

    private Integer status;

    /**
     * Selected energy menu ids, comma separated.
     */
    private String menuIds;

    private String remark;

}
