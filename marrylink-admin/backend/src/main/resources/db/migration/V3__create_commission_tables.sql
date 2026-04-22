-- 平台抽成配置表
CREATE TABLE IF NOT EXISTS `commission_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `commission_rate` DECIMAL(5,2) NOT NULL DEFAULT 10.00 COMMENT '抽成比例(%)',
    `min_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '最低抽成金额',
    `is_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用 1-启用 0-禁用',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台抽成配置';

-- 插入默认配置
INSERT INTO `commission_config` (`commission_rate`, `min_amount`, `is_enabled`, `remark`)
VALUES (10.00, 0.00, 1, '默认抽成比例10%');

-- 平台抽成记录表
CREATE TABLE IF NOT EXISTS `commission_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_no` VARCHAR(64) NOT NULL COMMENT '关联订单号',
    `order_id` BIGINT NOT NULL COMMENT '关联订单ID',
    `order_amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    `commission_rate` DECIMAL(5,2) NOT NULL COMMENT '抽成比例(%)',
    `commission_amount` DECIMAL(10,2) NOT NULL COMMENT '抽成金额',
    `host_id` BIGINT DEFAULT NULL COMMENT '主持人ID',
    `host_name` VARCHAR(100) DEFAULT NULL COMMENT '主持人姓名',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1-待结算 2-已结算',
    `settled_time` DATETIME DEFAULT NULL COMMENT '结算时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX `idx_order_no` (`order_no`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台抽成记录';

-- 平台提现记录表
CREATE TABLE IF NOT EXISTS `platform_withdrawal` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `withdrawal_no` VARCHAR(64) NOT NULL COMMENT '提现单号',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '提现金额',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1-待处理 2-处理中 3-已完成 4-已拒绝',
    `account_type` VARCHAR(50) DEFAULT NULL COMMENT '收款账户类型(支付宝/银行卡/微信)',
    `account_no` VARCHAR(100) DEFAULT NULL COMMENT '收款账号',
    `account_name` VARCHAR(100) DEFAULT NULL COMMENT '收款人姓名',
    `operator` VARCHAR(100) DEFAULT NULL COMMENT '操作人',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `process_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE INDEX `idx_withdrawal_no` (`withdrawal_no`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台提现记录';
