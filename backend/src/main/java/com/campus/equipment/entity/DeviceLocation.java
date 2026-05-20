package com.campus.equipment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("device_location")
public class DeviceLocation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String building;
    private String floor;
    private String room;
    private String fullAddress;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String remark;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
