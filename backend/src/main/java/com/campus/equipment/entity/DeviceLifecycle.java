package com.campus.equipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备生命周期记录表
 * 记录设备从采购到报废的全生命周期事件
 */
@Data
@TableName("device_lifecycle")
public class DeviceLifecycle {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 设备ID */
    private Long deviceId;

    /** 设备编号 */
    private String deviceCode;

    /** 设备名称 */
    private String deviceName;

    /** 生命周期类型：purchase-采购 maintenance-维修 replacement-更换 inspection-巡检 retirement-报废 */
    private String lifecycleType;

    /** 事件日期 */
    private LocalDate eventDate;

    /** 事件描述 */
    private String description;

    /** 相关费用 */
    private BigDecimal cost;

    /** 供应商/维修商 */
    private String vendor;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String contactPhone;

    /** 操作人 */
    private String operator;

    /** 备注 */
    private String remark;

    /** 附件（多个逗号分隔） */
    private String attachments;

    /** 逻辑删除：0正常 1删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
