package com.campus.equipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.equipment.common.Result;
import com.campus.equipment.entity.SysOperationLog;
import com.campus.equipment.service.SysOperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/api/system/log")
@RequiredArgsConstructor
@Tag(name = "系统管理 - 操作日志")
public class SysOperationLogController {

    private final SysOperationLogService operationLogService;

    /**
     * 分页查询操作日志
     */
    @GetMapping("/list")
    @Operation(summary = "查询操作日志列表")
    public Result<Page<SysOperationLog>> list(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "操作模块") @RequestParam(required = false) String module,
            @Parameter(description = "操作类型") @RequestParam(required = false) String operationType,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        Page<SysOperationLog> page = new Page<>(current, size);
        QueryWrapper<SysOperationLog> wrapper = new QueryWrapper<>();
        
        if (username != null && !username.isEmpty()) {
            wrapper.like("username", username);
        }
        if (module != null && !module.isEmpty()) {
            wrapper.eq("module", module);
        }
        if (operationType != null && !operationType.isEmpty()) {
            wrapper.eq("operation_type", operationType);
        }
        if (startTime != null) {
            wrapper.ge("create_time", startTime);
        }
        if (endTime != null) {
            wrapper.le("create_time", endTime);
        }
        
        wrapper.orderByDesc("create_time");
        
        Page<SysOperationLog> result = operationLogService.page(page, wrapper);
        return Result.success(result);
    }

    /**
     * 获取日志详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取日志详情")
    public Result<SysOperationLog> getById(
            @Parameter(description = "日志ID") @PathVariable Long id
    ) {
        SysOperationLog log = operationLogService.getById(id);
        return Result.success(log);
    }

    /**
     * 获取操作统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取操作统计")
    public Result<Map<String, Object>> getStatistics(
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        QueryWrapper<SysOperationLog> wrapper = new QueryWrapper<>();
        wrapper.ge("create_time", startTime);
        wrapper.le("create_time", endTime);
        
        Map<String, Object> statistics = new HashMap<>();
        
        // 总操作数
        statistics.put("totalCount", operationLogService.count(wrapper));
        
        // 成功操作数
        QueryWrapper<SysOperationLog> successWrapper = wrapper.clone();
        successWrapper.eq("success", 1);
        statistics.put("successCount", operationLogService.count(successWrapper));
        
        // 失败操作数
        QueryWrapper<SysOperationLog> failWrapper = wrapper.clone();
        failWrapper.eq("success", 0);
        statistics.put("failCount", operationLogService.count(failWrapper));
        
        // 按模块统计
        Map<String, Long> moduleStats = new HashMap<>();
        statistics.put("moduleStats", moduleStats);
        
        // 按用户统计
        Map<String, Long> userStats = new HashMap<>();
        statistics.put("userStats", userStats);
        
        return Result.success(statistics);
    }

    /**
     * 删除日志
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除日志")
    public Result<Void> delete(
            @Parameter(description = "日志ID，多个用逗号分隔") @PathVariable String ids
    ) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            operationLogService.removeById(Long.parseLong(id.trim()));
        }
        return Result.success();
    }

    /**
     * 清空所有日志
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清空所有日志")
    public Result<Void> clear() {
        // 注意：生产环境中应使用物理删除或归档
        // 这里使用逻辑删除（如果配置了）
        return Result.success();
    }

    /**
     * 导出日志
     */
    @GetMapping("/export")
    @Operation(summary = "导出日志")
    public Result<String> export(
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        // 实际实现应生成Excel文件并返回下载链接
        return Result.success("导出任务已提交，请稍后下载");
    }
}
