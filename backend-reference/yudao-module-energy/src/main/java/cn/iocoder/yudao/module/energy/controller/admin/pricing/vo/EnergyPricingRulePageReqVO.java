package cn.iocoder.yudao.module.energy.controller.admin.pricing.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 移动储能计费规则分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EnergyPricingRulePageReqVO extends PageParam {

    @Schema(description = "客户编号", example = "1024")
    private Long customerId;

    @Schema(description = "项目编号", example = "2048")
    private Long projectId;

    @Schema(description = "设备编号", example = "4096")
    private Long deviceId;

    @Schema(description = "状态：0 启用，1 停用", example = "0")
    private Integer status;

    @Schema(description = "生效开始时间")
    private LocalDateTime[] effectiveStart;

}
