package com.marrylink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marrylink.entity.PlatformWithdrawal;

public interface IPlatformWithdrawalService extends IService<PlatformWithdrawal> {

    /**
     * 发起提现申请
     */
    void applyWithdrawal(PlatformWithdrawal withdrawal);

    /**
     * 更新提现状态
     */
    void updateWithdrawalStatus(Long id, Integer status);
}
