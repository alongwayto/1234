package com.campus.equipment.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class WorkOrderAssignDTO {
    @NotNull(message = "故障ID不能为空")
    private Long faultId;
    @NotNull(message = "维护员ID不能为空")
    private Long assigneeId;
    private LocalDateTime expectedTime;
}
