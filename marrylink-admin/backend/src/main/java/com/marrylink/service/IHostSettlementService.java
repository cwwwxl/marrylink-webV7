package com.marrylink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marrylink.entity.HostSettlement;

import java.math.BigDecimal;
import java.util.Map;

public interface IHostSettlementService extends IService<HostSettlement> {

    /**
     * 订单完成时生成主持人结算记录
     * @param orderNo 订单号
     * @param orderId 订单ID
     * @param orderAmount 订单总金额
     * @param commissionAmount 平台抽成金额
     * @param hostId 主持人ID
     * @param hostName 主持人姓名
     */
    void createSettlement(String orderNo, Long orderId, BigDecimal orderAmount,
                          BigDecimal commissionAmount, Long hostId, String hostName);

    /**
     * 手动下发：标记为已下发
     */
    void disburse(Long id, String payMethod, String payAccount, String payRemark);

    /**
     * 获取主持人下发统计
     */
    Map<String, Object> getSettlementStats();
}
