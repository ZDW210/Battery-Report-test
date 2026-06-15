package cn.iocoder.yudao.module.energy.controller.admin.permission.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 移动储能用户授权分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EnergyUserScopePageReqVO extends PageParam {

    @Schema(description = "用户编号", example = "202605310001")
    private Long userId;

    @Schema(description = "用户类型：1 App 用户，2 后台用户", example = "1")
    private Integer userType;

    @Schema(description = "客户编号", example = "1024")
    private Long customerId;

    @Schema(description = "项目编号", example = "2048")
    private Long projectId;

    @Schema(description = "设备编号", example = "4096")
    private Long deviceId;

    @Schema(description = "状态：0 启用，1 禁用", example = "0")
    private Integer status;

}
