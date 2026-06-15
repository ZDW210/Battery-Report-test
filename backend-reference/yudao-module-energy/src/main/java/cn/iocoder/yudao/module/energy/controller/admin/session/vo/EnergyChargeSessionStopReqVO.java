package cn.iocoder.yudao.module.energy.controller.admin.session.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 移动储能充放电会话结束 Request VO")
@Data
public class EnergyChargeSessionStopReqVO {

    @Schema(description = "会话编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1900501")
    @NotNull(message = "会话编号不能为空")
    private Long sessionId;

    @Schema(description = "结束电量读数；不传时自动取最新 EPI", example = "12500.200")
    private BigDecimal endEnergy;

}
