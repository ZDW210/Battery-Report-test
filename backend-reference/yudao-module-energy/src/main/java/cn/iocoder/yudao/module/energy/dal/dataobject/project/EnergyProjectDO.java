package cn.iocoder.yudao.module.energy.dal.dataobject.project;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * 移动储能项目/场站 DO。
 */
@TableName("energy_project")
@KeySequence("energy_project_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyProjectDO extends TenantBaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 客户编号
     */
    private Long customerId;
    /**
     * 项目名称
     */
    private String name;
    /**
     * 项目编码
     */
    private String code;
    /**
     * 地址
     */
    private String address;
    /**
     * 纬度
     */
    private BigDecimal latitude;
    /**
     * 经度
     */
    private BigDecimal longitude;
    /**
     * 状态：0 启用，1 停用
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
