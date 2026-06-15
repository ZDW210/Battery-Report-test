package cn.iocoder.yudao.module.energy.controller.admin.customeraccount.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "管理后台 - 客户老板账号 Response VO")
@Data
public class EnergyCustomerAccountRespVO {

    private Long id;

    private Long customerId;

    private String customerName;

    private Long systemUserId;

    private Long roleId;

    private String username;

    private String nickname;

    private String mobile;

    private Integer status;

    private Set<Long> menuIds;

    private String remark;

    private LocalDateTime createTime;

}
