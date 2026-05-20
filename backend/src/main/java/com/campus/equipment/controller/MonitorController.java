package com.campus.equipment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.equipment.aspect.OperationLog;
import com.campus.equipment.common.Result;
import com.campus.equipment.entity.DeviceAlert;
import com.campus.equipment.entity.DeviceStatusRecord;
import com.campus.equipment.service.MonitorService;
import com.campus.equipment.vo.RealtimeMonitorVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Status Monitor")
@RestController
@RequestMapping("/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final MonitorService monitorService;

    @Operation(summary = "Report device status")
    @PostMapping("/status/report")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER')")
    public Result<Void> reportStatus(@RequestBody DeviceStatusRecord record) {
        monitorService.reportStatus(record);
        return Result.success();
    }

    @Operation(summary = "Get recent device status records")
    @GetMapping("/status/{deviceId}")
    public Result<List<DeviceStatusRecord>> getRecentStatus(@PathVariable Long deviceId,
                                                            @RequestParam(defaultValue = "20") int limit) {
        return Result.success(monitorService.getRecentStatus(deviceId, limit));
    }

    @Operation(summary = "Get realtime monitor dashboard")
    @GetMapping("/realtime")
    public Result<RealtimeMonitorVO> realtime() {
        return Result.success(monitorService.getRealtimeMonitor());
    }

    @Operation(summary = "Page alerts")
    @GetMapping("/alerts")
    public Result<Page<DeviceAlert>> pageAlerts(@RequestParam(defaultValue = "1") int pageNum,
                                                @RequestParam(defaultValue = "10") int pageSize,
                                                @RequestParam(required = false) Long deviceId,
                                                @RequestParam(required = false) Integer status,
                                                @RequestParam(required = false) Integer alertLevel) {
        return Result.success(monitorService.pageAlerts(pageNum, pageSize, deviceId, status, alertLevel));
    }

    @Operation(summary = "Handle alert")
    @PostMapping("/alerts/{id}/handle")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER')")
    @OperationLog(module = "Status Monitor", operation = "Handle alert")
    public Result<String> handleAlert(@PathVariable Long id, @RequestBody HandleAlertRequest req) {
        monitorService.handleAlert(id, req.getHandler(), req.getRemark(), req.getStatus());
        return Result.success("Handled successfully");
    }

    @Operation(summary = "Count unhandled alerts")
    @GetMapping("/alerts/unhandled-count")
    public Result<Long> unhandledCount() {
        return Result.success(monitorService.countUnhandledAlerts());
    }

    @Data
    public static class HandleAlertRequest {
        private String handler;
        private String remark;
        private Integer status;
    }
}
