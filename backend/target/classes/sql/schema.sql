-- =============================================
-- 智能校园设备管理系统 数据库建表脚本
-- 数据库: campus_equipment
-- =============================================

CREATE DATABASE IF NOT EXISTS campus_equipment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_equipment;

-- ----------------------------
-- 1. 系统用户表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    `real_name`   VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    `email`       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `gender`      TINYINT      DEFAULT 0 COMMENT '性别：0未知 1男 2女',
    `department`  VARCHAR(100) DEFAULT NULL COMMENT '所属部门',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态：0禁用 1启用',
    `last_login`  DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    `login_ip`    VARCHAR(50)  DEFAULT NULL COMMENT '最后登录IP',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted`     TINYINT      DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ----------------------------
-- 2. 角色表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name`   VARCHAR(50)  NOT NULL COMMENT '角色名称',
    `role_code`   VARCHAR(50)  NOT NULL COMMENT '角色编码',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态：0禁用 1启用',
    `deleted`     TINYINT      DEFAULT 0,
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ----------------------------
-- 3. 用户角色关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT NOT NULL COMMENT '用户ID',
    `role_id`     BIGINT NOT NULL COMMENT '角色ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ----------------------------
-- 4. 权限表
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `parent_id`       BIGINT       DEFAULT 0 COMMENT '父权限ID，0为顶级',
    `perm_name`       VARCHAR(100) NOT NULL COMMENT '权限名称',
    `perm_code`       VARCHAR(100) NOT NULL COMMENT '权限编码',
    `perm_type`       TINYINT      DEFAULT 1 COMMENT '类型：1菜单 2按钮 3接口',
    `path`            VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    `component`       VARCHAR(200) DEFAULT NULL COMMENT '前端组件',
    `icon`            VARCHAR(100) DEFAULT NULL COMMENT '图标',
    `sort_order`      INT          DEFAULT 0 COMMENT '排序',
    `status`          TINYINT      DEFAULT 1,
    `deleted`         TINYINT      DEFAULT 0,
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- ----------------------------
-- 5. 角色权限关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
    `id`            BIGINT NOT NULL AUTO_INCREMENT,
    `role_id`       BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_perm` (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ----------------------------
-- 6. 操作日志表
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id`        BIGINT        DEFAULT NULL COMMENT '操作用户ID',
    `username`       VARCHAR(50)   DEFAULT NULL COMMENT '操作用户名',
    `module`         VARCHAR(50)   DEFAULT NULL COMMENT '操作模块',
    `operation`      VARCHAR(100)  DEFAULT NULL COMMENT '操作描述',
    `method`         VARCHAR(200)  DEFAULT NULL COMMENT '请求方法',
    `request_url`    VARCHAR(500)  DEFAULT NULL COMMENT '请求URL',
    `request_method` VARCHAR(10)   DEFAULT NULL COMMENT '请求方式',
    `request_params` TEXT          DEFAULT NULL COMMENT '请求参数',
    `response_data`  TEXT          DEFAULT NULL COMMENT '响应数据',
    `ip`             VARCHAR(50)   DEFAULT NULL COMMENT '操作IP',
    `status`         TINYINT       DEFAULT 1 COMMENT '操作状态：0失败 1成功',
    `error_msg`      TEXT          DEFAULT NULL COMMENT '错误信息',
    `cost_time`      BIGINT        DEFAULT NULL COMMENT '耗时（毫秒）',
    `create_time`    DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ----------------------------
-- 7. 数据备份记录表
-- ----------------------------
DROP TABLE IF EXISTS `sys_backup_record`;
CREATE TABLE `sys_backup_record` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `file_name`   VARCHAR(200) NOT NULL COMMENT '备份文件名',
    `file_path`   VARCHAR(500) NOT NULL COMMENT '备份文件路径',
    `file_size`   BIGINT       DEFAULT NULL COMMENT '文件大小（字节）',
    `backup_type` TINYINT      DEFAULT 1 COMMENT '备份类型：1手动 2自动',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态：0失败 1成功',
    `remark`      VARCHAR(500) DEFAULT NULL,
    `operator`    VARCHAR(50)  DEFAULT NULL COMMENT '操作人',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据备份记录表';

