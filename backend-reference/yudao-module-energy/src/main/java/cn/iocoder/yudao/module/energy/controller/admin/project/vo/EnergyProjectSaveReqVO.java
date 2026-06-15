package cn.iocoder.yudao.module.energy.controller.admin.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 移动储能项目新增/修改 Request VO")
@Data
public class EnergyProjectSaveReqVO {

    @Schema(description = "项目编号", example = "1024")
    private Long id;

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "客户不能为空")
    private Long customerId;

    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "上海园区")
    @NotEmpty(message = "项目名称不能为空")
    @Size(max = 128, message = "项目名称长度不能超过 128 个字符")
    private String name;

    @Schema(description = "项目编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "PRJ-001")
    @NotEmpty(message = "项目编码不能为空")
    @Size(max = 64, message = "项目编码长度不能超过 64 个字符")
    private String code;

    @Schema(description = "地址", example = "上海市嘉定区")
    @Size(max = 255, message = "地址长度不能超过 255 个字符")
    private String address;

    @Schema(description = "纬度", example = "31.230416")
    private BigDecimal latitude;

    @Schema(description = "经度", example = "121.473701")
    private BigDecimal longitude;

    @Schema(description = "状态：0 启用，1 停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    @Size(max = 512, message = "备注长度不能超过 512 个字符")
    private String remark;

}
