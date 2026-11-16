package com.edumy.payout.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePayoutAccountRequest {

    @NotBlank(message = "Vui lòng nhập tên ngân hàng.")
    private String bankName;

    @NotBlank(message = "Vui lòng nhập số tài khoản ngân hàng.")
    private String accountNumber;

    @NotBlank(message = "Vui lòng nhập tên chủ tài khoản.")
    private String accountHolder;

    @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại để xác thực.")
    private String currentPassword;
}
