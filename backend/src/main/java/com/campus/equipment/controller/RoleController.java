package com.campus.equipment.controller;

import com.campus.equipment.aspect.OperationLog;
import com.campus.equipment.common.Result;
import com.campus.equipment.entity.SysRole;
import com.campus.equipment.service.SysRoleService;
import com.campus.equipment.vo.PermissionTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色权限管理")
@RestController
@RequestMapping("/system/roles")
@RequiredArgsConstructor
public class RoleController {

    private final SysRoleService roleService;

    @Operation(summary = "获取所有角色")
    @GetMapping
    public Result<List<SysRole>> listAll() {
        return Result.success(roleService.listAll());
    }

    @Operation(summary = "获取权限树")
    @GetMapping("/permissions/tree")
    public Result<List<PermissionTreeVO>> permTree() {
        return Result.success(roleService.getPermissionTree());
    }

    @Operation(summary = "获取角色权限ID列表")
    @GetMapping("/{id}/permissions")
    public Result<List<Long>> getRolePerms(@PathVariable Long id) {
        return Result.success(roleService.getRolePermIds(id));
    }

    @Operation(summary = "创建角色")
    @PostMapping
    @PreAuthorize("hasAuthority('system:role')")
    @OperationLog(module = "角色管理", operation = "创建角色")
    public Result<String>create(@RequestBody RoleRequest req) {
        roleService.createRole(req.getRole(), req.getPermIds());
        return Result.success("创建成功");
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role')")
    @OperationLog(module = "角色管理", operation = "更新角色")
    public Result<String> update(@PathVariable Long id, @RequestBody RoleRequest req) {
        req.getRole().setId(id);
        roleService.updateRole(req.getRole(), req.getPermIds());
        return Result.success("更新成功");
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role')")
    @OperationLog(module = "角色管理", operation = "删除角色")
    public Result<String> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success("删除成功");
    }

    @Data
    public static class RoleRequest {
        private SysRole role;
        private List<Long> permIds;
    }
}
