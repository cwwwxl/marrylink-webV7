package com.marrylink.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.marrylink.common.PageResult;
import com.marrylink.common.Result;
import com.marrylink.entity.CommissionConfig;
import com.marrylink.entity.CommissionRecord;
import com.marrylink.entity.HostSettlement;
import com.marrylink.entity.PlatformWithdrawal;
import com.marrylink.service.ICommissionConfigService;
import com.marrylink.service.ICommissionRecordService;
import com.marrylink.service.IHostSettlementService;
import com.marrylink.service.IPlatformWithdrawalService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 平台抽成管理
 */
@RestController
@RequestMapping("/commission")
public class CommissionController {

    @Resource
    private ICommissionConfigService commissionConfigService;
    @Resource
    private ICommissionRecordService commissionRecordService;
    @Resource
    private IPlatformWithdrawalService platformWithdrawalService;
    @Resource
    private IHostSettlementService hostSettlementService;

    // ==================== 抽成配置 ====================

    /** 获取当前生效的抽成配置 */
    @GetMapping("/config")
    public Result<CommissionConfig> getConfig() {
        return Result.ok(commissionConfigService.getActiveConfig());
    }

    /** 获取所有抽成配置 */
    @GetMapping("/config/list")
    public Result<List<CommissionConfig>> getConfigList() {
        LambdaQueryWrapper<CommissionConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(CommissionConfig::getUpdateTime);
        return Result.ok(commissionConfigService.list(wrapper));
    }

    /** 新增抽成配置 */
    @PostMapping("/config")
    public Result<Void> saveConfig(@RequestBody CommissionConfig config) {
        commissionConfigService.save(config);
        return Result.ok();
    }

    /** 更新抽成配置 */
    @PutMapping("/config")
    public Result<Void> updateConfig(@RequestBody CommissionConfig config) {
        commissionConfigService.updateById(config);
        return Result.ok();
    }

    // ==================== 抽成记录 ====================

    /** 分页查询抽成记录 */
    @GetMapping("/record/page")
    public Result<PageResult<CommissionRecord>> getRecordPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String hostName) {

        Page<CommissionRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<CommissionRecord> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(CommissionRecord::getStatus, status);
        }
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like(CommissionRecord::getOrderNo, orderNo);
        }
        if (hostName != null && !hostName.isEmpty()) {
            wrapper.like(CommissionRecord::getHostName, hostName);
        }

        wrapper.orderByDesc(CommissionRecord::getCreateTime);
        commissionRecordService.page(page, wrapper);

        return Result.ok(PageResult.of(page));
    }

    /** 获取抽成统计数据 */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.ok(commissionRecordService.getCommissionStats());
    }

    /** 批量结算抽成记录 */
    @PostMapping("/record/settle")
    public Result<Void> settleRecords(@RequestBody List<Long> ids) {
        commissionRecordService.settleCommissions(ids);
        return Result.ok();
    }

    // ==================== 平台提现 ====================

    /** 分页查询提现记录 */
    @GetMapping("/withdrawal/page")
    public Result<PageResult<PlatformWithdrawal>> getWithdrawalPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status) {

        Page<PlatformWithdrawal> page = new Page<>(current, size);
        LambdaQueryWrapper<PlatformWithdrawal> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(PlatformWithdrawal::getStatus, status);
        }

        wrapper.orderByDesc(PlatformWithdrawal::getCreateTime);
        platformWithdrawalService.page(page, wrapper);

        return Result.ok(PageResult.of(page));
    }

    /** 发起提现申请 */
    @PostMapping("/withdrawal")
    public Result<Void> applyWithdrawal(@RequestBody PlatformWithdrawal withdrawal) {
        platformWithdrawalService.applyWithdrawal(withdrawal);
        return Result.ok();
    }

    /** 更新提现状态 */
    @PutMapping("/withdrawal/{id}/status")
    public Result<Void> updateWithdrawalStatus(@PathVariable Long id, @RequestParam Integer status) {
        platformWithdrawalService.updateWithdrawalStatus(id, status);
        return Result.ok();
    }

    /** 获取可提现余额 */
    @GetMapping("/withdrawal/balance")
    public Result<Map<String, Object>> getWithdrawableBalance() {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("balance", commissionRecordService.getWithdrawableBalance());
        return Result.ok(result);
    }

    // ==================== 主持人下发 ====================

    /** 分页查询主持人结算记录 */
    @GetMapping("/settlement/page")
    public Result<PageResult<HostSettlement>> getSettlementPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String hostName,
            @RequestParam(required = false) String orderNo) {

        Page<HostSettlement> page = new Page<>(current, size);
        LambdaQueryWrapper<HostSettlement> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(HostSettlement::getStatus, status);
        }
        if (hostName != null && !hostName.isEmpty()) {
            wrapper.like(HostSettlement::getHostName, hostName);
        }
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like(HostSettlement::getOrderNo, orderNo);
        }

        wrapper.orderByDesc(HostSettlement::getCreateTime);
        hostSettlementService.page(page, wrapper);

        return Result.ok(PageResult.of(page));
    }

    /** 获取主持人下发统计 */
    @GetMapping("/settlement/stats")
    public Result<Map<String, Object>> getSettlementStats() {
        return Result.ok(hostSettlementService.getSettlementStats());
    }

    /** 手动下发给主持人 */
    @PutMapping("/settlement/{id}/disburse")
    public Result<Void> disburseToHost(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String payMethod = params.get("payMethod");
        String payAccount = params.get("payAccount");
        String payRemark = params.get("payRemark");
        hostSettlementService.disburse(id, payMethod, payAccount, payRemark);
        return Result.ok();
    }
}
