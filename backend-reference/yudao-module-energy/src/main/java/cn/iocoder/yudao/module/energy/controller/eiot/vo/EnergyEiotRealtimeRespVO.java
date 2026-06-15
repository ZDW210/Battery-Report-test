package cn.iocoder.yudao.module.energy.controller.eiot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "EIOT - 电表实时数据接入 Response VO")
@Data
@Builder
public class EnergyEiotRealtimeRespVO {

    @Schema(description = "接收数量")
    private Integer received;

    @Schema(description = "更新数量")
    private Integer updated;

    @Schema(description = "忽略数量")
    private Integer ignored;

    @Schema(description = "失败数量")
    private Integer failed;

}
