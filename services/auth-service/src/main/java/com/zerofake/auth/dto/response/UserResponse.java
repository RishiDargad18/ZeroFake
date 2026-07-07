package com.zerofake.auth.dto.response;

import com.zerofake.auth.constant.RoleType;
import com.zerofake.auth.constant.UserStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private RoleType role;
    private UserStatus status;
    private LocalDateTime lastLogin;
}