package com.marrylink.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("host_commission_bill")
public class HostCommissionBill {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 账单编号 */
    private String billNo;
    /** 关联订单号 */
    private String orderNo;
    /** 关联订单ID */
    private Long orderId;
    /** 订单金额 */
    private BigDecimal orderAmount;
    /** 佣金比例(%) */
    private BigDecimal commissionRate;
    /** 应付佣金金额 */
    private BigDecimal commissionAmount;
    /** 主持人ID */
    private Long hostId;
    /** 主持人姓名 */
    private String hostName;
    /** 状态 1-待支付 2-已支付 3-已逾期 */
    private Integer status;
    /** 最后支付期限 */
    private LocalDateTime dueDate;
    /** 支付时间 */
    private LocalDateTime paidTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
