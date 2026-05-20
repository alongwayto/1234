USE campus_equipment;

-- ----------------------------
-- 初始化角色数据
-- ----------------------------
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`) VALUES
(1, '超级管理员', 'ROLE_ADMIN', '系统超级管理员，拥有所有权限'),
(2, '维护员', 'ROLE_MAINTAINER', '设备维护人员，负责故障处理和设备维护'),
(3, '普通用户', 'ROLE_USER', '普通用户，可查看设备信息和上报故障');

-- ----------------------------
-- 初始化用户数据（密码均为 admin123，BCrypt加密）
-- ----------------------------
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `email`, `phone`, `department`, `status`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'admin@campus.edu.cn', '13800000001', '信息技术中心', 1),
(2, 'maintainer1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '张维修', 'zhang@campus.edu.cn', '13800000002', '设备维护部', 1),
(3, 'maintainer2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '李技术', 'li@campus.edu.cn', '13800000003', '设备维护部', 1),
(4, 'user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '王同学', 'wang@campus.edu.cn', '13800000004', '计算机学院', 1),
(5, 'user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '赵老师', 'zhao@campus.edu.cn', '13800000005', '电子工程学院', 1);

-- 注意：上面的密码hash是示例，实际需要用BCrypt重新生成 admin123 的hash
-- 正确的 admin123 BCrypt hash:
UPDATE `sys_user` SET `password` = '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2' WHERE `id` IN (1,2,3,4,5);

-- ----------------------------
-- 用户角色关联
-- ----------------------------
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),  -- admin -> 超级管理员
(2, 2),  -- maintainer1 -> 维护员
(3, 2),  -- maintainer2 -> 维护员
(4, 3),  -- user1 -> 普通用户
(5, 3);  -- user2 -> 普通用户

-- ----------------------------
-- 初始化权限数据
-- ----------------------------
INSERT INTO `sys_permission` (`id`, `parent_id`, `perm_name`, `perm_code`, `perm_type`, `path`, `component`, `icon`, `sort_order`) VALUES
-- 一级菜单
(1,  0, '首页仪表盘', 'dashboard', 1, '/dashboard', 'dashboard/index', 'Odometer', 1),
(2,  0, '设备管理', 'device', 1, '/device', NULL, 'Monitor', 2),
(3,  0, '状态监控', 'monitor', 1, '/monitor', NULL, 'DataAnalysis', 3),
(4,  0, '故障管理', 'fault', 1, '/fault', NULL, 'Warning', 4),
(5,  0, '数据统计', 'statistics', 1, '/statistics', NULL, 'TrendCharts', 5),
(6,  0, '系统管理', 'system', 1, '/system', NULL, 'Setting', 6),
(7,  0, 'AI助手', 'ai', 1, '/ai', 'ai/index', 'MagicStick', 7),
-- 设备管理子菜单
(10, 2, '设备列表', 'device:list', 1, '/device/list', 'device/list/index', '', 1),
(11, 2, '设备分类', 'device:category', 1, '/device/category', 'device/category/index', '', 2),
(12, 2, '位置管理', 'device:location', 1, '/device/location', 'device/location/index', '', 3),
-- 状态监控子菜单
(20, 3, '实时监控', 'monitor:status', 1, '/monitor/status', 'monitor/status/index', '', 1),
(21, 3, '预警管理', 'monitor:alert', 1, '/monitor/alert', 'monitor/alert/index', '', 2),
-- 故障管理子菜单
(30, 4, '故障上报', 'fault:report', 1, '/fault/report', 'fault/report/index', '', 1),
(31, 4, '工单管理', 'fault:workorder', 1, '/fault/workorder', 'fault/workorder/index', '', 2),
(32, 4, '故障归档', 'fault:archive', 1, '/fault/archive', 'fault/archive/index', '', 3),
-- 统计分析子菜单
(40, 5, '使用率统计', 'statistics:usage', 1, '/statistics/usage', 'statistics/usage/index', '', 1),
(41, 5, '维护成本', 'statistics:cost', 1, '/statistics/cost', 'statistics/cost/index', '', 2),
(42, 5, '故障报表', 'statistics:fault', 1, '/statistics/fault', 'statistics/fault/index', '', 3),
-- 系统管理子菜单
(50, 6, '用户管理', 'system:user', 1, '/system/user', 'system/user/index', '', 1),
(51, 6, '角色管理', 'system:role', 1, '/system/role', 'system/role/index', '', 2),
(52, 6, '操作日志', 'system:log', 1, '/system/log', 'system/log/index', '', 3),
(53, 6, '数据备份', 'system:backup', 1, '/system/backup', 'system/backup/index', '', 4);

-- 管理员拥有所有权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `sys_permission`;

-- 维护员权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5),
(2, 10), (2, 20), (2, 21), (2, 30), (2, 31), (2, 32),
(2, 40), (2, 41), (2, 42), (2, 7);

-- 普通用户权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(3, 1), (3, 2), (3, 3), (3, 4),
(3, 10), (3, 20), (3, 30), (3, 7);

-- ----------------------------
-- 初始化设备分类
-- ----------------------------
INSERT INTO `device_category` (`id`, `parent_id`, `name`, `code`, `icon`, `description`, `sort_order`) VALUES
(1, 0, '计算机设备', 'COMPUTER', 'Monitor', '包括台式机、笔记本、服务器等', 1),
(2, 0, '网络设备', 'NETWORK', 'Connection', '包括路由器、交换机、防火墙等', 2),
(3, 0, '多媒体设备', 'MULTIMEDIA', 'VideoCamera', '包括投影仪、显示屏、音响等', 3),
(4, 0, '安防设备', 'SECURITY', 'Camera', '包括摄像头、门禁、报警器等', 4),
(5, 0, '实验室设备', 'LAB', 'Experiment', '包括各类实验仪器设备', 5),
(6, 0, '办公设备', 'OFFICE', 'Printer', '包括打印机、复印机、扫描仪等', 6),
(7, 1, '台式电脑', 'DESKTOP', '', '台式计算机', 1),
(8, 1, '笔记本电脑', 'LAPTOP', '', '便携式计算机', 2),
(9, 1, '服务器', 'SERVER', '', '服务器设备', 3),
(10, 2, '路由器', 'ROUTER', '', '网络路由设备', 1),
(11, 2, '交换机', 'SWITCH', '', '网络交换设备', 2),
(12, 3, '投影仪', 'PROJECTOR', '', '投影显示设备', 1),
(13, 3, '智慧大屏', 'SMARTSCREEN', '', '智能显示大屏', 2);

-- ----------------------------
-- 初始化位置数据
-- ----------------------------
INSERT INTO `device_location` (`id`, `building`, `floor`, `room`, `full_address`) VALUES
(1, '教学楼A', '1F', '101机房', '教学楼A 1楼 101机房'),
(2, '教学楼A', '2F', '201机房', '教学楼A 2楼 201机房'),
(3, '教学楼B', '1F', '101教室', '教学楼B 1楼 101教室'),
(4, '教学楼B', '3F', '301多媒体教室', '教学楼B 3楼 301多媒体教室'),
(5, '图书馆', '1F', '阅览室', '图书馆 1楼 阅览室'),
(6, '实验楼', '2F', '201实验室', '实验楼 2楼 201实验室'),
(7, '行政楼', '3F', '301办公室', '行政楼 3楼 301办公室'),
(8, '网络中心', '1F', '机房', '网络中心 1楼 机房');

-- ----------------------------
-- 初始化设备数据（示例）
-- ----------------------------
INSERT INTO `device_info` (`id`, `device_code`, `device_name`, `category_id`, `location_id`, `brand`, `model`, `serial_number`, `purchase_date`, `purchase_price`, `warranty_date`, `status`, `online_status`, `tags`, `responsible_person`, `responsible_phone`) VALUES
(1, 'DEV-2026-001', '联想台式电脑#1', 7, 1, '联想', 'ThinkCentre M720', 'SN20240001', '2024-01-15', 4500.00, '2027-01-15', 1, 1, '计算机,教学', '张维修', '13800000002'),
(2, 'DEV-2026-002', '联想台式电脑#2', 7, 1, '联想', 'ThinkCentre M720', 'SN20240002', '2024-01-15', 4500.00, '2027-01-15', 1, 1, '计算机,教学', '张维修', '13800000002'),
(3, 'DEV-2026-003', '戴尔服务器#1', 9, 8, '戴尔', 'PowerEdge R740', 'SN20240003', '2023-06-01', 35000.00, '2026-06-01', 1, 1, '服务器,核心', '张维修', '13800000002'),
(4, 'DEV-2026-004', '华为交换机#1', 11, 8, '华为', 'S5735-L48T4X', 'SN20240004', '2023-08-01', 8000.00, '2026-08-01', 1, 1, '网络,核心', '李技术', '13800000003'),
(5, 'DEV-2026-005', '爱普生投影仪#1', 12, 3, '爱普生', 'CB-X51', 'SN20240005', '2024-02-01', 3200.00, '2027-02-01', 1, 0, '多媒体,教学', '李技术', '13800000003'),
(6, 'DEV-2026-006', '海康摄像头#1', 4, 1, '海康威视', 'DS-2CD2T47G2', 'SN20240006', '2023-09-01', 1200.00, '2026-09-01', 1, 1, '安防,监控', '张维修', '13800000002'),
(7, 'DEV-2026-007', '惠普打印机#1', 6, 7, '惠普', 'LaserJet Pro M404', 'SN20240007', '2024-03-01', 2800.00, '2027-03-01', 2, 0, '办公,打印', '李技术', '13800000003'),
(8, 'DEV-2026-008', '联想笔记本#1', 8, 5, '联想', 'ThinkPad E14', 'SN20240008', '2024-01-20', 6500.00, '2027-01-20', 1, 1, '计算机,移动', '张维修', '13800000002'),
(9, 'DEV-2026-009', '思科路由器#1', 10, 8, '思科', 'ISR 4321', 'SN20240009', '2023-07-01', 12000.00, '2026-07-01', 1, 1, '网络,核心', '李技术', '13800000003'),
(10, 'DEV-2026-010', '三星智慧大屏#1', 13, 4, '三星', 'QM85R', 'SN20240010', '2024-04-01', 18000.00, '2027-04-01', 1, 1, '多媒体,教学', '李技术', '13800000003');

-- ----------------------------
-- 初始化故障数据（示例）
-- ----------------------------
INSERT INTO `fault_report` (`id`, `fault_no`, `device_id`, `device_code`, `device_name`, `fault_type`, `fault_level`, `fault_desc`, `reporter_id`, `reporter_name`, `status`) VALUES
(1, 'FLT-20240416-001', 7, 'DEV-2026-007', '惠普打印机#1', '硬件故障', 2, '打印机卡纸，无法正常打印，已尝试清除卡纸但问题依然存在', 4, '王同学', 2),
(2, 'FLT-20240416-002', 5, 'DEV-2026-005', '爱普生投影仪#1', '显示异常', 3, '投影仪开机后显示花屏，颜色失真，影响正常教学使用', 5, '赵老师', 0);

-- 初始化工单
INSERT INTO `fault_work_order` (`id`, `order_no`, `fault_id`, `assignee_id`, `assignee_name`, `assign_time`, `assigner_id`, `assigner_name`, `expected_time`, `status`) VALUES
(1, 'WO-20240416-001', 1, 2, '张维修', NOW(), 1, '系统管理员', DATE_ADD(NOW(), INTERVAL 2 DAY), 1);

-- 初始化维护成本
INSERT INTO `maintenance_cost` (`device_id`, `device_code`, `device_name`, `cost_type`, `cost_amount`, `cost_date`, `description`, `operator`) VALUES
(7, 'DEV-2026-007', '惠普打印机#1', 'parts', 150.00, '2024-04-16', '更换打印机送纸辊', '张维修'),
(3, 'DEV-2026-003', '戴尔服务器#1', 'labor', 200.00, '2024-03-15', '定期维护保养', '李技术'),
(4, 'DEV-2026-004', '华为交换机#1', 'repair', 500.00, '2024-02-20', '端口故障维修', '李技术');

-- 初始化预警数据
INSERT INTO `device_alert` (`device_id`, `device_code`, `device_name`, `alert_type`, `alert_level`, `alert_value`, `threshold`, `alert_msg`, `status`) VALUES
(3, 'DEV-2026-003', '戴尔服务器#1', 'cpu', 3, '92%', '90%', 'CPU使用率超过阈值，当前92%，阈值90%', 0),
(3, 'DEV-2026-003', '戴尔服务器#1', 'temperature', 2, '75℃', '70℃', '服务器温度偏高，当前75℃，阈值70℃', 0);

