package com.example.MyProject.service;

import com.example.MyProject.model.UserModel;
import com.example.MyProject.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
