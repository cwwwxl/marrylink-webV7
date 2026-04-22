-- 主持人佣金账单表
CREATE TABLE IF NOT EXISTS `host_commission_bill` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `bill_no` VARCHAR(64) NOT NULL COMMENT '账单编号',
    `order_no` VARCHAR(64) NOT NULL COMMENT '关联订单号',
    `order_id` BIGINT NOT NULL COMMENT '关联订单ID',
    `order_amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    `commission_rate` DECIMAL(5,2) NOT NULL COMMENT '佣金比例(%)',
    `commission_amount` DECIMAL(10,2) NOT NULL COMMENT '应付佣金金额',
    `host_id` BIGINT NOT NULL COMMENT '主持人ID',
    `host_name` VARCHAR(100) DEFAULT NULL COMMENT '主持人姓名',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1-待支付 2-已支付 3-已逾期',
    `due_date` DATETIME NOT NULL COMMENT '最后支付期限(创建后7天)',
    `paid_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE INDEX `idx_bill_no` (`bill_no`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_host_id` (`host_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_due_date` (`due_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主持人佣金账单';

-- 给host表添加收款账户字段
ALTER TABLE `host` ADD COLUMN `pay_account_type` VARCHAR(50) DEFAULT NULL COMMENT '收款方式(支付宝/银行卡/微信)' AFTER `description`;
ALTER TABLE `host` ADD COLUMN `pay_account_no` VARCHAR(100) DEFAULT NULL COMMENT '收款账号' AFTER `pay_account_type`;
ALTER TABLE `host` ADD COLUMN `pay_account_name` VARCHAR(100) DEFAULT NULL COMMENT '收款人姓名' AFTER `pay_account_no`;
