package cn.iocoder.yudao.module.energy.controller.admin.customeraccount.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 客户老板账号分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class EnergyCustomerAccountPageReqVO extends PageParam {

    @Schema(description = "客户编号", example = "1024")
    private Long customerId;

    @Schema(description = "账号/昵称/手机号关键字")
    private String keyword;

    @Schema(description = "状态：0 启用，1 停用", example = "0")
    private Integer status;

}
