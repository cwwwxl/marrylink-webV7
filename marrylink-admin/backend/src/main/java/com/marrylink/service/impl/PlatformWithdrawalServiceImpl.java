package com.marrylink.service.impl;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marrylink.entity.PlatformWithdrawal;
import com.marrylink.exception.BusinessException;
import com.marrylink.mapper.PlatformWithdrawalMapper;
import com.marrylink.service.ICommissionRecordService;
import com.marrylink.service.IPlatformWithdrawalService;
import com.marrylink.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PlatformWithdrawalServiceImpl extends ServiceImpl<PlatformWithdrawalMapper, PlatformWithdrawal>
        implements IPlatformWithdrawalService {

    @Resource
    private ICommissionRecordService commissionRecordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyWithdrawal(PlatformWithdrawal withdrawal) {
        // 校验可提现余额
        BigDecimal withdrawable = commissionRecordService.getWithdrawableBalance();
        if (withdrawal.getAmount().compareTo(withdrawable) > 0) {
            throw new BusinessException("提现金额超过可提现余额，当前可提现: ¥" + withdrawable);
        }
        if (withdrawal.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("提现金额必须大于0");
        }

        withdrawal.setWithdrawalNo(UUID.fastUUID().toString());
        withdrawal.setStatus(1); // 待处理
        withdrawal.setOperator(SecurityUtils.getCurrentUsername());
        save(withdrawal);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWithdrawalStatus(Long id, Integer status) {
        PlatformWithdrawal withdrawal = getById(id);
        if (withdrawal == null) {
            throw new BusinessException("提现记录不存在");
        }

        PlatformWithdrawal update = new PlatformWithdrawal();
        update.setId(id);
        update.setStatus(status);

        if (status == 2) {
            update.setProcessTime(LocalDateTime.now());
        } else if (status == 3) {
            update.setCompleteTime(LocalDateTime.now());
        }

        updateById(update);
    }
}
