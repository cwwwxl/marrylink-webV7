package com.marrylink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marrylink.entity.HostCommissionBill;

import java.math.BigDecimal;
import java.util.Map;

public interface IHostCommissionBillService extends IService<HostCommissionBill> {

    /**
     * 创建佣金账单
     */
    void createBill(String orderNo, Long orderId, BigDecimal orderAmount,
                    BigDecimal commissionRate, BigDecimal commissionAmount,
                    Long hostId, String hostName);

    /**
     * 主持人支付佣金
     */
    void payBill(Long billId, Long hostId);

    /**
     * 检查主持人是否有逾期未支付的账单(超过7天)
     * @return true=有逾期账单，应限制登录
     */
    boolean hasOverdueBills(Long hostId);

    /**
     * 将逾期账单标记为已逾期状态
     */
    int markOverdueBills();

    /**
     * 获取佣金账单统计
     */
    Map<String, Object> getBillStats();
}
