-- 主持人结算下发记录表
CREATE TABLE IF NOT EXISTS `host_settlement` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_no` VARCHAR(64) NOT NULL COMMENT '关联订单号',
    `order_id` BIGINT NOT NULL COMMENT '关联订单ID',
    `order_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `commission_amount` DECIMAL(10,2) NOT NULL COMMENT '平台抽成金额',
    `host_amount` DECIMAL(10,2) NOT NULL COMMENT '主持人应收金额',
    `host_id` BIGINT NOT NULL COMMENT '主持人ID',
    `host_name` VARCHAR(100) DEFAULT NULL COMMENT '主持人姓名',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1-待下发 2-已下发',
    `pay_method` VARCHAR(50) DEFAULT NULL COMMENT '下发方式(支付宝/银行卡/微信/现金)',
    `pay_account` VARCHAR(100) DEFAULT NULL COMMENT '收款账号',
    `pay_remark` VARCHAR(255) DEFAULT NULL COMMENT '下发备注',
    `pay_time` DATETIME DEFAULT NULL COMMENT '下发时间',
    `operator` VARCHAR(100) DEFAULT NULL COMMENT '操作人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX `idx_order_no` (`order_no`),
    INDEX `idx_host_id` (`host_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主持人结算下发记录';
