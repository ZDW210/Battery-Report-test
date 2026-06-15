package cn.iocoder.yudao.module.energy.dal.dataobject.customer;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 移动储能客户 DO。
 */
@TableName("energy_customer")
@KeySequence("energy_customer_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyCustomerDO extends TenantBaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 客户名称
     */
    private String name;
    /**
     * 联系人
     */
    private String contactName;
    /**
     * 联系电话
     */
    private String contactMobile;
    /**
     * 区域
     */
    private String region;
    /**
     * 状态：0 启用，1 停用
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
