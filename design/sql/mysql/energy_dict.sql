-- 移动储能系统字典
-- 注意：不同 RuoYi-Vue-Pro 版本 sys_dict_type/sys_dict_data 字段可能略有差异，执行前请按实际版本核对。

INSERT INTO system_dict_type
  (id, name, type, status, remark, creator, create_time, updater, update_time, deleted)
VALUES
  (190000000001, '移动储能设备状态', 'energy_device_status', 0, '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000000002, '移动储能设备类型', 'energy_device_type', 0, '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000000003, '移动储能运行模式', 'energy_run_mode', 0, '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000000004, '移动储能告警等级', 'energy_alarm_level', 0, '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000000005, '移动储能告警状态', 'energy_alarm_status', 0, '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000000006, '移动储能会话类型', 'energy_session_type', 0, '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000000007, '移动储能会话状态', 'energy_session_status', 0, '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000000008, '移动储能调度状态', 'energy_dispatch_status', 0, '', 'system', NOW(), 'system', NOW(), b'0');

INSERT INTO system_dict_data
  (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
VALUES
  (190000001001, 1, '在线', '0', 'energy_device_status', 0, 'success', '', '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000001002, 2, '离线', '1', 'energy_device_status', 0, 'info', '', '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000001003, 3, '故障', '2', 'energy_device_status', 0, 'danger', '', '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000001004, 4, '维护', '3', 'energy_device_status', 0, 'warning', '', '', 'system', NOW(), 'system', NOW(), b'0'),

  (190000002001, 1, '储能车', '0', 'energy_device_type', 0, 'primary', '', '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000002002, 2, '储能柜', '1', 'energy_device_type', 0, 'primary', '', '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000002003, 3, '电池包', '2', 'energy_device_type', 0, 'primary', '', '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000002004, 4, 'PCS', '3', 'energy_device_type', 0, 'primary', '', '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000002005, 5, 'BMS', '4', 'energy_device_type', 0, 'primary', '', '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000002006, 6, '电表', '5', 'energy_device_type', 0, 'primary', '', '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000002007, 7, '网关', '6', 'energy_device_type', 0, 'primary', '', '', 'system', NOW(), 'system', NOW(), b'0'),

  (190000003001, 1, '充电', '0', 'energy_run_mode', 0, 'success', '', '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000003002, 2, '放电', '1', 'energy_run_mode', 0, 'warning', '', '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000003003, 3, '待机', '2', 'energy_run_mode', 0, 'info', '', '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000003004, 4, '故障', '3', 'energy_run_mode', 0, 'danger', '', '', 'system', NOW(), 'system', NOW(), b'0'),

  (190000004001, 1, '提示', '0', 'energy_alarm_level', 0, 'info', '', '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000004002, 2, '一般', '1', 'energy_alarm_level', 0, 'warning', '', '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000004003, 3, '严重', '2', 'energy_alarm_level', 0, 'danger', '', '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000004004, 4, '紧急', '3', 'energy_alarm_level', 0, 'danger', '', '', 'system', NOW(), 'system', NOW(), b'0'),

  (190000005001, 1, '未确认', '0', 'energy_alarm_status', 0, 'danger', '', '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000005002, 2, '已确认', '1', 'energy_alarm_status', 0, 'warning', '', '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000005003, 3, '已关闭', '2', 'energy_alarm_status', 0, 'info', '', '', 'system', NOW(), 'system', NOW(), b'0'),

  (190000006001, 1, '充电', '0', 'energy_session_type', 0, 'success', '', '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000006002, 2, '放电', '1', 'energy_session_type', 0, 'warning', '', '', 'system', NOW(), 'system', NOW(), b'0'),

  (190000007001, 1, '进行中', '0', 'energy_session_status', 0, 'success', '', '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000007002, 2, '已结束', '1', 'energy_session_status', 0, 'info', '', '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000007003, 3, '异常', '2', 'energy_session_status', 0, 'danger', '', '', 'system', NOW(), 'system', NOW(), b'0'),

  (190000008001, 1, '待下发', '0', 'energy_dispatch_status', 0, 'info', '', '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000008002, 2, '执行中', '1', 'energy_dispatch_status', 0, 'warning', '', '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000008003, 3, '成功', '2', 'energy_dispatch_status', 0, 'success', '', '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000008004, 4, '失败', '3', 'energy_dispatch_status', 0, 'danger', '', '', 'system', NOW(), 'system', NOW(), b'0'),
  (190000008005, 5, '取消', '4', 'energy_dispatch_status', 0, 'info', '', '', 'system', NOW(), 'system', NOW(), b'0');

