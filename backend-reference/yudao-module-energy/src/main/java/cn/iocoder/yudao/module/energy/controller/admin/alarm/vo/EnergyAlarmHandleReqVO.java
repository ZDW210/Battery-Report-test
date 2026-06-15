package cn.iocoder.yudao.module.energy.controller.admin.alarm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 移动储能告警处理 Request VO")
@Data
public class EnergyAlarmHandleReqVO {

    @Schema(description = "告警编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "告警编号不能为空")
    private Long id;

    @Schema(description = "处理备注", example = "现场已确认，持续观察")
    private String remark;

}
