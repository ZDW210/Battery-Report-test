package cn.iocoder.yudao.module.energy.controller.admin.permission.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 移动储能用户授权新增/修改 Request VO")
@Data
public class EnergyUserScopeSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "202605310001")
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @Schema(description = "用户类型：1 App 用户，2 后台用户", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "用户类型不能为空")
    private Integer userType;

    @Schema(description = "客户编号", example = "1024")
    private Long customerId;

    @Schema(description = "项目编号", example = "2048")
    private Long projectId;

    @Schema(description = "设备编号", example = "4096")
    private Long deviceId;

    @Schema(description = "状态：0 启用，1 禁用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "客户小程序账号")
    private String remark;

}
