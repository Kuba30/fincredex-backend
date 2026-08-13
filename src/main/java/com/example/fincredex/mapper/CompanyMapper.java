package com.example.fincredex.mapper;

import com.example.fincredex.model.dto.CompanyDTO;
import com.example.fincredex.model.entities.Company;
import com.example.fincredex.model.request.CompanyRequest;
import org.hibernate.type.descriptor.DateTimeUtils;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {DateTimeUtils.class, Object.class}
)
public interface CompanyMapper{

    Company create(CompanyRequest request);


    CompanyDTO toDTO(Company saved);
}
