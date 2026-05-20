package com.campus.equipment.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class FaultReportDTO {
    private Long id;
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;
    private String faultType;
    private Integer faultLevel;
    @NotBlank(message = "故障描述不能为空")
    private String faultDesc;
    private String faultImages;
}
