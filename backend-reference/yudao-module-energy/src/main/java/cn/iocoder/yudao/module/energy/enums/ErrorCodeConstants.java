package cn.iocoder.yudao.module.energy.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Energy 错误码枚举类。
 *
 * energy 系统，使用 1-061-000-000 段。
 */
public interface ErrorCodeConstants {

    // ========== 移动储能设备 1-061-100-000 ==========
    ErrorCode DEVICE_NOT_EXISTS = new ErrorCode(1_061_100_000, "设备不存在");
    ErrorCode DEVICE_NO_DUPLICATE = new ErrorCode(1_061_100_001, "设备编码重复");
    ErrorCode DEVICE_METER_NO_DUPLICATE = new ErrorCode(1_061_100_002, "仪表编号重复");

    // ========== 移动储能客户 1-061-101-000 ==========
    ErrorCode CUSTOMER_NOT_EXISTS = new ErrorCode(1_061_101_000, "客户不存在");

    // ========== 移动储能项目 1-061-102-000 ==========
    ErrorCode PROJECT_NOT_EXISTS = new ErrorCode(1_061_102_000, "项目不存在");
    ErrorCode PROJECT_CODE_DUPLICATE = new ErrorCode(1_061_102_001, "项目编码重复");

    // ========== 移动储能告警 1-061-103-000 ==========
    ErrorCode ALARM_NOT_EXISTS = new ErrorCode(1_061_103_000, "告警不存在");
    ErrorCode ALARM_STATUS_NOT_PENDING = new ErrorCode(1_061_103_001, "只有未确认告警允许确认");
    ErrorCode ALARM_STATUS_CLOSED = new ErrorCode(1_061_103_002, "告警已关闭");

    // ========== 移动储能用户授权 1-061-104-000 ==========
    ErrorCode USER_SCOPE_NOT_EXISTS = new ErrorCode(1_061_104_000, "用户授权不存在");
    ErrorCode USER_SCOPE_TARGET_REQUIRED = new ErrorCode(1_061_104_001, "必须且只能选择一个授权范围");
    ErrorCode USER_SCOPE_DUPLICATE = new ErrorCode(1_061_104_002, "用户授权范围重复");

    // ========== Energy App auth 1-061-105-000 ==========
    ErrorCode APP_AUTH_LOGIN_BAD_CREDENTIALS = new ErrorCode(1_061_105_000, "App username or password invalid");
    ErrorCode APP_AUTH_USER_DISABLED = new ErrorCode(1_061_105_001, "App user disabled");

    // ========== 移动储能计费规则 1-061-106-000 ==========
    ErrorCode PRICING_RULE_NOT_EXISTS = new ErrorCode(1_061_106_000, "计费规则不存在");
    ErrorCode PRICING_RULE_TARGET_REQUIRED = new ErrorCode(1_061_106_001, "必须且只能选择一个计费范围");
    ErrorCode PRICING_RULE_TIME_INVALID = new ErrorCode(1_061_106_002, "计费规则结束时间不能早于开始时间");

    // ========== 移动储能充放电会话 1-061-107-000 ==========
    ErrorCode CHARGE_SESSION_NOT_EXISTS = new ErrorCode(1_061_107_000, "充放电会话不存在");
    ErrorCode CHARGE_SESSION_RUNNING_EXISTS = new ErrorCode(1_061_107_001, "该设备存在进行中的充放电会话");
    ErrorCode CHARGE_SESSION_NOT_RUNNING = new ErrorCode(1_061_107_002, "只有进行中的会话允许结束");
    ErrorCode CHARGE_SESSION_NOT_COMPLETED = new ErrorCode(1_061_107_003, "只有已结束或异常会话允许结算");
    ErrorCode CHARGE_SESSION_NO_PRICING_RULE = new ErrorCode(1_061_107_004, "未匹配到当前生效计费规则");
    ErrorCode CHARGE_SESSION_ENERGY_INVALID = new ErrorCode(1_061_107_005, "结束电量不能小于开始电量");
    ErrorCode CHARGE_SESSION_DEVICE_CUSTOMER_REQUIRED = new ErrorCode(1_061_107_006, "开始充放电任务前，请先给设备绑定客户");
    ErrorCode CHARGE_SESSION_PRICING_RULE_NOT_MATCH = new ErrorCode(1_061_107_007, "所选计费规则不适用于当前设备");
    ErrorCode CHARGE_SESSION_DEVICE_CONTROL_FAILED = new ErrorCode(1_061_107_008, "设备控制失败，原因：{}");

    // ========== 移动储能 App 扫码 1-061-108-000 ==========
    ErrorCode APP_SCAN_CODE_EMPTY = new ErrorCode(1_061_108_000, "扫码内容不能为空");
    ErrorCode APP_SCAN_DEVICE_NOT_EXISTS = new ErrorCode(1_061_108_001, "扫码设备不存在");
    ErrorCode APP_SCAN_DEVICE_FORBIDDEN = new ErrorCode(1_061_108_002, "当前账号无权使用该设备");

    // ========== Energy vehicle 1-061-109-000 ==========
    ErrorCode VEHICLE_NOT_EXISTS = new ErrorCode(1_061_109_000, "Vehicle does not exist");
    ErrorCode VEHICLE_NO_DUPLICATE = new ErrorCode(1_061_109_001, "Vehicle number already exists");
    ErrorCode VEHICLE_QR_CODE_DUPLICATE = new ErrorCode(1_061_109_002, "Vehicle QR code already exists");

    ErrorCode APP_SCAN_ACCOUNT_UNKNOWN = new ErrorCode(1_061_110_000, "未知账户");

    // ========== 移动储能客户老板账号 1-061-111-000 ==========
    ErrorCode CUSTOMER_ACCOUNT_NOT_EXISTS = new ErrorCode(1_061_111_000, "客户账号不存在");
    ErrorCode CUSTOMER_ACCOUNT_PASSWORD_REQUIRED = new ErrorCode(1_061_111_001, "新增客户账号时首次密码不能为空");
    ErrorCode CUSTOMER_ACCOUNT_MENU_REQUIRED = new ErrorCode(1_061_111_002, "至少选择一个开放板块");

}
