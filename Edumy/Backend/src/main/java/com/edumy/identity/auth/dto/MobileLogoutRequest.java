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
public class MobileLogoutRequest {
    @ToString.Exclude
    private String refreshToken;

    @Override
    public String toString() {
        return "MobileLogoutRequest(refreshToken=[PROTECTED])";
    }
}
