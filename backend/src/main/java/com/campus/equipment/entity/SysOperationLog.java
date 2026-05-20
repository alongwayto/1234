package com.campus.equipment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
public class SysOperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private String module;
    private String operation;
    private String method;
    private String requestUrl;
    private String requestMethod;
    private String requestParams;
    private String responseData;
    private String ip;
    private Integer status;
    private String errorMsg;
    private Long costTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
