package com.campus.equipment.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WorkOrderImportDTO {
    private String faultNo;
    private String orderNo;

    @NotNull(message = "设备ID不能为空")
    private Long deviceId;
    private String faultType;
    private Integer faultLevel;
    private String faultDesc;
    private String reporterName;
    private LocalDateTime reportTime;

    private Long assigneeId;
    private String assigneeName;
    private String assignerName;
    private LocalDateTime assignTime;
    private LocalDateTime expectedTime;
    private LocalDateTime actualStart;

    @NotNull(message = "实际完成时间不能为空")
    private LocalDateTime actualEnd;
    private String handleDesc;
    private Integer handleResult;

    @DecimalMin(value = "0.00", message = "维修费用不能小于0")
    private BigDecimal cost;
    private String partsReplaced;
}
