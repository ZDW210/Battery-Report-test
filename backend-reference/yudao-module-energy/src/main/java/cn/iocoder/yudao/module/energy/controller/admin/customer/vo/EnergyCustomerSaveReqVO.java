package cn.iocoder.yudao.module.energy.controller.admin.customer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - 移动储能客户新增/修改 Request VO")
@Data
public class EnergyCustomerSaveReqVO {

    @Schema(description = "客户编号", example = "1024")
    private Long id;

    @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "安科瑞")
    @NotEmpty(message = "客户名称不能为空")
    @Size(max = 128, message = "客户名称长度不能超过 128 个字符")
    private String name;

    @Schema(description = "联系人", example = "张三")
    @Size(max = 64, message = "联系人长度不能超过 64 个字符")
    private String contactName;

    @Schema(description = "联系电话", example = "13800000000")
    @Size(max = 32, message = "联系电话长度不能超过 32 个字符")
    private String contactMobile;

    @Schema(description = "区域", example = "华东")
    @Size(max = 64, message = "区域长度不能超过 64 个字符")
    private String region;

    @Schema(description = "状态：0 启用，1 停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    @Size(max = 512, message = "备注长度不能超过 512 个字符")
    private String remark;

}
