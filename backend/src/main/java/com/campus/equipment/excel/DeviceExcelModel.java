package com.campus.equipment.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class DeviceExcelModel {
    @ExcelProperty("设备编号")
    @ColumnWidth(20)
    private String deviceCode;

    @ExcelProperty("设备名称")
    @ColumnWidth(30)
    private String deviceName;

    @ExcelProperty("分类编码")
    @ColumnWidth(15)
    private String categoryCode;

    @ExcelProperty("品牌")
    @ColumnWidth(15)
    private String brand;

    @ExcelProperty("型号")
    @ColumnWidth(20)
    private String model;

    @ExcelProperty("序列号")
    @ColumnWidth(25)
    private String serialNumber;

    @ExcelProperty("购买日期(yyyy-MM-dd)")
    @ColumnWidth(22)
    private String purchaseDate;

    @ExcelProperty("购买价格")
    @ColumnWidth(15)
    private BigDecimal purchasePrice;

    @ExcelProperty("保修到期日(yyyy-MM-dd)")
    @ColumnWidth(22)
    private String warrantyDate;

    @ExcelProperty("楼栋")
    @ColumnWidth(15)
    private String building;

    @ExcelProperty("楼层")
    @ColumnWidth(10)
    private String floor;

    @ExcelProperty("房间")
    @ColumnWidth(20)
    private String room;

    @ExcelProperty("负责人")
    @ColumnWidth(15)
    private String responsiblePerson;

    @ExcelProperty("负责人电话")
    @ColumnWidth(18)
    private String responsiblePhone;

    @ExcelProperty("标签")
    @ColumnWidth(20)
    private String tags;

    @ExcelProperty("备注")
    @ColumnWidth(30)
    private String description;
}
