package cn.iocoder.yudao.module.energy.controller.admin.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 移动储能 App 用户精简 Response VO")
@Data
public class EnergyAppUserSimpleRespVO {

    @Schema(description = "App 用户编号", example = "202606010001")
    private Long id;

    @Schema(description = "账号", example = "wx_openid_user")
    private String username;

    @Schema(description = "昵称", example = "微信小程序用户")
    private String nickname;

    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

    @Schema(description = "卡号", example = "CARD-0001")
    private String cardNo;

    @Schema(description = "是否开放小程序管理端权限", example = "false")
    private Boolean miniAdminEnabled;

    @Schema(description = "状态：0 启用，1 禁用", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
