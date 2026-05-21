package com.campus.equipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预测性维护记录表
 * 基于设备运行数据的智能预测维护提醒
 */
@Data
@TableName("predictive_maintenance")
public class PredictiveMaintenance {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 设备ID */
    private Long deviceId;

    /** 设备编号 */
    private String deviceCode;

    /** 设备名称 */
    private String deviceName;

    /** 预测类型：failure-故障预测 wear-磨损预测 performance-性能预测 */
    private String predictType;

    /** 预测风险等级：1-低 2-中 3-高 4-紧急 */
    private Integer riskLevel;

    /** 预测概率（0-100） */
    private Integer probability;

    /** 预测发生时间 */
    private LocalDateTime predictTime;

    /** 预测描述 */
    private String description;

    /** 建议措施 */
    private String suggestion;

    /** AI分析依据 */
    private String analysisBasis;

    /** 处理状态：0-未处理 1-已处理 2-忽略 */
    private Integer status;

    /** 处理人 */
    private String handler;

    /** 处理时间 */
    private LocalDateTime handleTime;

    /** 处理备注 */
    private String handleRemark;

    /** 逻辑删除：0正常 1删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
