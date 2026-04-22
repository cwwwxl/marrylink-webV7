package com.marrylink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marrylink.entity.CommissionRecord;
import com.marrylink.entity.Order;

import java.math.BigDecimal;
import java.util.Map;

public interface ICommissionRecordService extends IService<CommissionRecord> {

    /**
     * 订单完成时生成抽成记录
     */
    void generateCommission(Order order);

    /**
     * 获取抽成统计数据
     */
    Map<String, Object> getCommissionStats();

    /**
     * 批量结算抽成记录
     */
    void settleCommissions(java.util.List<Long> ids);

    /**
     * 获取可提现余额（已结算未提现金额）
     */
    BigDecimal getWithdrawableBalance();
}
