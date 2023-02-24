package com.example.MyProject.controller;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.MyProject.controller.DTO.Request.LoginDTO;
import com.example.MyProject.controller.DTO.Request.SignupDTO;
import com.example.MyProject.controller.DTO.Response.JwtDTO;
import com.example.MyProject.model.ERole;
import com.example.MyProject.model.RoleModel;
import com.example.MyProject.model.UserModel;
import com.example.MyProject.repo.RoleRepo;
import com.example.MyProject.repo.UserRepo;
import com.example.MyProject.security.JwtUtils;
import com.example.MyProject.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthorizationController {
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    UserRepo userRepo;
    @Autowired
    RoleRepo roleRepo;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginDTO loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtDTO(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupDTO signUpRequest) {
        if (userRepo.existsByUsername(signUpRequest.getUsername()))
            return ResponseEntity.badRequest().build();

        if (userRepo.existsByEmail(signUpRequest.getEmail()))
            return ResponseEntity.badRequest().build();

        UserModel user = new UserModel(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<RoleModel> roles = new HashSet<>();

        if (strRoles == null) roles.add(roleRepo.findByRole(ERole.ROLE_USER));
        else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin" -> {
                        RoleModel adminRole = roleRepo.findByRole(ERole.ROLE_ADMIN);
                        roles.add(adminRole);
                    }
                    case "mod" -> {
                        RoleModel modRole = roleRepo.findByRole(ERole.ROLE_MODERATOR);
                        roles.add(modRole);
                    }
                    default -> {
                        RoleModel userRole = roleRepo.findByRole(ERole.ROLE_USER);
                        roles.add(userRole);
                    }
                }
            });
        }
        user.setRoles(roles);
        userRepo.save(user);

        return ResponseEntity.ok().build();
    }
}