-- =============================================
-- 智能校园设备管理系统 - 功能扩展表
-- 添加：设备生命周期、预测性维护、AI聊天
-- =============================================

USE campus_equipment;

-- ----------------------------
-- 1. 设备生命周期记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `device_lifecycle` (
    `id`              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `device_id`       BIGINT         NOT NULL COMMENT '设备ID',
    `device_code`     VARCHAR(50)    DEFAULT NULL COMMENT '设备编号',
    `device_name`     VARCHAR(200)   DEFAULT NULL COMMENT '设备名称',
    `lifecycle_type`  VARCHAR(50)    NOT NULL COMMENT '生命周期类型：purchase-采购 maintenance-维修 replacement-更换 inspection-巡检 retirement-报废',
    `event_date`      DATE           NOT NULL COMMENT '事件日期',
    `description`      TEXT           DEFAULT NULL COMMENT '事件描述',
    `cost`            DECIMAL(12,2)  DEFAULT NULL COMMENT '相关费用',
    `vendor`          VARCHAR(200)   DEFAULT NULL COMMENT '供应商/维修商',
    `contact_person`  VARCHAR(50)    DEFAULT NULL COMMENT '联系人',
    `contact_phone`   VARCHAR(20)    DEFAULT NULL COMMENT '联系电话',
    `operator`        VARCHAR(50)    DEFAULT NULL COMMENT '操作人',
    `remark`          VARCHAR(500)   DEFAULT NULL COMMENT '备注',
    `attachments`     VARCHAR(1000)  DEFAULT NULL COMMENT '附件（多个逗号分隔）',
    `deleted`         TINYINT        DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
    `create_time`     DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_lifecycle_type` (`lifecycle_type`),
    KEY `idx_event_date` (`event_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备生命周期记录表';

-- ----------------------------
-- 2. 预测性维护记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `predictive_maintenance` (
    `id`              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `device_id`       BIGINT         NOT NULL COMMENT '设备ID',
    `device_code`     VARCHAR(50)    DEFAULT NULL COMMENT '设备编号',
    `device_name`     VARCHAR(200)   DEFAULT NULL COMMENT '设备名称',
    `predict_type`    VARCHAR(50)    NOT NULL COMMENT '预测类型：failure-故障预测 wear-磨损预测 performance-性能预测',
    `risk_level`      TINYINT        DEFAULT 1 COMMENT '预测风险等级：1-低 2-中 3-高 4-紧急',
    `probability`     INT            DEFAULT 0 COMMENT '预测概率（0-100）',
    `predict_time`    DATETIME       DEFAULT NULL COMMENT '预测发生时间',
    `description`     TEXT           DEFAULT NULL COMMENT '预测描述',
    `suggestion`      TEXT           DEFAULT NULL COMMENT '建议措施',
    `analysis_basis`  TEXT           DEFAULT NULL COMMENT 'AI分析依据',
    `status`          TINYINT        DEFAULT 0 COMMENT '处理状态：0-未处理 1-已处理 2-忽略',
    `handler`         VARCHAR(50)    DEFAULT NULL COMMENT '处理人',
    `handle_time`     DATETIME       DEFAULT NULL COMMENT '处理时间',
    `handle_remark`   VARCHAR(500)   DEFAULT NULL COMMENT '处理备注',
    `deleted`         TINYINT        DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
    `create_time`     DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_risk_level` (`risk_level`),
    KEY `idx_status` (`status`),
    KEY `idx_predict_time` (`predict_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预测性维护记录表';

-- ----------------------------
-- 3. AI聊天会话表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `ai_chat_session` (
    `id`              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    `user_id`         BIGINT         DEFAULT NULL COMMENT '用户ID',
    `title`           VARCHAR(200)    DEFAULT NULL COMMENT '会话标题',
    `session_type`    VARCHAR(50)    DEFAULT 'general' COMMENT '会话类型：device-设备咨询 fault-故障咨询 general-通用问答',
    `device_id`       BIGINT         DEFAULT NULL COMMENT '关联设备ID',
    `fault_id`        BIGINT         DEFAULT NULL COMMENT '关联故障ID',
    `message_count`   INT             DEFAULT 0 COMMENT '消息数量',
    `last_active_time` DATETIME       DEFAULT NULL COMMENT '最后活跃时间',
    `deleted`         TINYINT        DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
    `create_time`     DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_session_type` (`session_type`),
    KEY `idx_last_active` (`last_active_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天会话表';

-- ----------------------------
-- 4. AI聊天消息表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `ai_chat_message` (
    `id`              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `session_id`      BIGINT         NOT NULL COMMENT '会话ID',
    `role`            VARCHAR(20)    NOT NULL COMMENT '消息角色：user-用户 assistant-助手 system-系统',
    `content`         TEXT           NOT NULL COMMENT '消息内容',
    `message_type`    VARCHAR(20)    DEFAULT 'text' COMMENT '消息类型：text-文本 image-图片 card-卡片',
    `metadata`        JSON           DEFAULT NULL COMMENT '关联数据（如设备ID、故障ID等）',
    `cost_time`       BIGINT         DEFAULT NULL COMMENT '耗时（毫秒）',
    `deleted`         TINYINT        DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
    `create_time`     DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天消息表';

-- ----------------------------
-- 5. 插入测试数据 - 设备生命周期
-- ----------------------------
INSERT INTO `device_lifecycle` (`device_id`, `device_code`, `device_name`, `lifecycle_type`, `event_date`, `description`, `cost`, `vendor`, `operator`, `create_time`) VALUES
(1, 'DEV001', '核心交换机', 'purchase', '2023-01-15', '采购华为S5735S-L48T4S-A1核心交换机', 15800.00, '华为官方授权经销商', '系统管理员', NOW()),
(1, 'DEV001', '核心交换机', 'maintenance', '2023-06-20', '例行维护，清理灰尘，更换散热风扇', 800.00, '校园网络维护组', '维护员张三', NOW()),
(1, 'DEV001', '核心交换机', 'inspection', '2023-12-01', '年度设备巡检，检查端口和配置', 0.00, NULL, '维护员李四', NOW()),
(2, 'DEV002', '服务器集群-01', 'purchase', '2022-06-01', '采购Dell PowerEdge R750服务器', 85000.00, 'Dell官方授权经销商', '系统管理员', NOW()),
(2, 'DEV002', '服务器集群-01', 'maintenance', '2023-08-15', '硬盘健康检查，发现一块硬盘有坏道，及时更换', 1200.00, 'Dell售后服务', '维护员王五', NOW());

-- ----------------------------
-- 6. 插入测试数据 - 预测性维护
-- ----------------------------
INSERT INTO `predictive_maintenance` (`device_id`, `device_code`, `device_name`, `predict_type`, `risk_level`, `probability`, `predict_time`, `description`, `suggestion`, `analysis_basis`, `status`, `create_time`) VALUES
(1, 'DEV001', '核心交换机', 'failure', 3, 65, DATE_ADD(NOW(), INTERVAL 30 DAY), '基于最近30天的CPU使用率和温度数据分析，设备可能在30天内出现性能下降', '1. 建议本周内进行一次全面清理和固件升级\n2. 准备备用设备以应对突发故障\n3. 加强监控频率', 'CPU平均使用率从45%上升至62%，温度从38℃上升至45℃，呈上升趋势', 0, NOW()),
(2, 'DEV002', '服务器集群-01', 'wear', 2, 35, DATE_ADD(NOW(), INTERVAL 90 DAY), '硬盘写入量接近设计寿命，建议关注', '1. 建议在60天内进行硬盘健康检查\n2. 考虑添加新硬盘扩容\n3. 备份重要数据', '硬盘累计写入量已达设计寿命的78%', 0, NOW()),
(3, 'DEV003', '监控摄像头-01', 'performance', 1, 15, DATE_ADD(NOW(), INTERVAL 60 DAY), '存储空间使用率达85%，预计60天后需要清理或扩容', '1. 设置自动清理策略\n2. 考虑升级存储方案\n3. 评估是否需要扩容', '近3个月存储增长率分析', 1, NOW());

-- ----------------------------
-- 7. 插入测试数据 - AI聊天会话
-- ----------------------------
INSERT INTO `ai_chat_session` (`user_id`, `title`, `session_type`, `message_count`, `last_active_time`, `create_time`) VALUES
(1, '如何申请设备维修？', 'general', 4, NOW(), NOW()),
(1, '服务器故障咨询', 'device', 6, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(2, '报修流程咨询', 'general', 3, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO `ai_chat_message` (`session_id`, `role`, `content`, `create_time`) VALUES
(1, 'user', '如何申请设备维修？', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(1, 'assistant', '您好！申请设备维修的流程如下：\n\n1. **登录系统**：使用您的账号登录智能校园设备管理系统\n2. **故障上报**：进入「故障管理」→「故障上报」页面\n3. **填写信息**：选择故障设备，描述故障现象\n4. **提交工单**：系统会自动生成维修工单\n\n如需紧急维修，请直接联系维护部门电话：400-xxx-xxxx', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(1, 'user', '维修需要收费吗？', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(1, 'assistant', '关于维修费用，需要根据以下情况判断：\n\n1. **保修期内**：设备在保修期内，非人为损坏，维修免费\n2. **保修期外**：根据故障情况，可能需要收取材料费和人工费\n3. **人为损坏**：需要自行承担全部维修费用\n\n您可以在系统中查看设备详情了解保修状态。如有疑问，欢迎继续咨询！', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(2, 'user', '服务器连接不上怎么办？', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(2, 'assistant', '服务器连接不上的常见原因和解决方法：\n\n1. **检查网络**：确认您的电脑与服务器网络是否连通\n2. **检查端口**：确认SSH端口（默认22）是否开放\n3. **检查防火墙**：确认防火墙规则是否阻止了连接\n4. **查看状态**：登录系统查看服务器在线状态\n\n如果以上都无法解决，建议提交故障工单，维护人员会尽快处理。', DATE_SUB(NOW(), INTERVAL 3 HOUR));

-- ----------------------------
-- 8. 添加索引优化
-- ----------------------------
-- 为 device_lifecycle 添加复合索引
CREATE INDEX idx_device_type_date ON device_lifecycle(device_id, lifecycle_type, event_date);

-- 为 predictive_maintenance 添加复合索引
CREATE INDEX idx_device_risk ON predictive_maintenance(device_id, risk_level, status);

-- 为 ai_chat_session 添加用户会话索引
CREATE INDEX idx_user_active ON ai_chat_session(user_id, last_active_time);

-- 为 ai_chat_message 添加会话消息索引
CREATE INDEX idx_session_time ON ai_chat_message(session_id, create_time);

COMMIT;
