package com.campus.equipment.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
    private Integer gender;
    private String department;
    private Integer status;
    private LocalDateTime lastLogin;
    private LocalDateTime createTime;
    private List<String> roles;
    private List<String> permissions;
}
