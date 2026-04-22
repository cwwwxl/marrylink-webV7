package com.marrylink.service.impl;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marrylink.entity.CommissionRecord;
import com.marrylink.entity.HostCommissionBill;
import com.marrylink.exception.BusinessException;
import com.marrylink.mapper.HostCommissionBillMapper;
import com.marrylink.service.ICommissionRecordService;
import com.marrylink.service.IHostCommissionBillService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HostCommissionBillServiceImpl extends ServiceImpl<HostCommissionBillMapper, HostCommissionBill>
        implements IHostCommissionBillService {

    @Resource
    @Lazy
    private ICommissionRecordService commissionRecordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createBill(String orderNo, Long orderId, BigDecimal orderAmount,
                           BigDecimal commissionRate, BigDecimal commissionAmount,
                           Long hostId, String hostName) {
        // 防重复
        LambdaQueryWrapper<HostCommissionBill> check = new LambdaQueryWrapper<>();
        check.eq(HostCommissionBill::getOrderId, orderId);
        if (count(check) > 0) {
            return;
        }

        HostCommissionBill bill = new HostCommissionBill();
        bill.setBillNo(UUID.fastUUID().toString());
        bill.setOrderNo(orderNo);
        bill.setOrderId(orderId);
        bill.setOrderAmount(orderAmount);
        bill.setCommissionRate(commissionRate);
        bill.setCommissionAmount(commissionAmount);
        bill.setHostId(hostId);
        bill.setHostName(hostName);
        bill.setStatus(1); // 待支付
        bill.setDueDate(LocalDateTime.now().plusDays(7)); // 7天期限
        save(bill);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payBill(Long billId, Long hostId) {
        HostCommissionBill bill = getById(billId);
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }
        if (hostId != null && !bill.getHostId().equals(hostId)) {
            throw new BusinessException("无权操作此账单");
        }
        if (bill.getStatus() == 2) {
            throw new BusinessException("该账单已支付");
        }

        // 更新账单为已支付
        HostCommissionBill update = new HostCommissionBill();
        update.setId(billId);
        update.setStatus(2);
        update.setPaidTime(LocalDateTime.now());
        updateById(update);

        // 同步将对应的抽成记录标记为已结算（佣金到账）
        LambdaQueryWrapper<CommissionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommissionRecord::getOrderId, bill.getOrderId())
               .eq(CommissionRecord::getStatus, 1);
        CommissionRecord crUpdate = new CommissionRecord();
        crUpdate.setStatus(2); // 已结算
        crUpdate.setSettledTime(LocalDateTime.now());
        commissionRecordService.update(crUpdate, wrapper);
    }

    @Override
    public boolean hasOverdueBills(Long hostId) {
        LambdaQueryWrapper<HostCommissionBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HostCommissionBill::getHostId, hostId)
               .in(HostCommissionBill::getStatus, 1, 3) // 待支付或已逾期
               .lt(HostCommissionBill::getDueDate, LocalDateTime.now()); // 已过期
        return count(wrapper) > 0;
    }

    @Override
    public int markOverdueBills() {
        // 将所有已过期但仍为待支付状态的账单标记为已逾期
        LambdaUpdateWrapper<HostCommissionBill> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(HostCommissionBill::getStatus, 1)
               .lt(HostCommissionBill::getDueDate, LocalDateTime.now())
               .set(HostCommissionBill::getStatus, 3);
        return baseMapper.update(null, wrapper);
    }

    @Override
    public Map<String, Object> getBillStats() {
        Map<String, Object> stats = new HashMap<>();
        List<HostCommissionBill> all = list();

        // 待支付总额
        BigDecimal pendingAmount = all.stream()
                .filter(b -> b.getStatus() == 1)
                .map(HostCommissionBill::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("pendingAmount", pendingAmount);

        // 已支付总额（已收佣金）
        BigDecimal paidAmount = all.stream()
                .filter(b -> b.getStatus() == 2)
                .map(HostCommissionBill::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("paidAmount", paidAmount);

        // 已逾期总额
        BigDecimal overdueAmount = all.stream()
                .filter(b -> b.getStatus() == 3)
                .map(HostCommissionBill::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("overdueAmount", overdueAmount);

        // 逾期笔数
        long overdueCount = all.stream().filter(b -> b.getStatus() == 3).count();
        stats.put("overdueCount", overdueCount);

        // 待支付笔数
        long pendingCount = all.stream().filter(b -> b.getStatus() == 1).count();
        stats.put("pendingCount", pendingCount);

        return stats;
    }
}
