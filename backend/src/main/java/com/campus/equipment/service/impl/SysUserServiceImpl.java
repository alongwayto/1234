package com.campus.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.equipment.dto.ChangePasswordDTO;
import com.campus.equipment.entity.*;
import com.campus.equipment.exception.BusinessException;
import com.campus.equipment.mapper.*;
import com.campus.equipment.service.SysUserService;
import com.campus.equipment.utils.JwtUtils;
import com.campus.equipment.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Override
    public UserVO login(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        user.setLastLogin(LocalDateTime.now());
        userMapper.updateById(user);
        UserVO vo = buildUserVO(user);
        // token 通过 controller 单独返回，这里只返回用户信息
        return vo;
    }

    @Override
    public UserVO getCurrentUser(String username) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) throw new BusinessException("用户不存在");
        return buildUserVO(user);
    }

    @Override
    public Page<UserVO> pageUsers(int pageNum, int pageSize, String username, String realName, Integer status) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .like(StringUtils.hasText(username), SysUser::getUsername, username)
                .like(StringUtils.hasText(realName), SysUser::getRealName, realName)
                .eq(status != null, SysUser::getStatus, status)
                .orderByDesc(SysUser::getCreateTime);
        Page<SysUser> userPage = userMapper.selectPage(page, wrapper);
        Page<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream().map(this::buildUserVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public UserVO getUserById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        return buildUserVO(user);
    }

    @Override
    @Transactional
    public void createUser(SysUser user, List<Long> roleIds) {
        if (userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, user.getUsername())) != null) {
            throw new BusinessException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getStatus() == null) user.setStatus(1);
        userMapper.insert(user);
        if (roleIds != null && !roleIds.isEmpty()) {
            assignRoles(user.getId(), roleIds);
        }
    }

    @Override
    @Transactional
    public void updateUser(SysUser user, List<Long> roleIds) {
        if (userMapper.selectById(user.getId()) == null) throw new BusinessException("用户不存在");
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        userMapper.updateById(user);
        if (roleIds != null) {
            deleteUserRoles(user.getId());
            if (!roleIds.isEmpty()) assignRoles(user.getId(), roleIds);
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        if ("admin".equals(user.getUsername())) throw new BusinessException("不能删除超级管理员");
        userMapper.deleteById(id);
        deleteUserRoles(id);
    }

    @Override
    public void changePassword(String username, ChangePasswordDTO dto) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public void updateProfile(String username, SysUser updateData) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (updateData.getRealName() != null) user.setRealName(updateData.getRealName());
        if (updateData.getEmail() != null) user.setEmail(updateData.getEmail());
        if (updateData.getPhone() != null) user.setPhone(updateData.getPhone());
        if (updateData.getGender() != null) user.setGender(updateData.getGender());
        if (updateData.getDepartment() != null) user.setDepartment(updateData.getDepartment());
        if (updateData.getRemark() != null) user.setRemark(updateData.getRemark());
        userMapper.updateById(user);
    }

    @Override
    public void updateAvatar(String username, String avatarUrl) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    @Override
    public void toggleStatus(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        userMapper.updateById(user);
    }

    private UserVO buildUserVO(SysUser user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());
        vo.setRoles(roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList()));
        List<SysPermission> perms = permissionMapper.selectPermsByUserId(user.getId());
        vo.setPermissions(perms.stream().map(SysPermission::getPermCode).collect(Collectors.toList()));
        return vo;
    }

    private void assignRoles(Long userId, List<Long> roleIds) {
        roleIds.forEach(roleId -> {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            ur.setCreateTime(LocalDateTime.now());
            userRoleMapper.insert(ur);
        });
    }

    private void deleteUserRoles(Long userId) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
    }
}
