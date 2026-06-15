package cn.iocoder.yudao.module.energy.controller.admin.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "Admin - Energy App user response")
@Data
public class EnergyAppUserRespVO {

    private Long id;

    private String username;

    private String nickname;

    private String mobile;

    private String cardNo;

    @Schema(description = "是否开放小程序管理端权限")
    private Boolean miniAdminEnabled;

    private Integer status;

    private String loginIp;

    private LocalDateTime loginDate;

    private String remark;

    private LocalDateTime createTime;

}
