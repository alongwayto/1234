package com.campus.equipment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.equipment.aspect.OperationLog;
import com.campus.equipment.common.Result;
import com.campus.equipment.entity.SysUser;
import com.campus.equipment.service.SysUserService;
import com.campus.equipment.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/users")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService userService;

    @Operation(summary = "分页查询用户")
    @GetMapping
    @PreAuthorize("hasAuthority('system:user')")
    public Result<Page<UserVO>> page(@RequestParam(defaultValue = "1") int pageNum,
                                     @RequestParam(defaultValue = "10") int pageSize,
                                     @RequestParam(required = false) String username,
                                     @RequestParam(required = false) String realName,
                                     @RequestParam(required = false) Integer status) {
        return Result.success(userService.pageUsers(pageNum, pageSize, username, realName, status));
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user')")
    public Result<UserVO> getById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @Operation(summary = "创建用户")
    @PostMapping
    @PreAuthorize("hasAuthority('system:user')")
    @OperationLog(module = "用户管理", operation = "创建用户")
    public Result<String> create(@RequestBody CreateUserRequest req) {
        userService.createUser(req.getUser(), req.getRoleIds());
        return Result.success("创建成功");
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user')")
    @OperationLog(module = "用户管理", operation = "更新用户")
    public Result<String> update(@PathVariable Long id, @RequestBody CreateUserRequest req) {
        req.getUser().setId(id);
        userService.updateUser(req.getUser(), req.getRoleIds());
        return Result.success("更新成功");
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user')")
    @OperationLog(module = "用户管理", operation = "删除用户")
    public Result<String> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "重置密码")
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('system:user')")
    @OperationLog(module = "用户管理", operation = "重置密码")
    public Result<String> resetPassword(@PathVariable Long id, @RequestBody ResetPasswordRequest req) {
        userService.resetPassword(id, req.getPassword());
        return Result.success("密码重置成功");
    }

    @Operation(summary = "切换用户状态")
    @PostMapping("/{id}/toggle-status")
    @PreAuthorize("hasAuthority('system:user')")
    @OperationLog(module = "用户管理", operation = "切换用户状态")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        userService.toggleStatus(id);
        return Result.success();
    }

    @Data
    public static class CreateUserRequest {
        private SysUser user;
        private List<Long> roleIds;
    }

    @Data
    public static class ResetPasswordRequest {
        private String password;
    }
}
