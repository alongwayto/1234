package com.campus.equipment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.equipment.entity.SysOperationLog;
import com.campus.equipment.mapper.SysOperationLogMapper;
import com.campus.equipment.service.SysOperationLogService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务实现
 */
@Service
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog>
        implements SysOperationLogService {

    /**
     * 异步保存日志
     */
    @Override
    @Async
    public void saveLog(SysOperationLog log) {
        this.save(log);
    }

    /**
     * 记录操作日志
     */
    @Override
    public void logOperation(String module, String description, String operationType) {
        SysOperationLog log = new SysOperationLog();
        log.setModule(module);
        log.setDescription(description);
        log.setOperationType(operationType);
        log.setSuccess(1);
        this.save(log);
    }
}
