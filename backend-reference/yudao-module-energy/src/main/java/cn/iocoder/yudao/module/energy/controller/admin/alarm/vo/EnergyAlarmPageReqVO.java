package cn.iocoder.yudao.module.energy.controller.admin.alarm.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 移动储能告警分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EnergyAlarmPageReqVO extends PageParam {

    @Schema(description = "设备编号", example = "1024")
    private Long deviceId;

    @Schema(description = "告警等级：0 提示，1 一般，2 严重，3 紧急", example = "2")
    private Integer level;

    @Schema(description = "状态：0 未确认，1 已确认，2 已关闭", example = "0")
    private Integer status;

    @Schema(description = "告警代码", example = "BATTERY_TEMP_HIGH")
    private String code;

    @Schema(description = "告警标题", example = "电池温度过高")
    private String title;

    @Schema(description = "发生时间")
    private LocalDateTime[] occurTime;

}
