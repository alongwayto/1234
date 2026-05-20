package com.campus.equipment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.equipment.aspect.OperationLog;
import com.campus.equipment.common.Result;
import com.campus.equipment.entity.SysBackupRecord;
import com.campus.equipment.service.BackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Slf4j
@Tag(name = "数据备份")
@RestController
@RequestMapping("/system/backup")
@RequiredArgsConstructor
public class BackupController {

    private final BackupService backupService;

    @Operation(summary = "手动备份数据库")
    @PostMapping("/manual")
    @PreAuthorize("hasAuthority('system:backup')")
    @OperationLog(module = "数据备份", operation = "手动备份")
    public Result<SysBackupRecord> manualBackup(@AuthenticationPrincipal UserDetails userDetails) {
        return Result.success(backupService.backup(1, getOperator(userDetails)));
    }

    @Operation(summary = "查询备份记录")
    @GetMapping
    @PreAuthorize("hasAuthority('system:backup')")
    public Result<Page<SysBackupRecord>> page(@RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(backupService.pageBackups(pageNum, pageSize));
    }

    @Operation(summary = "下载备份文件")
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('system:backup')")
    public void download(@PathVariable Long id, HttpServletResponse response) throws IOException {
        File file = backupService.getBackupFile(id);
        response.setContentType("application/sql;charset=UTF-8");
        String fileName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + fileName);
        Files.copy(file.toPath(), response.getOutputStream());
    }

    @Operation(summary = "恢复数据库")
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('system:backup')")
    @OperationLog(module = "数据备份", operation = "恢复数据库")
    public Result<SysBackupRecord> restore(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        return Result.success("恢复成功", backupService.restore(id, getOperator(userDetails)));
    }

    private String getOperator(UserDetails userDetails) {
        return userDetails != null ? userDetails.getUsername() : "system";
    }
}
