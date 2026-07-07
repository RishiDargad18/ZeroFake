package com.zerofake.auth.dto.response;
import com.zerofake.auth.constant.RoleType;
import com.zerofake.auth.constant.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private RoleType role;
    private UserStatus status;
}
