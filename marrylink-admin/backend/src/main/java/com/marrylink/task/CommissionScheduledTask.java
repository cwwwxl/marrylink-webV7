package com.marrylink.task;

import com.marrylink.service.IHostCommissionBillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 佣金账单定时任务
 * 自动标记逾期账单，逾期主持人将被限制登录
 */
@Slf4j
@Component
public class CommissionScheduledTask {

    @Resource
    private IHostCommissionBillService hostCommissionBillService;

    /**
     * 每天凌晨1点自动检查并标记逾期账单
     * 主持人7天未支付佣金，账单将被标记为已逾期，登录将被限制
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void markOverdueBills() {
        log.info("开始执行佣金账单逾期检查定时任务...");
        try {
            int count = hostCommissionBillService.markOverdueBills();
            if (count > 0) {
                log.info("已标记 {} 条逾期佣金账单，相关主持人登录将被限制", count);
            } else {
                log.info("没有需要标记的逾期账单");
            }
        } catch (Exception e) {
            log.error("佣金账单逾期检查定时任务执行失败", e);
        }
    }
}
