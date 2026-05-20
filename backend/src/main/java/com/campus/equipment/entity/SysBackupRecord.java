package com.campus.equipment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_backup_record")
public class SysBackupRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private Integer backupType;
    private Integer status;
    private String remark;
    private String operator;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
