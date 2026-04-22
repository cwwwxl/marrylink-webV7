package com.marrylink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marrylink.entity.CommissionConfig;

public interface ICommissionConfigService extends IService<CommissionConfig> {

    /**
     * 获取当前生效的抽成配置
     */
    CommissionConfig getActiveConfig();
}
