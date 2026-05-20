package com.campus.equipment.dto;

import lombok.Data;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DeviceDTO {
    private Long id;
    @NotBlank(message = "设备编号不能为空")
    private String deviceCode;
    @NotBlank(message = "设备名称不能为空")
    private String deviceName;
    @NotNull(message = "设备分类不能为空")
    private Long categoryId;
    private Long locationId;
    private String brand;
    private String model;
    private String serialNumber;
    private LocalDate purchaseDate;
    @DecimalMin(value = "0.00", message = "购买价格不能小于0")
    private BigDecimal purchasePrice;
    private LocalDate warrantyDate;
    private Integer status;
    @Size(max = 500, message = "标签长度不能超过500个字符")
    private String tags;
    @Size(max = 1000, message = "设备描述长度不能超过1000个字符")
    private String description;
    private String imageUrl;
    private String responsiblePerson;
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "负责人电话格式不正确")
    private String responsiblePhone;
}
