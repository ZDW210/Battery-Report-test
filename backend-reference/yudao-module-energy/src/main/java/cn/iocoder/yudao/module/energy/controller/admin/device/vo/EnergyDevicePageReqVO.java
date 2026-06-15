package cn.iocoder.yudao.module.energy.controller.admin.device.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 移动储能设备分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EnergyDevicePageReqVO extends PageParam {

    @Schema(description = "设备编码", example = "ESS-001")
    private String deviceNo;

    @Schema(description = "设备名称", example = "移动储能柜 A")
    private String deviceName;

    @Schema(description = "设备类型", example = "1")
    private Integer deviceType;

    @Schema(description = "网关序列号", example = "GW20260530001")
    private String gatewaySn;

    @Schema(description = "电表序列号", example = "METER-SN-001")
    private String meterSn;

    @Schema(description = "仪表编号", example = "METER-001")
    private String meterNo;

    @Schema(description = "客户编号", example = "1024")
    private Long customerId;

    @Schema(description = "项目编号", example = "2048")
    private Long projectId;

    @Schema(description = "设备状态：0 在线，1 离线，2 故障，3 维护", example = "1")
    private Integer status;

}
