package com.marrylink.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("commission_config")
public class CommissionConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 抽成比例(%) */
    private BigDecimal commissionRate;
    /** 最低抽成金额 */
    private BigDecimal minAmount;
    /** 是否启用 1-启用 0-禁用 */
    private Integer isEnabled;
    /** 备注 */
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
