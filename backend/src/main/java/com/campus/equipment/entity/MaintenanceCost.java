package com.campus.equipment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("maintenance_cost")
public class MaintenanceCost {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deviceId;
    private String deviceCode;
    private String deviceName;
    private String costType;
    private BigDecimal costAmount;
    private LocalDate costDate;
    private Long workOrderId;
    private String description;
    private String operator;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
