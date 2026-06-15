package cn.iocoder.yudao.module.energy.controller.admin.customeraccount.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

@Schema(description = "管理后台 - 客户老板账号新增/修改 Request VO")
@Data
public class EnergyCustomerAccountSaveReqVO {

    @Schema(description = "账号编号", example = "1024")
    private Long id;

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "客户不能为空")
    private Long customerId;

    @Schema(description = "登录账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "fleet001")
    @NotEmpty(message = "登录账号不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,30}$", message = "登录账号由 4-30 位数字或字母组成")
    private String username;

    @Schema(description = "首次密码，新增时必填", example = "123456")
    @Length(min = 4, max = 16, message = "密码长度为 4-16 位")
    private String password;

    @Schema(description = "账号名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张总")
    @NotEmpty(message = "账号名称不能为空")
    @Size(max = 30, message = "账号名称长度不能超过 30 个字符")
    private String nickname;

    @Schema(description = "手机号", example = "13800000000")
    @Size(max = 32, message = "手机号长度不能超过 32 个字符")
    private String mobile;

    @Schema(description = "状态：0 启用，1 停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "可见板块菜单编号")
    private Set<Long> menuIds;

    @Schema(description = "备注")
    @Size(max = 512, message = "备注长度不能超过 512 个字符")
    private String remark;

}
