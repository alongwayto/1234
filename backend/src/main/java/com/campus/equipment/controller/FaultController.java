package com.campus.equipment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.equipment.aspect.OperationLog;
import com.campus.equipment.common.Result;
import com.campus.equipment.dto.FaultReportDTO;
import com.campus.equipment.dto.WorkOrderAssignDTO;
import com.campus.equipment.dto.WorkOrderHandleDTO;
import com.campus.equipment.dto.WorkOrderImportDTO;
import com.campus.equipment.entity.FaultReport;
import com.campus.equipment.entity.FaultWorkOrder;
import com.campus.equipment.service.FaultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "故障管理")
@RestController
@RequestMapping("/fault")
@RequiredArgsConstructor
public class FaultController {

    private final FaultService faultService;

    @Operation(summary = "分页查询故障")
    @GetMapping
    public Result<Page<FaultReport>> page(@RequestParam(defaultValue = "1") int pageNum,
                                          @RequestParam(defaultValue = "10") int pageSize,
                                          @RequestParam(required = false) Long deviceId,
                                          @RequestParam(required = false) Integer status,
                                          @RequestParam(required = false) Integer faultLevel) {
        return Result.success(faultService.pageFaults(pageNum, pageSize, deviceId, status, faultLevel));
    }

    @Operation(summary = "获取故障详情")
    @GetMapping("/{id}")
    public Result<FaultReport> getById(@PathVariable Long id) {
        return Result.success(faultService.getFaultById(id));
    }

    @Operation(summary = "上报故障")
    @PostMapping
    @OperationLog(module = "故障管理", operation = "上报故障")
    public Result<FaultReport> create(@Valid @RequestBody FaultReportDTO dto,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        // 简化：用 username 作为 reporterName
        FaultReport fault = faultService.createFault(dto, userDetails.getUsername(), null);
        return Result.success("上报成功", fault);
    }

    @Operation(summary = "派单")
    @PostMapping("/assign")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @OperationLog(module = "故障管理", operation = "派单")
    public Result<FaultWorkOrder> assign(@Valid @RequestBody WorkOrderAssignDTO dto,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        FaultWorkOrder order = faultService.assignWorkOrder(dto, userDetails.getUsername(), null);
        return Result.success("派单成功", order);
    }

    @Operation(summary = "导入历史工单")
    @PostMapping("/workorder/import")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @OperationLog(module = "故障管理", operation = "导入历史工单")
    public Result<FaultWorkOrder> importHistoricalWorkOrder(@Valid @RequestBody WorkOrderImportDTO dto,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        String operatorName = userDetails != null ? userDetails.getUsername() : "system";
        FaultWorkOrder order = faultService.importHistoricalWorkOrder(dto, operatorName);
        return Result.success("导入成功", order);
    }

    @Operation(summary = "开始处理工单")
    @PostMapping("/workorder/{id}/start")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER')")
    @OperationLog(module = "故障管理", operation = "开始处理")
    public Result<String> startHandle(@PathVariable Long id) {
        faultService.startHandle(id);
        return Result.success("已开始处理");
    }

    @Operation(summary = "完成工单")
    @PostMapping("/workorder/complete")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER')")
    @OperationLog(module = "故障管理", operation = "完成工单")
    public Result<String> completeHandle(@RequestBody WorkOrderHandleDTO dto) {
        faultService.completeHandle(dto);
        return Result.success("处理完成");
    }

    @Operation(summary = "归档故障")
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @OperationLog(module = "故障管理", operation = "归档故障")
    public Result<String> archive(@PathVariable Long id) {
        faultService.archiveFault(id);
        return Result.success("归档成功");
    }

    @Operation(summary = "评价工单")
    @PostMapping("/workorder/{id}/evaluate")
    @OperationLog(module = "故障管理", operation = "评价工单")
    public Result<String> evaluate(@PathVariable Long id, @RequestBody EvaluateRequest req) {
        faultService.evaluateWorkOrder(id, req.getScore(), req.getRemark());
        return Result.success("评价成功");
    }

    @Operation(summary = "查询工单列表")
    @GetMapping("/workorders")
    public Result<Page<FaultWorkOrder>> pageWorkOrders(@RequestParam(defaultValue = "1") int pageNum,
                                                       @RequestParam(defaultValue = "10") int pageSize,
                                                       @RequestParam(required = false) Long assigneeId,
                                                       @RequestParam(required = false) Integer status) {
        return Result.success(faultService.pageWorkOrders(pageNum, pageSize, assigneeId, status));
    }

    @Operation(summary = "根据故障ID查询工单")
    @GetMapping("/{faultId}/workorder")
    public Result<FaultWorkOrder> getWorkOrder(@PathVariable Long faultId) {
        return Result.success(faultService.getWorkOrderByFaultId(faultId));
    }

    @Data
    public static class EvaluateRequest {
        private Integer score;
        private String remark;
    }
}
