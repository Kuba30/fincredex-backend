package com.example.fincredex.mapper;


import com.example.fincredex.model.dto.ScoringDTO;
import com.example.fincredex.model.entities.Scoring;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ScoringMapper {

    ScoringDTO toDto(Scoring scoring);
}
