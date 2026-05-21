package com.campus.equipment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_operation_log")
public class SysOperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 操作类型: CREATE, UPDATE, DELETE, QUERY, EXPORT, IMPORT, LOGIN, LOGOUT, OTHER
     */
    private String operationType;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 响应结果
     */
    private String responseResult;

    /**
     * 是否成功: 0-失败 1-成功
     */
    private Integer success;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 执行时长(毫秒)
     */
    private Long duration;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户真实姓名
     */
    private String realName;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 操作地点
     */
    private String location;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
