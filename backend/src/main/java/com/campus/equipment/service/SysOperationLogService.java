package com.campus.equipment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.equipment.entity.SysOperationLog;

/**
 * 操作日志服务接口
 */
public interface SysOperationLogService extends IService<SysOperationLog> {

    /**
     * 保存操作日志
     *
     * @param log 操作日志
     */
    void saveLog(SysOperationLog log);

    /**
     * 记录操作
     *
     * @param module 模块
     * @param description 描述
     * @param operationType 操作类型
     */
    void logOperation(String module, String description, String operationType);
}
