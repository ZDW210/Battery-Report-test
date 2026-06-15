package cn.iocoder.yudao.module.energy.controller.admin.customer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 移动储能客户 Response VO")
@Data
public class EnergyCustomerRespVO {

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "安科瑞")
    private String name;

    @Schema(description = "联系人", example = "张三")
    private String contactName;

    @Schema(description = "联系电话", example = "13800000000")
    private String contactMobile;

    @Schema(description = "区域", example = "华东")
    private String region;

    @Schema(description = "状态：0 启用，1 停用", example = "0")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
