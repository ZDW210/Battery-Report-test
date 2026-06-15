package cn.iocoder.yudao.module.energy.controller.admin.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 移动储能项目 Response VO")
@Data
public class EnergyProjectRespVO {

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long customerId;

    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "上海园区")
    private String name;

    @Schema(description = "项目编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "PRJ-001")
    private String code;

    @Schema(description = "地址", example = "上海市嘉定区")
    private String address;

    @Schema(description = "纬度", example = "31.230416")
    private BigDecimal latitude;

    @Schema(description = "经度", example = "121.473701")
    private BigDecimal longitude;

    @Schema(description = "状态：0 启用，1 停用", example = "0")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
