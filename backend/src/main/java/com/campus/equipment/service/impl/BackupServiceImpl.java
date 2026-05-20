package com.campus.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.equipment.entity.SysBackupRecord;
import com.campus.equipment.exception.BusinessException;
import com.campus.equipment.mapper.SysBackupRecordMapper;
import com.campus.equipment.service.BackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackupServiceImpl implements BackupService {

    private final SysBackupRecordMapper backupMapper;

    @Value("${backup.path:./backups/}")
    private String backupPath;

    @Value("${backup.mysql-dump-path:mysqldump}")
    private String mysqldumpPath;

    @Value("${backup.mysql-path:mysql}")
    private String mysqlPath;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @Override
    public Page<SysBackupRecord> pageBackups(int pageNum, int pageSize) {
        Page<SysBackupRecord> page = new Page<>(pageNum, pageSize);
        return backupMapper.selectPage(page,
                new LambdaQueryWrapper<SysBackupRecord>().orderByDesc(SysBackupRecord::getCreateTime));
    }

    @Override
    public SysBackupRecord backup(int backupType, String operator) {
        SysBackupRecord record = new SysBackupRecord();
        String fileName = "backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".sql";

        try {
            Path dir = Paths.get(backupPath).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            Path file = dir.resolve(fileName);
            DbConnectionInfo db = parseDatasourceUrl(datasourceUrl);

            record.setFileName(fileName);
            record.setFilePath(file.toString());
            record.setBackupType(backupType);
            record.setOperator(operator);

            List<String> command = new ArrayList<>();
            command.add(mysqldumpPath);
            addConnectionArgs(command, db);
            command.add("--databases");
            command.add(db.database);
            command.add("--result-file=" + file);

            ProcessResult result = run(command, null);
            if (result.exitCode == 0) {
                record.setFileSize(Files.exists(file) ? Files.size(file) : 0L);
                record.setStatus(1);
                record.setRemark("备份成功");
            } else {
                record.setStatus(0);
                record.setRemark("备份失败: " + result.output);
            }
        } catch (Exception e) {
            log.error("数据库备份失败", e);
            record.setFileName(fileName);
            record.setFilePath(Paths.get(backupPath, fileName).toAbsolutePath().normalize().toString());
            record.setBackupType(backupType);
            record.setOperator(operator);
            record.setStatus(0);
            record.setRemark("备份失败: " + e.getMessage());
        }

        backupMapper.insert(record);
        return record;
    }

    @Override
    public File getBackupFile(Long id) {
        SysBackupRecord record = backupMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("备份记录不存在");
        }
        if (!Integer.valueOf(1).equals(record.getStatus())) {
            throw new BusinessException("失败的备份不能下载或恢复");
        }
        File file = new File(record.getFilePath());
        if (!file.exists() || !file.isFile()) {
            throw new BusinessException("备份文件不存在");
        }
        return file;
    }

    @Override
    public SysBackupRecord restore(Long id, String operator) {
        SysBackupRecord record = backupMapper.selectById(id);
        File file = getBackupFile(id);
        try {
            DbConnectionInfo db = parseDatasourceUrl(datasourceUrl);
            List<String> command = new ArrayList<>();
            command.add(mysqlPath);
            addConnectionArgs(command, db);
            ProcessResult result = run(command, file);
            if (result.exitCode != 0) {
                throw new BusinessException("数据库恢复失败: " + result.output);
            }
            record.setRemark("最近恢复成功，操作人: " + operator);
            return record;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("数据库恢复失败", e);
            throw new BusinessException("数据库恢复失败: " + e.getMessage());
        }
    }

    private void addConnectionArgs(List<String> command, DbConnectionInfo db) {
        command.add("-h" + db.host);
        command.add("-P" + db.port);
        command.add("-u" + dbUsername);
        if (dbPassword != null && !dbPassword.isBlank()) {
            command.add("-p" + dbPassword);
        }
    }

    private ProcessResult run(List<String> command, File inputFile) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        if (inputFile != null) {
            builder.redirectInput(inputFile);
        }
        Process process = builder.start();
        String output = new String(process.getInputStream().readAllBytes());
        int exitCode = process.waitFor();
        return new ProcessResult(exitCode, output.length() > 450 ? output.substring(0, 450) : output);
    }

    private DbConnectionInfo parseDatasourceUrl(String url) {
        String prefix = "jdbc:mysql://";
        if (url == null || !url.startsWith(prefix)) {
            throw new BusinessException("仅支持 jdbc:mysql:// 格式的数据源地址");
        }
        String withoutPrefix = url.substring(prefix.length());
        String main = withoutPrefix.split("\\?")[0];
        int slashIndex = main.indexOf('/');
        if (slashIndex < 0 || slashIndex == main.length() - 1) {
            throw new BusinessException("数据源地址缺少数据库名");
        }
        String hostPort = main.substring(0, slashIndex);
        String database = main.substring(slashIndex + 1);
        String host = hostPort;
        String port = "3306";
        int colonIndex = hostPort.lastIndexOf(':');
        if (colonIndex > -1) {
            host = hostPort.substring(0, colonIndex);
            port = hostPort.substring(colonIndex + 1);
        }
        return new DbConnectionInfo(host, port, database);
    }

    private static class DbConnectionInfo {
        private final String host;
        private final String port;
        private final String database;

        private DbConnectionInfo(String host, String port, String database) {
            this.host = host;
            this.port = port;
            this.database = database;
        }
    }

    private static class ProcessResult {
        private final int exitCode;
        private final String output;

        private ProcessResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }
    }
}
