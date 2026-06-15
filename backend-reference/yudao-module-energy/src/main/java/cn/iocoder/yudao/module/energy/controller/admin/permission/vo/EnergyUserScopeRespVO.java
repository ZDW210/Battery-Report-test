package cn.iocoder.yudao.module.energy.controller.admin.permission.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 移动储能用户授权 Response VO")
@Data
public class EnergyUserScopeRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "用户编号", example = "202605310001")
    private Long userId;

    @Schema(description = "用户类型：1 App 用户，2 后台用户", example = "1")
    private Integer userType;

    @Schema(description = "App 用户账号", example = "wx_openid_user")
    private String userUsername;

    @Schema(description = "App 用户昵称", example = "微信小程序用户")
    private String userNickname;

    @Schema(description = "App 用户手机号", example = "13800138000")
    private String userMobile;

    @Schema(description = "客户编号", example = "1024")
    private Long customerId;

    @Schema(description = "客户名称", example = "安克瑞")
    private String customerName;

    @Schema(description = "项目编号", example = "2048")
    private Long projectId;

    @Schema(description = "项目名称", example = "上海储能站")
    private String projectName;

    @Schema(description = "设备编号", example = "4096")
    private Long deviceId;

    @Schema(description = "设备名称", example = "移动储能柜 A")
    private String deviceName;

    @Schema(description = "设备编码", example = "ESS-001")
    private String deviceNo;

    @Schema(description = "状态：0 启用，1 禁用", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "客户小程序账号")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
