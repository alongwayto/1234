package com.campus.equipment.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DeviceVO {
    private Long id;
    private String deviceCode;
    private String deviceName;
    private Long categoryId;
    private String categoryName;
    private Long locationId;
    private String locationName;
    private String brand;
    private String model;
    private String serialNumber;
    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;
    private LocalDate warrantyDate;
    private Integer status;
    private String statusName;
    private Integer onlineStatus;
    private String tags;
    private String description;
    private String imageUrl;
    private String responsiblePerson;
    private String responsiblePhone;
    private LocalDateTime lastMaintainTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
