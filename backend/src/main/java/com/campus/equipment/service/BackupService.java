package com.campus.equipment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.equipment.entity.SysBackupRecord;

import java.io.File;

public interface BackupService {
    Page<SysBackupRecord> pageBackups(int pageNum, int pageSize);

    SysBackupRecord backup(int backupType, String operator);

    File getBackupFile(Long id);

    SysBackupRecord restore(Long id, String operator);
}
