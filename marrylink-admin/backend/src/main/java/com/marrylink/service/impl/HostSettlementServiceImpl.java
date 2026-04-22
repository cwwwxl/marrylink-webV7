package com.marrylink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marrylink.entity.HostSettlement;
import com.marrylink.exception.BusinessException;
import com.marrylink.mapper.HostSettlementMapper;
import com.marrylink.service.IHostSettlementService;
import com.marrylink.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HostSettlementServiceImpl extends ServiceImpl<HostSettlementMapper, HostSettlement>
        implements IHostSettlementService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSettlement(String orderNo, Long orderId, BigDecimal orderAmount,
                                 BigDecimal commissionAmount, Long hostId, String hostName) {
        // 检查是否已存在
        LambdaQueryWrapper<HostSettlement> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(HostSettlement::getOrderId, orderId);
        if (count(checkWrapper) > 0) {
            return;
        }

        HostSettlement settlement = new HostSettlement();
        settlement.setOrderNo(orderNo);
        settlement.setOrderId(orderId);
        settlement.setOrderAmount(orderAmount);
        settlement.setCommissionAmount(commissionAmount);
        settlement.setHostAmount(orderAmount.subtract(commissionAmount));
        settlement.setHostId(hostId);
        settlement.setHostName(hostName);
        settlement.setStatus(1); // 待下发
        save(settlement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disburse(Long id, String payMethod, String payAccount, String payRemark) {
        HostSettlement settlement = getById(id);
        if (settlement == null) {
            throw new BusinessException("结算记录不存在");
        }
        if (settlement.getStatus() != 1) {
            throw new BusinessException("该记录已下发，不可重复操作");
        }

        HostSettlement update = new HostSettlement();
        update.setId(id);
        update.setStatus(2);
        update.setPayMethod(payMethod);
        update.setPayAccount(payAccount);
        update.setPayRemark(payRemark);
        update.setPayTime(LocalDateTime.now());
        update.setOperator(SecurityUtils.getCurrentUsername());
        updateById(update);
    }

    @Override
    public Map<String, Object> getSettlementStats() {
        Map<String, Object> stats = new HashMap<>();

        List<HostSettlement> all = list();

        // 主持人应收总额
        BigDecimal totalHostAmount = all.stream()
                .map(HostSettlement::getHostAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalHostAmount", totalHostAmount);

        // 待下发总额
        BigDecimal pendingDisburse = all.stream()
                .filter(s -> s.getStatus() == 1)
                .map(HostSettlement::getHostAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("pendingDisburse", pendingDisburse);

        // 已下发总额
        BigDecimal disbursedAmount = all.stream()
                .filter(s -> s.getStatus() == 2)
                .map(HostSettlement::getHostAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("disbursedAmount", disbursedAmount);

        // 待下发笔数
        long pendingCount = all.stream().filter(s -> s.getStatus() == 1).count();
        stats.put("pendingCount", pendingCount);

        return stats;
    }
}
