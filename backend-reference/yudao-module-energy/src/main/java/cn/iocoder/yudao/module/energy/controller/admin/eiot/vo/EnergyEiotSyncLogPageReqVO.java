package cn.iocoder.yudao.module.energy.controller.admin.eiot.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 移动储能 EIOT 同步日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EnergyEiotSyncLogPageReqVO extends PageParam {

    @Schema(description = "同步类型：realtime/alarm", example = "alarm")
    private String syncType;

    @Schema(description = "请求编号", example = "REQ-202606010001")
    private String requestId;

    @Schema(description = "网关序列号", example = "GW20260530001")
    private String gatewaySn;

    @Schema(description = "电表序列号", example = "METER-SN-001")
    private String meterSn;

    @Schema(description = "状态：0 成功，1 失败", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

}
