package cn.iocoder.yudao.module.energy.controller.admin.info.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Admin - Energy module runtime baseline response")
public record EnergyModuleInfoRespVO(
        @Schema(description = "Module name", example = "yudao-module-energy")
        String moduleName,
        @Schema(description = "Design version", example = "0.1.0")
        String designVersion,
        @Schema(description = "Runtime status", example = "BOOTSTRAPPED")
        String status,
        @Schema(description = "First batch business capabilities")
        List<String> capabilities) {
}
