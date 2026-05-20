package com.campus.equipment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("fault_work_order")
public class FaultWorkOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long faultId;
    private Long assigneeId;
    private String assigneeName;
    private LocalDateTime assignTime;
    private Long assignerId;
    private String assignerName;
    private LocalDateTime expectedTime;
    private LocalDateTime actualStart;
    private LocalDateTime actualEnd;
    private String handleDesc;
    private Integer handleResult;
    private BigDecimal cost;
    private String partsReplaced;
    /** 状态：0待处理 1处理中 2已完成 3已关闭 */
    private Integer status;
    private Integer evaluateScore;
    private String evaluateRemark;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
