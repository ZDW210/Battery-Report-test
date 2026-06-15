package cn.iocoder.yudao.module.energy.controller.admin.alarm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 移动储能告警 Response VO")
@Data
public class EnergyAlarmRespVO {

    @Schema(description = "告警编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "告警业务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "ALM202605300001")
    private String alarmNo;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long deviceId;

    @Schema(description = "设备名称", example = "移动储能柜 A")
    private String deviceName;

    @Schema(description = "告警代码", requiredMode = Schema.RequiredMode.REQUIRED, example = "BATTERY_TEMP_HIGH")
    private String code;

    @Schema(description = "告警等级：0 提示，1 一般，2 严重，3 紧急", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer level;

    @Schema(description = "告警标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "电池温度过高")
    private String title;

    @Schema(description = "告警内容", example = "电池温度超过阈值")
    private String content;

    @Schema(description = "状态：0 未确认，1 已确认，2 已关闭", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "发生时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime occurTime;

    @Schema(description = "确认人", example = "1")
    private Long ackUserId;

    @Schema(description = "确认时间")
    private LocalDateTime ackTime;

    @Schema(description = "关闭时间")
    private LocalDateTime closeTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
