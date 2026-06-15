package cn.iocoder.yudao.module.energy.controller.admin.eiot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 移动储能 EIOT 同步日志 Response VO")
@Data
public class EnergyEiotSyncLogRespVO {

    @Schema(description = "同步日志编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "同步类型：realtime/alarm", example = "alarm")
    private String syncType;

    @Schema(description = "请求编号", example = "REQ-202606010001")
    private String requestId;

    @Schema(description = "网关序列号", example = "GW20260530001")
    private String gatewaySn;

    @Schema(description = "电表序列号", example = "METER-SN-001")
    private String meterSn;

    @Schema(description = "原始报文归档地址")
    private String payloadUrl;

    @Schema(description = "状态：0 成功，1 失败", example = "1")
    private Integer status;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
