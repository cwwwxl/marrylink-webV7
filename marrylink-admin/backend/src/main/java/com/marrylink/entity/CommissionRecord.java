package com.marrylink.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("commission_record")
public class CommissionRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 关联订单号 */
    private String orderNo;
    /** 关联订单ID */
    private Long orderId;
    /** 订单金额 */
    private BigDecimal orderAmount;
    /** 抽成比例(%) */
    private BigDecimal commissionRate;
    /** 抽成金额 */
    private BigDecimal commissionAmount;
    /** 主持人ID */
    private Long hostId;
    /** 主持人姓名 */
    private String hostName;
    /** 状态 1-待结算 2-已结算 */
    private Integer status;
    /** 结算时间 */
    private LocalDateTime settledTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
