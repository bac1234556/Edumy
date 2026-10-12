package com.edumy.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDto {
    @NotBlank
    @Size(max = 100, message = "Full name cannot exceed 100 characters.")
    private String fullName = "";

    @NotBlank
    @Email(message = "Invalid email address format.")
    private String email = "";

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters long.")
    private String password = "";

    private String role = "Student";
}
