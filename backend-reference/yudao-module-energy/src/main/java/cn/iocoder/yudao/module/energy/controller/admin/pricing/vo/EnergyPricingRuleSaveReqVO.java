package cn.iocoder.yudao.module.energy.controller.admin.pricing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 移动储能计费规则保存 Request VO")
@Data
public class EnergyPricingRuleSaveReqVO {

    @Schema(description = "计费规则编号", example = "1024")
    private Long id;

    @Schema(description = "客户编号", example = "1024")
    private Long customerId;

    @Schema(description = "项目编号", example = "2048")
    private Long projectId;

    @Schema(description = "设备编号", example = "4096")
    private Long deviceId;

    @Schema(description = "时间单价", requiredMode = Schema.RequiredMode.REQUIRED, example = "10.0000")
    @NotNull(message = "时间单价不能为空")
    @DecimalMin(value = "0", message = "时间单价不能小于 0")
    private BigDecimal timeRate;

    @Schema(description = "电量单价", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.2500")
    @NotNull(message = "电量单价不能为空")
    @DecimalMin(value = "0", message = "电量单价不能小于 0")
    private BigDecimal energyRate;

    @Schema(description = "生效开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "生效开始时间不能为空")
    private String effectiveStart;

    @Schema(description = "生效结束时间")
    private String effectiveEnd;

    @Schema(description = "状态：0 启用，1 停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

}
