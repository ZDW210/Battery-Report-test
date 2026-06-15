package cn.iocoder.yudao.module.energy.controller.admin.customer.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 移动储能客户分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EnergyCustomerPageReqVO extends PageParam {

    @Schema(description = "客户名称", example = "安科瑞")
    private String name;

    @Schema(description = "联系人", example = "张三")
    private String contactName;

    @Schema(description = "区域", example = "华东")
    private String region;

    @Schema(description = "状态：0 启用，1 停用", example = "0")
    private Integer status;

}
