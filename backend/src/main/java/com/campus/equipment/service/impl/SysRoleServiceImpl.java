package com.campus.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.equipment.entity.SysPermission;
import com.campus.equipment.entity.SysRole;
import com.campus.equipment.entity.SysRolePermission;
import com.campus.equipment.exception.BusinessException;
import com.campus.equipment.mapper.SysPermissionMapper;
import com.campus.equipment.mapper.SysRoleMapper;
import com.campus.equipment.mapper.SysRolePermissionMapper;
import com.campus.equipment.service.SysRoleService;
import com.campus.equipment.vo.PermissionTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermMapper;

    @Override
    public List<SysRole> listAll() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>().eq(SysRole::getStatus, 1).orderByAsc(SysRole::getId));
    }

    @Override
    public SysRole getById(Long id) {
        return roleMapper.selectById(id);
    }

    @Override
    public List<Long> getRolePermIds(Long roleId) {
        return rolePermMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId))
                .stream().map(SysRolePermission::getPermissionId).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createRole(SysRole role, List<Long> permIds) {
        if (roleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, role.getRoleCode())) != null) {
            throw new BusinessException("角色编码已存在");
        }
        roleMapper.insert(role);
        assignPerms(role.getId(), permIds);
    }

    @Override
    @Transactional
    public void updateRole(SysRole role, List<Long> permIds) {
        roleMapper.updateById(role);
        rolePermMapper.delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, role.getId()));
        assignPerms(role.getId(), permIds);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        roleMapper.deleteById(id);
        rolePermMapper.delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, id));
    }

    @Override
    public List<PermissionTreeVO> getPermissionTree() {
        List<SysPermission> all = permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getDeleted, 0).orderByAsc(SysPermission::getSortOrder));
        return buildTree(all, 0L);
    }

    private List<PermissionTreeVO> buildTree(List<SysPermission> all, Long parentId) {
        return all.stream()
                .filter(p -> parentId.equals(p.getParentId()))
                .map(p -> {
                    PermissionTreeVO vo = new PermissionTreeVO();
                    BeanUtils.copyProperties(p, vo);
                    List<PermissionTreeVO> children = buildTree(all, p.getId());
                    if (!children.isEmpty()) vo.setChildren(children);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private void assignPerms(Long roleId, List<Long> permIds) {
        if (permIds == null || permIds.isEmpty()) return;
        permIds.forEach(permId -> {
            SysRolePermission rp = new SysRolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permId);
            rp.setCreateTime(LocalDateTime.now());
            rolePermMapper.insert(rp);
        });
    }
}
