package com.campus.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.equipment.entity.SysPermission;
import com.campus.equipment.entity.SysRole;
import com.campus.equipment.entity.SysUser;
import com.campus.equipment.mapper.SysPermissionMapper;
import com.campus.equipment.mapper.SysRoleMapper;
import com.campus.equipment.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .eq(SysUser::getDeleted, 0));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        if (user.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已被禁用");
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        // 添加角色
        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());
        roles.forEach(r -> authorities.add(new SimpleGrantedAuthority(r.getRoleCode())));
        // 添加权限
        List<SysPermission> perms = permissionMapper.selectPermsByUserId(user.getId());
        perms.forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getPermCode())));

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }
}
