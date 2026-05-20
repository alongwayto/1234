package com.campus.equipment.controller;

import com.campus.equipment.aspect.OperationLog;
import com.campus.equipment.common.Result;
import com.campus.equipment.dto.ChangePasswordDTO;
import com.campus.equipment.dto.LoginDTO;
import com.campus.equipment.entity.SysUser;
import com.campus.equipment.service.SysUserService;
import com.campus.equipment.utils.JwtUtils;
import com.campus.equipment.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService userService;
    private final JwtUtils jwtUtils;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    @OperationLog(module = "认证", operation = "用户登录")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        UserVO user = userService.login(dto.getUsername(), dto.getPassword());
        String token = jwtUtils.generateToken(dto.getUsername(), user.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        return Result.success("登录成功", data);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<UserVO> getInfo(@AuthenticationPrincipal UserDetails userDetails) {
        return Result.success(userService.getCurrentUser(userDetails.getUsername()));
    }

    @Operation(summary = "修改密码")
    @PostMapping("/change-password")
    @OperationLog(module = "认证", operation = "修改密码")
    public Result<Void> changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                       @Valid @RequestBody ChangePasswordDTO dto) {
        userService.changePassword(userDetails.getUsername(), dto);
        return Result.success();
    }

    @Operation(summary = "更新个人信息")
    @PutMapping("/profile")
    @OperationLog(module = "认证", operation = "更新个人信息")
    public Result<Void> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                      @RequestBody SysUser user) {
        userService.updateProfile(userDetails.getUsername(), user);
        return Result.success();
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@AuthenticationPrincipal UserDetails userDetails,
                                       @RequestParam("file") MultipartFile file) throws IOException {
        String dir = "./uploads/avatars/";
        new File(dir).mkdirs();
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        file.transferTo(new File(dir + filename));
        String url = "/uploads/avatars/" + filename;
        userService.updateAvatar(userDetails.getUsername(), url);
        return Result.success(url);
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
