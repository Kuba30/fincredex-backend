package com.example.fincredex.model.enums.enum_converter;


import com.example.fincredex.model.enums.RiskLevel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RiskLevelConverter implements AttributeConverter<RiskLevel, String> {

    @Override
    public String convertToDatabaseColumn(RiskLevel riskLevel) {
        return riskLevel == null ? null : riskLevel.name();
    }

    @Override
    public RiskLevel convertToEntityAttribute(String dbData) {
        return dbData == null ? null : RiskLevel.valueOf(dbData);
    }
}