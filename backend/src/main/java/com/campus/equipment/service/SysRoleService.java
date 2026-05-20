package com.campus.equipment.service;

import com.campus.equipment.entity.SysRole;
import com.campus.equipment.vo.PermissionTreeVO;
import java.util.List;

public interface SysRoleService {
    List<SysRole> listAll();
    SysRole getById(Long id);
    List<Long> getRolePermIds(Long roleId);
    void createRole(SysRole role, List<Long> permIds);
    void updateRole(SysRole role, List<Long> permIds);
    void deleteRole(Long id);
    List<PermissionTreeVO> getPermissionTree();
}
