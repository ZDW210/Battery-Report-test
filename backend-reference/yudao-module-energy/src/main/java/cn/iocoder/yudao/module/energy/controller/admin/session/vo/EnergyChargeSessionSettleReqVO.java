package cn.iocoder.yudao.module.energy.controller.admin.session.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 移动储能充放电会话结算 Request VO")
@Data
public class EnergyChargeSessionSettleReqVO {

    @Schema(description = "会话编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1900501")
    @NotNull(message = "会话编号不能为空")
    private Long sessionId;

}
