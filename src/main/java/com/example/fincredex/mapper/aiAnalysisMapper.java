package com.example.fincredex.mapper;

import com.example.fincredex.model.entities.AiAnalysis;
import com.example.fincredex.model.response.AiAnalysisResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface aiAnalysisMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    AiAnalysisResponse toResponse(AiAnalysis aiAnalysis);

    default List<String> map(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }

        try {
            return OBJECT_MAPPER.readValue(
                    value,
                    new TypeReference<List<String>>() {}
            );
        } catch (Exception e) {
            return List.of(value);
        }
    }

    default Map<String, String> mapToMap(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyMap();
        }

        try {
            return OBJECT_MAPPER.readValue(
                    value,
                    new TypeReference<Map<String, String>>() {}
            );
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}