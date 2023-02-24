package com.example.MyProject.controller.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}