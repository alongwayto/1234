package com.campus.equipment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("device_status_record")
public class DeviceStatusRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deviceId;
    private String deviceCode;
    private BigDecimal cpuUsage;
    private BigDecimal memoryUsage;
    private BigDecimal diskUsage;
    private BigDecimal temperature;
    private Integer powerStatus;
    private Integer networkStatus;
    private Long runDuration;
    private String extraParams;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime recordTime;
}
