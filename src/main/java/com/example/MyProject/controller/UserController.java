package com.example.MyProject.controller;

import com.example.MyProject.controller.DTO.Request.UserDTO;
import com.example.MyProject.model.UserModel;
import com.example.MyProject.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
@RestController
@RequestMapping("/user")
public class UserController {
    UserRepo userRepo;
    @PostMapping("/register")
    public ResponseEntity<Void> processRegister(UserDTO userTemp) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(userTemp.getPassword());
        userTemp.setPassword(encodedPassword);

        UserModel user = new UserModel(userTemp.getUsername(), userTemp.getPassword(), userTemp.getRoles());
        userRepo.save(user);

        return ResponseEntity.ok().build();
    }
}