-- ----------------------------
-- 8. 设备分类表
-- ----------------------------
DROP TABLE IF EXISTS `device_category`;
CREATE TABLE `device_category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `parent_id`   BIGINT       DEFAULT 0 COMMENT '父分类ID',
    `name`        VARCHAR(100) NOT NULL COMMENT '分类名称',
    `code`        VARCHAR(50)  NOT NULL COMMENT '分类编码',
    `icon`        VARCHAR(100) DEFAULT NULL COMMENT '图标',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `sort_order`  INT          DEFAULT 0 COMMENT '排序',
    `deleted`     TINYINT      DEFAULT 0,
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备分类表';

-- ----------------------------
-- 9. 设备位置表
-- ----------------------------
DROP TABLE IF EXISTS `device_location`;
CREATE TABLE `device_location` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '位置ID',
    `building`    VARCHAR(100) NOT NULL COMMENT '楼栋',
    `floor`       VARCHAR(20)  DEFAULT NULL COMMENT '楼层',
    `room`        VARCHAR(100) DEFAULT NULL COMMENT '房间/区域',
    `full_address` VARCHAR(300) DEFAULT NULL COMMENT '完整地址',
    `longitude`   DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
    `latitude`    DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
    `remark`      VARCHAR(500) DEFAULT NULL,
    `deleted`     TINYINT      DEFAULT 0,
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备位置表';

-- ----------------------------
-- 10. 设备信息表
-- ----------------------------
DROP TABLE IF EXISTS `device_info`;
CREATE TABLE `device_info` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '设备ID',
    `device_code`     VARCHAR(50)  NOT NULL COMMENT '设备编号',
    `device_name`     VARCHAR(200) NOT NULL COMMENT '设备名称',
    `category_id`     BIGINT       DEFAULT NULL COMMENT '分类ID',
    `location_id`     BIGINT       DEFAULT NULL COMMENT '位置ID',
    `brand`           VARCHAR(100) DEFAULT NULL COMMENT '品牌',
    `model`           VARCHAR(100) DEFAULT NULL COMMENT '型号',
    `serial_number`   VARCHAR(100) DEFAULT NULL COMMENT '序列号',
    `purchase_date`   DATE         DEFAULT NULL COMMENT '购买日期',
    `purchase_price`  DECIMAL(12,2) DEFAULT NULL COMMENT '购买价格',
    `warranty_date`   DATE         DEFAULT NULL COMMENT '保修到期日',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态：0停用 1正常 2维修中 3报废',
    `online_status`   TINYINT      DEFAULT 0 COMMENT '在线状态：0离线 1在线',
    `tags`            VARCHAR(500) DEFAULT NULL COMMENT '标签（逗号分隔）',
    `description`     TEXT         DEFAULT NULL COMMENT '设备描述',
    `image_url`       VARCHAR(500) DEFAULT NULL COMMENT '设备图片',
    `responsible_person` VARCHAR(50) DEFAULT NULL COMMENT '负责人',
    `responsible_phone`  VARCHAR(20) DEFAULT NULL COMMENT '负责人电话',
    `last_maintain_time` DATETIME   DEFAULT NULL COMMENT '最后维护时间',
    `deleted`         TINYINT      DEFAULT 0,
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_code` (`device_code`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_location_id` (`location_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备信息表';

-- ----------------------------
-- 11. 设备状态记录表
-- ----------------------------
DROP TABLE IF EXISTS `device_status_record`;
CREATE TABLE `device_status_record` (
    `id`              BIGINT         NOT NULL AUTO_INCREMENT,
    `device_id`       BIGINT         NOT NULL COMMENT '设备ID',
    `device_code`     VARCHAR(50)    DEFAULT NULL COMMENT '设备编号',
    `cpu_usage`       DECIMAL(5,2)   DEFAULT NULL COMMENT 'CPU使用率(%)',
    `memory_usage`    DECIMAL(5,2)   DEFAULT NULL COMMENT '内存使用率(%)',
    `disk_usage`      DECIMAL(5,2)   DEFAULT NULL COMMENT '磁盘使用率(%)',
    `temperature`     DECIMAL(5,2)   DEFAULT NULL COMMENT '温度(℃)',
    `power_status`    TINYINT        DEFAULT 1 COMMENT '电源状态：0关机 1开机',
    `network_status`  TINYINT        DEFAULT 1 COMMENT '网络状态：0断网 1正常',
    `run_duration`    BIGINT         DEFAULT NULL COMMENT '运行时长（分钟）',
    `extra_params`    JSON           DEFAULT NULL COMMENT '扩展参数（JSON）',
    `record_time`     DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    PRIMARY KEY (`id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_record_time` (`record_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备状态记录表';

-- ----------------------------
-- 12. 设备预警表
-- ----------------------------
DROP TABLE IF EXISTS `device_alert`;
CREATE TABLE `device_alert` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `device_id`    BIGINT       NOT NULL COMMENT '设备ID',
    `device_code`  VARCHAR(50)  DEFAULT NULL,
    `device_name`  VARCHAR(200) DEFAULT NULL,
    `alert_type`   VARCHAR(50)  NOT NULL COMMENT '预警类型：cpu/memory/disk/temperature/offline',
    `alert_level`  TINYINT      DEFAULT 1 COMMENT '预警级别：1低 2中 3高 4紧急',
    `alert_value`  VARCHAR(100) DEFAULT NULL COMMENT '触发值',
    `threshold`    VARCHAR(100) DEFAULT NULL COMMENT '阈值',
    `alert_msg`    VARCHAR(500) DEFAULT NULL COMMENT '预警信息',
    `status`       TINYINT      DEFAULT 0 COMMENT '处理状态：0未处理 1已处理 2忽略',
    `handler`      VARCHAR(50)  DEFAULT NULL COMMENT '处理人',
    `handle_time`  DATETIME     DEFAULT NULL COMMENT '处理时间',
    `handle_remark` VARCHAR(500) DEFAULT NULL COMMENT '处理备注',
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备预警表';

-- ----------------------------
-- 13. 故障上报表
-- ----------------------------
DROP TABLE IF EXISTS `fault_report`;
CREATE TABLE `fault_report` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '故障ID',
    `fault_no`      VARCHAR(50)  NOT NULL COMMENT '故障单号',
    `device_id`     BIGINT       NOT NULL COMMENT '设备ID',
    `device_code`   VARCHAR(50)  DEFAULT NULL,
    `device_name`   VARCHAR(200) DEFAULT NULL,
    `fault_type`    VARCHAR(50)  DEFAULT NULL COMMENT '故障类型',
    `fault_level`   TINYINT      DEFAULT 2 COMMENT '故障级别：1低 2中 3高 4紧急',
    `fault_desc`    TEXT         NOT NULL COMMENT '故障描述',
    `fault_images`  VARCHAR(1000) DEFAULT NULL COMMENT '故障图片（逗号分隔）',
    `reporter_id`   BIGINT       DEFAULT NULL COMMENT '上报人ID',
    `reporter_name` VARCHAR(50)  DEFAULT NULL COMMENT '上报人姓名',
    `report_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间',
    `status`        TINYINT      DEFAULT 0 COMMENT '状态：0待派单 1已派单 2处理中 3已完成 4已归档',
    `ai_diagnosis`  TEXT         DEFAULT NULL COMMENT 'AI诊断建议',
    `deleted`       TINYINT      DEFAULT 0,
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_fault_no` (`fault_no`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='故障上报表';

-- ----------------------------
-- 14. 故障工单表
-- ----------------------------
DROP TABLE IF EXISTS `fault_work_order`;
CREATE TABLE `fault_work_order` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '工单ID',
    `order_no`        VARCHAR(50)  NOT NULL COMMENT '工单号',
    `fault_id`        BIGINT       NOT NULL COMMENT '故障ID',
    `assignee_id`     BIGINT       DEFAULT NULL COMMENT '派单给（维护员ID）',
    `assignee_name`   VARCHAR(50)  DEFAULT NULL COMMENT '维护员姓名',
    `assign_time`     DATETIME     DEFAULT NULL COMMENT '派单时间',
    `assigner_id`     BIGINT       DEFAULT NULL COMMENT '派单人ID',
    `assigner_name`   VARCHAR(50)  DEFAULT NULL COMMENT '派单人姓名',
    `expected_time`   DATETIME     DEFAULT NULL COMMENT '预计完成时间',
    `actual_start`    DATETIME     DEFAULT NULL COMMENT '实际开始时间',
    `actual_end`      DATETIME     DEFAULT NULL COMMENT '实际完成时间',
    `handle_desc`     TEXT         DEFAULT NULL COMMENT '处理描述',
    `handle_result`   TINYINT      DEFAULT NULL COMMENT '处理结果：1已修复 2无法修复 3需更换',
    `cost`            DECIMAL(10,2) DEFAULT NULL COMMENT '维修费用',
    `parts_replaced`  VARCHAR(500) DEFAULT NULL COMMENT '更换配件',
    `status`          TINYINT      DEFAULT 0 COMMENT '状态：0待处理 1处理中 2已完成 3已关闭',
    `evaluate_score`  TINYINT      DEFAULT NULL COMMENT '评价分数（1-5）',
    `evaluate_remark` VARCHAR(500) DEFAULT NULL COMMENT '评价备注',
    `deleted`         TINYINT      DEFAULT 0,
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_fault_id` (`fault_id`),
    KEY `idx_assignee_id` (`assignee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='故障工单表';

-- ----------------------------
-- 15. 维护成本表
-- ----------------------------
DROP TABLE IF EXISTS `maintenance_cost`;
CREATE TABLE `maintenance_cost` (
    `id`           BIGINT         NOT NULL AUTO_INCREMENT,
    `device_id`    BIGINT         NOT NULL COMMENT '设备ID',
    `device_code`  VARCHAR(50)    DEFAULT NULL,
    `device_name`  VARCHAR(200)   DEFAULT NULL,
    `cost_type`    VARCHAR(50)    NOT NULL COMMENT '费用类型：repair/parts/labor/other',
    `cost_amount`  DECIMAL(12,2)  NOT NULL COMMENT '费用金额',
    `cost_date`    DATE           NOT NULL COMMENT '费用日期',
    `work_order_id` BIGINT        DEFAULT NULL COMMENT '关联工单ID',
    `description`  VARCHAR(500)   DEFAULT NULL COMMENT '费用说明',
    `operator`     VARCHAR(50)    DEFAULT NULL COMMENT '录入人',
    `deleted`      TINYINT        DEFAULT 0,
    `create_time`  DATETIME       DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_cost_date` (`cost_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维护成本表';
