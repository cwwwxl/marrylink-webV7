package com.marrylink.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("platform_withdrawal")
public class PlatformWithdrawal {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 提现单号 */
    private String withdrawalNo;
    /** 提现金额 */
    private BigDecimal amount;
    /** 状态 1-待处理 2-处理中 3-已完成 4-已拒绝 */
    private Integer status;
    /** 收款账户类型 */
    private String accountType;
    /** 收款账号 */
    private String accountNo;
    /** 收款人姓名 */
    private String accountName;
    /** 操作人 */
    private String operator;
    /** 备注 */
    private String remark;
    /** 处理时间 */
    private LocalDateTime processTime;
    /** 完成时间 */
    private LocalDateTime completeTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
