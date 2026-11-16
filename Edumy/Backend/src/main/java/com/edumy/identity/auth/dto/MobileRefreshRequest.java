package com.edumy.identity.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileRefreshRequest {
    @NotBlank(message = "Refresh token không được để trống")
    @ToString.Exclude
    private String refreshToken;

    @Override
    public String toString() {
        return "MobileRefreshRequest(refreshToken=[PROTECTED])";
    }
}
