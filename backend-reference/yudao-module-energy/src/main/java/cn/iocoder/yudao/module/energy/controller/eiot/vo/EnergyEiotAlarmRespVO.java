package cn.iocoder.yudao.module.energy.controller.eiot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "EIOT - 移动储能告警接入 Response VO")
@Data
@Builder
public class EnergyEiotAlarmRespVO {

    @Schema(description = "接收数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer received;

    @Schema(description = "创建数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer created;

    @Schema(description = "忽略数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer ignored;

    @Schema(description = "失败数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer failed;

}
