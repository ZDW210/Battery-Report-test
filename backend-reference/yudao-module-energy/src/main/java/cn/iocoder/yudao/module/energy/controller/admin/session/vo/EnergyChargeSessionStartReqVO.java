package cn.iocoder.yudao.module.energy.controller.admin.session.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 移动储能充放电会话开始 Request VO")
@Data
public class EnergyChargeSessionStartReqVO {

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "202605310003")
    @NotNull(message = "设备编号不能为空")
    private Long deviceId;

    @Schema(description = "会话类型：0 充电，1 放电", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "会话类型不能为空")
    private Integer sessionType;

    @Schema(description = "计费规则编号；不传时自动匹配当前生效规则", example = "1024")
    private Long pricingRuleId;

}
