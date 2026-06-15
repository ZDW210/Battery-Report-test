package cn.iocoder.yudao.module.energy.controller.admin.auth.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "Admin - Energy App user page request")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EnergyAppUserPageReqVO extends PageParam {

    @Schema(description = "Keyword: username/nickname/mobile/card no")
    private String keyword;

    @Schema(description = "Status: 0 enabled, 1 disabled")
    private Integer status;

}
