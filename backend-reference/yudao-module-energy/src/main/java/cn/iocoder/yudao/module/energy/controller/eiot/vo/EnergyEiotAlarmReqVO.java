package cn.iocoder.yudao.module.energy.controller.eiot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "EIOT - 移动储能告警接入 Request VO")
@Data
public class EnergyEiotAlarmReqVO {

    @Schema(description = "请求编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20260529103100123")
    private String requestId;

    @Schema(description = "网关序列号", requiredMode = Schema.RequiredMode.REQUIRED, example = "GW001")
    @NotBlank(message = "网关序列号不能为空")
    private String gatewaySn;

    @Schema(description = "网关下的仪表编号")
    private String meterSn;

    @Schema(description = "项目编码")
    private String projectCode;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "设备本地时间，格式 yyyy-MM-dd HH:mm:ss")
    private String createTime;

    @Schema(description = "Unix 时间戳")
    private Long timestamp;

    @Schema(description = "告警列表，兼容本系统 alarms 字段")
    @Valid
    private List<AlarmItem> alarms;

    @Schema(description = "告警列表，兼容 EIOT 原始 list 字段")
    @Valid
    private List<AlarmItem> list;

    @Schema(description = "EIOT - 移动储能告警项")
    @Data
    public static class AlarmItem {

        @Schema(description = "告警业务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "ALM202605290001")
        private String alarmNo;

        @Schema(description = "电表序列号", example = "METER001")
        private String meterSn;

        @Schema(description = "仪表编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100001")
        private String meterNo;

        @Schema(description = "告警代码", requiredMode = Schema.RequiredMode.REQUIRED, example = "BATTERY_TEMP_HIGH")
        private String code;

        @Schema(description = "告警等级：0 提示，1 一般，2 严重，3 紧急", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
        private String level;

        @Schema(description = "告警标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "电池温度过高")
        private Object title;

        @Schema(description = "告警内容", example = "电池温度超过阈值")
        private Object content;

        @Schema(description = "告警内容，兼容 EIOT 原始 message 字段")
        private Object message;

        @Schema(description = "发生时间", requiredMode = Schema.RequiredMode.REQUIRED)
        private String occurTime;

        public String getTitleText() {
            return resolveText(title);
        }

        public String getContentText() {
            Object value = content != null ? content : message;
            return resolveText(value);
        }

        private static String resolveText(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof String text) {
                return text;
            }
            if (value instanceof Map<?, ?> map) {
                Object zh = map.get("zh_CN");
                if (zh != null) {
                    return zh.toString();
                }
                Object en = map.get("en_US");
                if (en != null) {
                    return en.toString();
                }
                return map.values().stream().findFirst().map(Object::toString).orElse(null);
            }
            return value.toString();
        }

    }

}
