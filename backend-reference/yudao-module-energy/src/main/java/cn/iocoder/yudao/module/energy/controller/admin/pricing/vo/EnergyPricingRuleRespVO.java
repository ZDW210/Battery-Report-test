package cn.iocoder.yudao.module.energy.controller.admin.pricing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 移动储能计费规则 Response VO")
@Data
public class EnergyPricingRuleRespVO {

    @Schema(description = "计费规则编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "客户编号", example = "1024")
    private Long customerId;

    @Schema(description = "客户名称")
    private String customerName;

    @Schema(description = "项目编号", example = "2048")
    private Long projectId;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "设备编号", example = "4096")
    private Long deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备编码")
    private String deviceNo;

    @Schema(description = "时间单价", example = "10.0000")
    private BigDecimal timeRate;

    @Schema(description = "电量单价", example = "1.2500")
    private BigDecimal energyRate;

    @Schema(description = "生效开始时间")
    private LocalDateTime effectiveStart;

    @Schema(description = "生效结束时间")
    private LocalDateTime effectiveEnd;

    @Schema(description = "状态：0 启用，1 停用", example = "0")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
