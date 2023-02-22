package com.example.MyProject.controller.DTO.Request;

import com.example.MyProject.model.RoleModel;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class UserDTO {
    private String username;
    private String password;
    private Set<RoleModel> roles = new HashSet<>();
}
