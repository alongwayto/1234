package com.campus.equipment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("fault_report")
public class FaultReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String faultNo;
    private Long deviceId;
    private String deviceCode;
    private String deviceName;
    private String faultType;
    private Integer faultLevel;
    private String faultDesc;
    private String faultImages;
    private Long reporterId;
    private String reporterName;
    private LocalDateTime reportTime;
    /** 状态：0待派单 1已派单 2处理中 3已完成 4已归档 */
    private Integer status;
    private String aiDiagnosis;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
