package cn.iocoder.yudao.module.energy.dal.dataobject.auth;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

@TableName("energy_app_user")
@KeySequence("energy_app_user_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyAppUserDO extends TenantBaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String mobile;

    private String cardNo;

    private Boolean miniAdminEnabled;

    private Integer status;

    private String loginIp;

    private LocalDateTime loginDate;

    private String remark;

}
