package com.zerofake.fraud.mapper;

import com.zerofake.fraud.dto.response.FraudReportResponse;
import com.zerofake.fraud.entity.FraudReport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FraudReportMapper {

    @Mapping(target = "reportId", source = "id")
    FraudReportResponse toResponse(FraudReport entity);

    List<FraudReportResponse> toResponseList(List<FraudReport> entities);

}