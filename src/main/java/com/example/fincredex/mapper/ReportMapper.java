package com.example.fincredex.mapper;

import com.example.fincredex.model.dto.ReportDTO;
import com.example.fincredex.model.entities.Report;
import com.example.fincredex.model.request.NewReportRequest;
import org.hibernate.type.descriptor.DateTimeUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {DateTimeUtils.class, Object.class}
)
public interface ReportMapper {

    ReportDTO toReportDto(Report report);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "applications", ignore = true)
    @Mapping(target = "reportMonth", ignore = true)
    Report create(NewReportRequest request);


    void updateReport(NewReportRequest request,@MappingTarget Long id);
}
