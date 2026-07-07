package com.zerofake.auth.mapper;

import com.zerofake.auth.dto.request.RegisterRequest;
import com.zerofake.auth.dto.response.RegisterResponse;
import com.zerofake.auth.dto.response.UserResponse;
import com.zerofake.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toEntity(RegisterRequest registerRequest);

    UserResponse toUserResponse(User user);

    RegisterResponse toRegisterResponse(User user);
}