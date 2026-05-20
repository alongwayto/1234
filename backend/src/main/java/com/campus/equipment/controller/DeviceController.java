package com.campus.equipment.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.equipment.aspect.OperationLog;
import com.campus.equipment.common.Result;
import com.campus.equipment.dto.DeviceDTO;
import com.campus.equipment.entity.DeviceCategory;
import com.campus.equipment.entity.DeviceLocation;
import com.campus.equipment.service.DeviceService;
import com.campus.equipment.vo.DeviceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@Tag(name = "设备管理")
@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @Operation(summary = "分页查询设备")
    @GetMapping
    public Result<IPage<DeviceVO>> page(@RequestParam(defaultValue = "1") int pageNum,
                                       @RequestParam(defaultValue = "10") int pageSize,
                                       @RequestParam(required = false) String deviceCode,
                                       @RequestParam(required = false) String deviceName,
                                       @RequestParam(required = false) Long categoryId,
                                       @RequestParam(required = false) Long locationId,
                                       @RequestParam(required = false) Integer status,
                                       @RequestParam(required = false) Integer onlineStatus) {
        return Result.success(deviceService.pageDevices(pageNum, pageSize, deviceCode, deviceName,
                categoryId, locationId, status, onlineStatus));
    }

    @Operation(summary = "获取设备详情")
    @GetMapping("/{id}")
    public Result<DeviceVO> getById(@PathVariable Long id) {
        return Result.success(deviceService.getDeviceById(id));
    }

    @Operation(summary = "创建设备")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER')")
    @OperationLog(module = "设备管理", operation = "创建设备")
    public Result<String> create(@Valid @RequestBody DeviceDTO dto) {
        deviceService.createDevice(dto);
        return Result.success("创建成功");
    }

    @Operation(summary = "更新设备")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER')")
    @OperationLog(module = "设备管理", operation = "更新设备")
    public Result<String> update(@PathVariable Long id, @Valid @RequestBody DeviceDTO dto) {
        dto.setId(id);
        deviceService.updateDevice(dto);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除设备")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER')")
    @OperationLog(module = "设备管理", operation = "删除设备")
    public Result<String> delete(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "批量删除设备")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER')")
    @OperationLog(module = "设备管理", operation = "批量删除设备")
    public Result<String> batchDelete(@RequestBody List<Long> ids) {
        deviceService.batchDelete(ids);
        return Result.success("删除成功");
    }

    @Operation(summary = "导入设备（Excel）")
    @PostMapping("/import")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER')")
    @OperationLog(module = "设备管理", operation = "导入设备")
    public Result<String> importDevices(@RequestParam("file") MultipartFile file) throws IOException {
        deviceService.importDevices(file);
        return Result.success("导入成功");
    }

    @Operation(summary = "下载设备导入模板")
    @GetMapping("/import-template")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER')")
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        deviceService.exportImportTemplate(response);
    }

    @Operation(summary = "导出设备（Excel）")
    @GetMapping("/export")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER')")
    @OperationLog(module = "设备管理", operation = "导出设备")
    public void exportDevices(HttpServletResponse response,
                              @RequestParam(required = false) String deviceCode,
                              @RequestParam(required = false) String deviceName,
                              @RequestParam(required = false) Long categoryId,
                              @RequestParam(required = false) Integer status) throws IOException {
        deviceService.exportDevices(response, deviceCode, deviceName, categoryId, status);
    }

    @Operation(summary = "更新在线状态")
    @PutMapping("/{id}/online-status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER')")
    @OperationLog(module = "设备管理", operation = "更新在线状态")
    public Result<Void> updateOnlineStatus(@PathVariable Long id, @RequestParam Integer onlineStatus) {
        deviceService.updateOnlineStatus(id, onlineStatus);
        return Result.success();
    }

    // ===== 分类管理 =====
    @GetMapping("/categories")
    public Result<List<DeviceCategory>> listCategories() {
        return Result.success(deviceService.listCategories());
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER')")
    @OperationLog(module = "设备管理", operation = "创建分类")
    public Result<String> createCategory(@RequestBody DeviceCategory category) {
        deviceService.createCategory(category);
        return Result.success("创建成功");
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER')")
    @OperationLog(module = "设备管理", operation = "更新分类")
    public Result<String> updateCategory(@PathVariable Long id, @RequestBody DeviceCategory category) {
        category.setId(id);
        deviceService.updateCategory(category);
        return Result.success("更新成功");
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER')")
    @OperationLog(module = "设备管理", operation = "删除分类")
    public Result<String> deleteCategory(@PathVariable Long id) {
        deviceService.deleteCategory(id);
        return Result.success("删除成功");
    }

    // ===== 位置管理 =====
    @GetMapping("/locations")
    public Result<List<DeviceLocation>> listLocations() {
        return Result.success(deviceService.listLocations());
    }

    @PostMapping("/locations")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER')")
    @OperationLog(module = "设备管理", operation = "创建位置")
    public Result<String> createLocation(@RequestBody DeviceLocation location) {
        deviceService.createLocation(location);
        return Result.success("创建成功");
    }

    @PutMapping("/locations/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER')")
    @OperationLog(module = "设备管理", operation = "更新位置")
    public Result<String> updateLocation(@PathVariable Long id, @RequestBody DeviceLocation location) {
        location.setId(id);
        deviceService.updateLocation(location);
        return Result.success("更新成功");
    }

    @DeleteMapping("/locations/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER')")
    @OperationLog(module = "设备管理", operation = "删除位置")
    public Result<String> deleteLocation(@PathVariable Long id) {
        deviceService.deleteLocation(id);
        return Result.success("删除成功");
    }
}
