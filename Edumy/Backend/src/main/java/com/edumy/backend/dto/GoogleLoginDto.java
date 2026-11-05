package com.edumy.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginDto {
    @NotBlank
    private String token = "";
}
