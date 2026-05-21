package com.campus.equipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.equipment.aspect.OperationLog;
import com.campus.equipment.common.Result;
import com.campus.equipment.entity.DeviceLifecycle;
import com.campus.equipment.mapper.DeviceLifecycleMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 设备生命周期控制器
 */
@Tag(name = "设备生命周期管理")
@RestController
@RequestMapping("/lifecycle")
@RequiredArgsConstructor
public class DeviceLifecycleController {

    private final DeviceLifecycleMapper lifecycleMapper;

    @Operation(summary = "分页查询生命周期记录")
    @GetMapping
    public Result<IPage<DeviceLifecycle>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String lifecycleType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        QueryWrapper<DeviceLifecycle> query = new QueryWrapper<>();
        if (deviceId != null) {
            query.eq("device_id", deviceId);
        }
        if (lifecycleType != null && !lifecycleType.isEmpty()) {
            query.eq("lifecycle_type", lifecycleType);
        }
        if (startDate != null) {
            query.ge("event_date", startDate);
        }
        if (endDate != null) {
            query.le("event_date", endDate);
        }
        query.orderByDesc("event_date", "create_time");
        
        return Result.success(lifecycleMapper.selectPage(new Page<>(pageNum, pageSize), query));
    }

    @Operation(summary = "获取设备生命周期记录")
    @GetMapping("/device/{deviceId}")
    public Result<List<DeviceLifecycle>> getByDevice(@PathVariable Long deviceId) {
        QueryWrapper<DeviceLifecycle> query = new QueryWrapper<>();
        query.eq("device_id", deviceId);
        query.orderByDesc("event_date");
        return Result.success(lifecycleMapper.selectList(query));
    }

    @Operation(summary = "获取生命周期详情")
    @GetMapping("/{id}")
    public Result<DeviceLifecycle> getById(@PathVariable Long id) {
        return Result.success(lifecycleMapper.selectById(id));
    }

    @Operation(summary = "创建生命周期记录")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER')")
    @OperationLog(module = "设备生命周期", operation = "创建记录")
    public Result<String> create(@RequestBody LifecycleRequest request) {
        DeviceLifecycle lifecycle = new DeviceLifecycle();
        lifecycle.setDeviceId(request.getDeviceId());
        lifecycle.setDeviceCode(request.getDeviceCode());
        lifecycle.setDeviceName(request.getDeviceName());
        lifecycle.setLifecycleType(request.getLifecycleType());
        lifecycle.setEventDate(request.getEventDate());
        lifecycle.setDescription(request.getDescription());
        lifecycle.setCost(request.getCost());
        lifecycle.setVendor(request.getVendor());
        lifecycle.setContactPerson(request.getContactPerson());
        lifecycle.setContactPhone(request.getContactPhone());
        lifecycle.setOperator(request.getOperator());
        lifecycle.setRemark(request.getRemark());
        lifecycle.setAttachments(request.getAttachments());
        
        lifecycleMapper.insert(lifecycle);
        return Result.success("创建成功");
    }

    @Operation(summary = "更新生命周期记录")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER')")
    @OperationLog(module = "设备生命周期", operation = "更新记录")
    public Result<String> update(@PathVariable Long id, @RequestBody LifecycleRequest request) {
        DeviceLifecycle lifecycle = lifecycleMapper.selectById(id);
        if (lifecycle == null) {
            return Result.fail("记录不存在");
        }
        
        lifecycle.setLifecycleType(request.getLifecycleType());
        lifecycle.setEventDate(request.getEventDate());
        lifecycle.setDescription(request.getDescription());
        lifecycle.setCost(request.getCost());
        lifecycle.setVendor(request.getVendor());
        lifecycle.setContactPerson(request.getContactPerson());
        lifecycle.setContactPhone(request.getContactPhone());
        lifecycle.setOperator(request.getOperator());
        lifecycle.setRemark(request.getRemark());
        lifecycle.setAttachments(request.getAttachments());
        
        lifecycleMapper.updateById(lifecycle);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除生命周期记录")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @OperationLog(module = "设备生命周期", operation = "删除记录")
    public Result<String> delete(@PathVariable Long id) {
        lifecycleMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取生命周期类型统计")
    @GetMapping("/statistics/types")
    public Result<List<Object>> statisticsByType() {
        String sql = "SELECT lifecycle_type as type, COUNT(*) as count FROM device_lifecycle WHERE deleted = 0 GROUP BY lifecycle_type";
        List<Object> result = lifecycleMapper.selectMaps(new QueryWrapper<DeviceLifecycle>()
                .select("lifecycle_type as type, COUNT(*) as count")
                .groupBy("lifecycle_type")).stream().map(m -> (Object) m).toList();
        return Result.success(result);
    }

    @Data
    public static class LifecycleRequest {
        private Long deviceId;
        private String deviceCode;
        private String deviceName;
        private String lifecycleType;
        private LocalDate eventDate;
        private String description;
        private java.math.BigDecimal cost;
        private String vendor;
        private String contactPerson;
        private String contactPhone;
        private String operator;
        private String remark;
        private String attachments;
    }
}
