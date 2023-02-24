package com.example.MyProject.service;

import com.example.MyProject.model.AssetModel;
import com.example.MyProject.model.ERole;
import com.example.MyProject.model.RoleModel;
import com.example.MyProject.model.UserModel;
import com.example.MyProject.repo.RoleRepo;
import com.example.MyProject.repo.UserRepo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    public final UserRepo userRepo;

    public List<UserModel> getAllUsers() {
        return userRepo.findAll();
    }
    public void deleteUserById(Long id){
        userRepo.deleteById(id);
    }
}
