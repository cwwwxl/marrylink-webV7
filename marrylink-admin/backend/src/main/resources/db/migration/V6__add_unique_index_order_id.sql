-- 防止同一订单重复生成佣金记录、结算记录和账单
ALTER TABLE `commission_record` ADD UNIQUE INDEX `uk_order_id` (`order_id`);
ALTER TABLE `host_settlement` ADD UNIQUE INDEX `uk_order_id` (`order_id`);
ALTER TABLE `host_commission_bill` ADD UNIQUE INDEX `uk_order_id` (`order_id`);
