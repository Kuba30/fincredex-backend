package com.example.fincredex.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class ApiErrorResponse {

    private String message;

    private int status;

    private Map<String, String> errors;

    private LocalDateTime timestamp;
}
