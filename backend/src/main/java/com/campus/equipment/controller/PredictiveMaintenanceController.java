package com.campus.equipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.equipment.aspect.OperationLog;
import com.campus.equipment.common.Result;
import com.campus.equipment.entity.PredictiveMaintenance;
import com.campus.equipment.mapper.PredictiveMaintenanceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 预测性维护控制器
 */
@Tag(name = "预测性维护")
@RestController
@RequestMapping("/predictive-maintenance")
@RequiredArgsConstructor
public class PredictiveMaintenanceController {

    private final PredictiveMaintenanceMapper predictiveMapper;

    @Operation(summary = "分页查询预测记录")
    @GetMapping
    public Result<IPage<PredictiveMaintenance>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) Integer riskLevel,
            @RequestParam(required = false) Integer status) {
        
        QueryWrapper<PredictiveMaintenance> query = new QueryWrapper<>();
        if (deviceId != null) {
            query.eq("device_id", deviceId);
        }
        if (riskLevel != null) {
            query.eq("risk_level", riskLevel);
        }
        if (status != null) {
            query.eq("status", status);
        }
        query.orderByDesc("probability", "create_time");
        
        return Result.success(predictiveMapper.selectPage(new Page<>(pageNum, pageSize), query));
    }

    @Operation(summary = "获取设备预测记录")
    @GetMapping("/device/{deviceId}")
    public Result<IPage<PredictiveMaintenance>> getByDevice(
            @PathVariable Long deviceId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        QueryWrapper<PredictiveMaintenance> query = new QueryWrapper<>();
        query.eq("device_id", deviceId);
        query.orderByDesc("create_time");
        
        return Result.success(predictiveMapper.selectPage(new Page<>(pageNum, pageSize), query));
    }

    @Operation(summary = "获取未处理的高风险预测")
    @GetMapping("/high-risk")
    public Result<IPage<PredictiveMaintenance>> getHighRisk() {
        QueryWrapper<PredictiveMaintenance> query = new QueryWrapper<>();
        query.eq("status", 0);
        query.ge("risk_level", 3);
        query.orderByDesc("probability");
        query.last("LIMIT 20");
        
        return Result.success(predictiveMapper.selectPage(new Page<>(1, 20), query));
    }

    @Operation(summary = "获取预测详情")
    @GetMapping("/{id}")
    public Result<PredictiveMaintenance> getById(@PathVariable Long id) {
        return Result.success(predictiveMapper.selectById(id));
    }

    @Operation(summary = "处理预测记录")
    @PutMapping("/{id}/handle")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER')")
    @OperationLog(module = "预测性维护", operation = "处理预测")
    public Result<String> handle(@PathVariable Long id, @RequestBody HandleRequest request) {
        PredictiveMaintenance record = predictiveMapper.selectById(id);
        if (record == null) {
            return Result.fail("记录不存在");
        }
        
        record.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        record.setHandler(request.getHandler());
        record.setHandleRemark(request.getHandleRemark());
        record.setHandleTime(java.time.LocalDateTime.now());
        
        predictiveMapper.updateById(record);
        return Result.success("处理成功");
    }

    @Operation(summary = "忽略预测记录")
    @PutMapping("/{id}/ignore")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER')")
    @OperationLog(module = "预测性维护", operation = "忽略预测")
    public Result<String> ignore(@PathVariable Long id, @RequestBody HandleRequest request) {
        PredictiveMaintenance record = predictiveMapper.selectById(id);
        if (record == null) {
            return Result.fail("记录不存在");
        }
        
        record.setStatus(2);
        record.setHandler(request.getHandler());
        record.setHandleRemark(request.getHandleRemark());
        record.setHandleTime(java.time.LocalDateTime.now());
        
        predictiveMapper.updateById(record);
        return Result.success("已忽略");
    }

    @Operation(summary = "删除预测记录")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @OperationLog(module = "预测性维护", operation = "删除记录")
    public Result<String> delete(@PathVariable Long id) {
        predictiveMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "统计预测数据")
    @GetMapping("/statistics")
    public Result<StatisticsVO> statistics() {
        StatisticsVO stats = new StatisticsVO();
        
        // 总预测数
        stats.setTotalCount((int) predictiveMapper.selectCount(null));
        
        // 高风险数
        stats.setHighRiskCount((int) predictiveMapper.selectCount(
            new QueryWrapper<PredictiveMaintenance>().eq("status", 0).ge("risk_level", 3)
        ));
        
        // 已处理数
        stats.setHandledCount((int) predictiveMapper.selectCount(
            new QueryWrapper<PredictiveMaintenance>().eq("status", 1)
        ));
        
        return Result.success(stats);
    }

    @Data
    public static class HandleRequest {
        private Integer status;
        private String handler;
        private String handleRemark;
    }

    @Data
    public static class StatisticsVO {
        private Integer totalCount;
        private Integer highRiskCount;
        private Integer handledCount;
    }
}
