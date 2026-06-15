package cn.iocoder.yudao.module.energy.controller.admin.project.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 移动储能项目分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EnergyProjectPageReqVO extends PageParam {

    @Schema(description = "客户编号", example = "1024")
    private Long customerId;

    @Schema(description = "项目名称", example = "上海园区")
    private String name;

    @Schema(description = "项目编码", example = "PRJ-001")
    private String code;

    @Schema(description = "状态：0 启用，1 停用", example = "0")
    private Integer status;

}
