package com.marrylink.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("host_settlement")
public class HostSettlement {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 关联订单号 */
    private String orderNo;
    /** 关联订单ID */
    private Long orderId;
    /** 订单总金额 */
    private BigDecimal orderAmount;
    /** 平台抽成金额 */
    private BigDecimal commissionAmount;
    /** 主持人应收金额 */
    private BigDecimal hostAmount;
    /** 主持人ID */
    private Long hostId;
    /** 主持人姓名 */
    private String hostName;
    /** 状态 1-待下发 2-已下发 */
    private Integer status;
    /** 下发方式 */
    private String payMethod;
    /** 收款账号 */
    private String payAccount;
    /** 下发备注 */
    private String payRemark;
    /** 下发时间 */
    private LocalDateTime payTime;
    /** 操作人 */
    private String operator;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
