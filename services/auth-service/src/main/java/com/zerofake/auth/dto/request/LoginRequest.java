package com.zerofake.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email is required.")
    @Email(message = "A valid email address is required.")
    private String email;

    @NotBlank(message = "Password is required.")
    private String password;
}
