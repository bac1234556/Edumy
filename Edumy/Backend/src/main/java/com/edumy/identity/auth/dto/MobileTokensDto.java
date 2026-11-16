package com.edumy.identity.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileTokensDto {
    private String accessToken;
    private String expiresAt;

    @ToString.Exclude
    private String refreshToken;

    private String refreshExpiresAt;

    @Override
    public String toString() {
        return "MobileTokensDto(accessToken=" + (accessToken != null ? "[PROTECTED]" : "null")
                + ", expiresAt=" + expiresAt
                + ", refreshToken=[PROTECTED]"
                + ", refreshExpiresAt=" + refreshExpiresAt + ")";
    }
}
