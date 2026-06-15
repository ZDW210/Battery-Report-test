package cn.iocoder.yudao.module.energy.controller.admin.telemetry.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 移动储能逐日极值 Response VO")
@Data
@Builder
public class EnergyTelemetryDailyStatRespVO {

    @Schema(description = "统计日期", example = "2026-06-04")
    private LocalDate date;

    @Schema(description = "最大值")
    private BigDecimal max;

    @Schema(description = "最大值发生时间")
    private LocalDateTime maxTime;

    @Schema(description = "最小值")
    private BigDecimal min;

    @Schema(description = "最小值发生时间")
    private LocalDateTime minTime;

    @Schema(description = "平均值")
    private BigDecimal avg;

}
