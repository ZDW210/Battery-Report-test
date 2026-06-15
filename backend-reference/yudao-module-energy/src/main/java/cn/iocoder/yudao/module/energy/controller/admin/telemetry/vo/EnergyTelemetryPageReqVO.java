package cn.iocoder.yudao.module.energy.controller.admin.telemetry.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 移动储能实时采集数据分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EnergyTelemetryPageReqVO extends PageParam {

    @Schema(description = "设备编号", example = "202605310003")
    private Long deviceId;

    @Schema(description = "网关序列号", example = "12207013690004")
    private String gatewaySn;

    @Schema(description = "仪表序列号", example = "12207013690004")
    private String meterSn;

    @Schema(description = "仪表全平台编号", example = "12207013690004_12207013690004")
    private String meterNo;

    @Schema(description = "数据类型：REALTIME/HISTORY", example = "REALTIME")
    private String source;

    @Schema(description = "仪表状态：ONLINE/OFFLINE", example = "ONLINE")
    private String state;

    @Schema(description = "采集时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] collectTime;

}
