package com.marrylink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marrylink.entity.CommissionConfig;
import com.marrylink.mapper.CommissionConfigMapper;
import com.marrylink.service.ICommissionConfigService;
import org.springframework.stereotype.Service;

@Service
public class CommissionConfigServiceImpl extends ServiceImpl<CommissionConfigMapper, CommissionConfig>
        implements ICommissionConfigService {

    @Override
    public CommissionConfig getActiveConfig() {
        LambdaQueryWrapper<CommissionConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommissionConfig::getIsEnabled, 1)
               .orderByDesc(CommissionConfig::getUpdateTime)
               .last("LIMIT 1");
        return getOne(wrapper);
    }
}
