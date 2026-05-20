package com.campus.equipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.equipment.common.Result;
import com.campus.equipment.entity.SysOperationLog;
import com.campus.equipment.mapper.SysOperationLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Tag(name = "操作日志")
@RestController
@RequestMapping("/system/logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final SysOperationLogMapper logMapper;

    @Operation(summary = "分页查询操作日志")
    @GetMapping
    @PreAuthorize("hasAuthority('system:log')")
    public Result<Page<SysOperationLog>> page(@RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize,
                                               @RequestParam(required = false) String username,
                                               @RequestParam(required = false) String module,
                                               @RequestParam(required = false) Integer status) {
        Page<SysOperationLog> pg = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<SysOperationLog>()
                .like(StringUtils.hasText(username), SysOperationLog::getUsername, username)
                .like(StringUtils.hasText(module), SysOperationLog::getModule, module)
                .eq(status != null, SysOperationLog::getStatus, status)
                .orderByDesc(SysOperationLog::getCreateTime);
        return Result.success(logMapper.selectPage(pg, wrapper));
    }

    @Operation(summary = "清空操作日志")
    @DeleteMapping("/clear")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<String> clear() {
        logMapper.delete(null);
        return Result.success("清空成功");
    }
}
