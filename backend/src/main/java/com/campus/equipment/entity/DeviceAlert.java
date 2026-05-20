package com.campus.equipment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("device_alert")
public class DeviceAlert {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deviceId;
    private String deviceCode;
    private String deviceName;
    private String alertType;
    private Integer alertLevel;
    private String alertValue;
    private String threshold;
    private String alertMsg;
    /** 处理状态：0未处理 1已处理 2忽略 */
    private Integer status;
    private String handler;
    private LocalDateTime handleTime;
    private String handleRemark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
