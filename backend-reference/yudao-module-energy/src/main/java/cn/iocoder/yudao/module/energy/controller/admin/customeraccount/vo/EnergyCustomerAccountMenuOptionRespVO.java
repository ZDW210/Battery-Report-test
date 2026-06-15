package cn.iocoder.yudao.module.energy.controller.admin.customeraccount.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 客户老板账号可见板块 Response VO")
@Data
public class EnergyCustomerAccountMenuOptionRespVO {

    private Long id;

    private String name;

    private String permission;

    private Long parentId;

    private Integer sort;

}
