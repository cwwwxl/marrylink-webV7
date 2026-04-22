package com.marrylink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marrylink.entity.CommissionConfig;
import com.marrylink.entity.CommissionRecord;
import com.marrylink.entity.Order;
import com.marrylink.entity.PlatformWithdrawal;
import com.marrylink.exception.BusinessException;
import com.marrylink.mapper.CommissionRecordMapper;
import com.marrylink.service.ICommissionConfigService;
import com.marrylink.service.ICommissionRecordService;
import com.marrylink.service.IHostCommissionBillService;
import com.marrylink.service.IHostSettlementService;
import com.marrylink.service.IPlatformWithdrawalService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommissionRecordServiceImpl extends ServiceImpl<CommissionRecordMapper, CommissionRecord>
        implements ICommissionRecordService {

    @Resource
    private ICommissionConfigService commissionConfigService;

    @Resource
    @Lazy
    private IPlatformWithdrawalService platformWithdrawalService;

    @Resource
    @Lazy
    private IHostSettlementService hostSettlementService;

    @Resource
    @Lazy
    private IHostCommissionBillService hostCommissionBillService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateCommission(Order order) {
        // 检查是否已经为该订单生成过抽成
        LambdaQueryWrapper<CommissionRecord> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(CommissionRecord::getOrderId, order.getId());
        if (count(checkWrapper) > 0) {
            return; // 已存在抽成记录，跳过
        }

        // 获取当前生效的抽成配置
        CommissionConfig config = commissionConfigService.getActiveConfig();
        if (config == null) {
            return; // 没有启用的抽成配置，跳过
        }

        BigDecimal orderAmount = order.getAmount();
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return; // 订单金额无效，跳过
        }

        // 计算抽成金额 = 订单金额 * 抽成比例 / 100
        BigDecimal rate = config.getCommissionRate();
        BigDecimal commissionAmount = orderAmount.multiply(rate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        // 检查最低抽成金额
        BigDecimal minAmount = config.getMinAmount() != null ? config.getMinAmount() : BigDecimal.ZERO;
        if (commissionAmount.compareTo(minAmount) < 0) {
            commissionAmount = minAmount;
        }

        // 创建抽成记录（待结算，主持人支付佣金后自动变为已结算）
        CommissionRecord record = new CommissionRecord();
        record.setOrderNo(order.getOrderNo());
        record.setOrderId(order.getId());
        record.setOrderAmount(orderAmount);
        record.setCommissionRate(rate);
        record.setCommissionAmount(commissionAmount);
        record.setHostId(order.getHostId());
        record.setHostName(order.getHostName());
        record.setStatus(1); // 待结算
        save(record);

        // 订单完成 → 全额打给主持人（后台可设置收款账号），自动标记为已下发
        hostSettlementService.createSettlement(
                order.getOrderNo(), order.getId(), orderAmount,
                BigDecimal.ZERO, order.getHostId(), order.getHostName());

        // 发送佣金账单给主持人（主持人需在7天内支付，逾期将限制登录）
        hostCommissionBillService.createBill(
                order.getOrderNo(), order.getId(), orderAmount,
                rate, commissionAmount, order.getHostId(), order.getHostName());
    }

    @Override
    public Map<String, Object> getCommissionStats() {
        Map<String, Object> stats = new HashMap<>();

        // 总抽成金额
        LambdaQueryWrapper<CommissionRecord> totalWrapper = new LambdaQueryWrapper<>();
        List<CommissionRecord> allRecords = list(totalWrapper);
        BigDecimal totalCommission = allRecords.stream()
                .map(CommissionRecord::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalCommission", totalCommission);

        // 待结算金额
        BigDecimal pendingAmount = allRecords.stream()
                .filter(r -> r.getStatus() == 1)
                .map(CommissionRecord::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("pendingAmount", pendingAmount);

        // 已结算金额
        BigDecimal settledAmount = allRecords.stream()
                .filter(r -> r.getStatus() == 2)
                .map(CommissionRecord::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("settledAmount", settledAmount);

        // 已提现金额
        LambdaQueryWrapper<PlatformWithdrawal> wWrapper = new LambdaQueryWrapper<>();
        wWrapper.eq(PlatformWithdrawal::getStatus, 3); // 已完成的提现
        List<PlatformWithdrawal> completedWithdrawals = platformWithdrawalService.list(wWrapper);
        BigDecimal withdrawnAmount = completedWithdrawals.stream()
                .map(PlatformWithdrawal::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("withdrawnAmount", withdrawnAmount);

        // 可提现余额
        stats.put("withdrawableBalance", settledAmount.subtract(withdrawnAmount));

        // 抽成记录数
        stats.put("totalRecords", allRecords.size());

        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settleCommissions(List<Long> ids) {
        for (Long id : ids) {
            CommissionRecord record = getById(id);
            if (record == null) {
                continue;
            }
            if (record.getStatus() != 1) {
                throw new BusinessException("抽成记录 #" + id + " 不是待结算状态");
            }
            CommissionRecord update = new CommissionRecord();
            update.setId(id);
            update.setStatus(2);
            update.setSettledTime(LocalDateTime.now());
            updateById(update);
        }
    }

    @Override
    public BigDecimal getWithdrawableBalance() {
        // 已结算总额
        LambdaQueryWrapper<CommissionRecord> settledWrapper = new LambdaQueryWrapper<>();
        settledWrapper.eq(CommissionRecord::getStatus, 2);
        List<CommissionRecord> settledRecords = list(settledWrapper);
        BigDecimal settledTotal = settledRecords.stream()
                .map(CommissionRecord::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 已完成提现总额
        LambdaQueryWrapper<PlatformWithdrawal> wWrapper = new LambdaQueryWrapper<>();
        wWrapper.eq(PlatformWithdrawal::getStatus, 3);
        List<PlatformWithdrawal> completedWithdrawals = platformWithdrawalService.list(wWrapper);
        BigDecimal withdrawnTotal = completedWithdrawals.stream()
                .map(PlatformWithdrawal::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 处理中的提现也要扣除
        LambdaQueryWrapper<PlatformWithdrawal> pendingWWrapper = new LambdaQueryWrapper<>();
        pendingWWrapper.in(PlatformWithdrawal::getStatus, 1, 2);
        List<PlatformWithdrawal> pendingWithdrawals = platformWithdrawalService.list(pendingWWrapper);
        BigDecimal pendingWithdrawTotal = pendingWithdrawals.stream()
                .map(PlatformWithdrawal::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return settledTotal.subtract(withdrawnTotal).subtract(pendingWithdrawTotal);
    }
}
