package com.example.fincredex.mapper;

import com.example.fincredex.model.dto.ApplicationDTO;
import com.example.fincredex.model.entities.Application;
import com.example.fincredex.model.request.NewApplicationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ApplicationMapper {

    ApplicationDTO toApplicationDto(Application application);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "report", ignore = true)
    @Mapping(target = "scoring", ignore = true)
    @Mapping(target = "aiAnalyses", ignore = true)
    Application create(NewApplicationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "report", ignore = true)
    @Mapping(target = "scoring", ignore = true)
    @Mapping(target = "aiAnalyses", ignore = true)
    void updateApplication(
            NewApplicationRequest request,
            @MappingTarget Application application
    );

    ApplicationDTO toDto(Application application);
}