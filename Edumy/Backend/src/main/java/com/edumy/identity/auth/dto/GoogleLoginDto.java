package com.edumy.identity.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginDto {
    @NotBlank
    private String token = "";
}
