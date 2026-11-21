package com.edumy.identity.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDto {
    @NotBlank(message = "Họ và tên không được để trống.")
    @Size(max = 100, message = "Full name cannot exceed 100 characters.")
    private String fullName = "";

    @NotBlank(message = "Email không được để trống.")
    @Email(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "Invalid email address format.")
    private String email = "";

    @NotBlank(message = "Mật khẩu không được để trống.")
    @Size(min = 8, max = 72, message = "Mật khẩu phải từ 8 đến 72 ký tự.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Mật khẩu phải chứa cả chữ cái và chữ số.")
    private String password = "";
}
