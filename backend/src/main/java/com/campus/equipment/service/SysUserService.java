package com.campus.equipment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.equipment.dto.ChangePasswordDTO;
import com.campus.equipment.entity.SysUser;
import com.campus.equipment.vo.UserVO;

import java.util.List;

public interface SysUserService {
    UserVO login(String username, String password);
    UserVO getCurrentUser(String username);
    Page<UserVO> pageUsers(int pageNum, int pageSize, String username, String realName, Integer status);
    UserVO getUserById(Long id);
    void createUser(SysUser user, List<Long> roleIds);
    void updateUser(SysUser user, List<Long> roleIds);
    void deleteUser(Long id);
    void changePassword(String username, ChangePasswordDTO dto);
    void updateProfile(String username, SysUser user);
    void updateAvatar(String username, String avatarUrl);
    void resetPassword(Long userId, String newPassword);
    void toggleStatus(Long userId);
}
