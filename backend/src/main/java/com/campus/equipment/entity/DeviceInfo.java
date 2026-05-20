package com.campus.equipment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("device_info")
public class DeviceInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String deviceCode;
    private String deviceName;
    private Long categoryId;
    private Long locationId;
    private String brand;
    private String model;
    private String serialNumber;
    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;
    private LocalDate warrantyDate;
    /** 状态：0停用 1正常 2维修中 3报废 */
    private Integer status;
    /** 在线状态：0离线 1在线 */
    private Integer onlineStatus;
    private String tags;
    private String description;
    private String imageUrl;
    private String responsiblePerson;
    private String responsiblePhone;
    private LocalDateTime lastMaintainTime;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
