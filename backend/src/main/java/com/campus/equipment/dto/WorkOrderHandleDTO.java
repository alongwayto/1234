package com.campus.equipment.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class WorkOrderHandleDTO {
    @NotNull(message = "工单ID不能为空")
    private Long orderId;
    private String handleDesc;
    private Integer handleResult;
    private BigDecimal cost;
    private String partsReplaced;
}
