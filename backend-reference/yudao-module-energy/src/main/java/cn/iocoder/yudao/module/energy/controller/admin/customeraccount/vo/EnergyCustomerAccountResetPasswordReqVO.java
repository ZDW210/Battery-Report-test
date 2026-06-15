package cn.iocoder.yudao.module.energy.controller.admin.customeraccount.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Schema(description = "管理后台 - 客户老板账号重置密码 Request VO")
@Data
public class EnergyCustomerAccountResetPasswordReqVO {

    @Schema(description = "账号编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "账号编号不能为空")
    private Long id;

    @Schema(description = "新密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @Length(min = 4, max = 16, message = "密码长度为 4-16 位")
    private String password;

}
