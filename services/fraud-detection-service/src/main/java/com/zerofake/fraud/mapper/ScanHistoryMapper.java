package com.zerofake.fraud.mapper;

import com.zerofake.fraud.dto.response.ScanHistoryResponse;
import com.zerofake.fraud.entity.ScanHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ScanHistoryMapper {

    @Mapping(target = "scanId", source = "id")
    ScanHistoryResponse toResponse(ScanHistory entity);

    List<ScanHistoryResponse> toResponseList(List<ScanHistory> entities);

}