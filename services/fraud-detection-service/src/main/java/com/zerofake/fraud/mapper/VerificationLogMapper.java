package com.zerofake.fraud.mapper;

import com.zerofake.fraud.dto.response.VerificationLogResponse;
import com.zerofake.fraud.entity.VerificationLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VerificationLogMapper {

    @Mapping(target = "verificationId", source = "id")
    VerificationLogResponse toResponse(VerificationLog entity);

    List<VerificationLogResponse> toResponseList(List<VerificationLog> entities);

}