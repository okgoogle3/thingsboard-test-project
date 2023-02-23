package com.example.MyProject.service;

import com.example.MyProject.model.ERole;
import com.example.MyProject.model.RoleModel;
import com.example.MyProject.repo.RoleRepo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    public final RoleRepo roleRepo;
    @PostConstruct
    public void createRoles(){
        roleRepo.save(new RoleModel(ERole.ROLE_USER));
        roleRepo.save(new RoleModel(ERole.ROLE_ADMIN));
        roleRepo.save(new RoleModel(ERole.ROLE_MODERATOR));
    }
}
