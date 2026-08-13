package com.example.fincredex.exception;

import com.example.fincredex.model.response.ApiErrorResponse;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.HttpRequestMethodNotSupportedException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * Validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException exception
    ) {

        Map<String, String> errors = new HashMap<>();

        exception
                .getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        ApiErrorResponse response =
                ApiErrorResponse.builder()
                        .message("Please correct the highlighted fields")
                        .status(400)
                        .errors(errors)
                        .timestamp(LocalDateTime.now())
                        .build();

        return ResponseEntity
                .badRequest()
                .body(response);
    }


    /*
     * Wrong HTTP method
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception
    ) {

        ApiErrorResponse response =
                ApiErrorResponse.builder()
                        .message(
                                "This operation is not supported."
                        )
                        .status(405)
                        .errors(null)
                        .timestamp(LocalDateTime.now())
                        .build();

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(response);
    }


    /*
     * Business/application errors
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(
            RuntimeException exception
    ) {

        ApiErrorResponse response =
                ApiErrorResponse.builder()
                        .message(exception.getMessage())
                        .status(400)
                        .errors(null)
                        .timestamp(LocalDateTime.now())
                        .build();

        return ResponseEntity
                .badRequest()
                .body(response);
    }


    /*
     * Unexpected errors
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception
    ) {

        ApiErrorResponse response =
                ApiErrorResponse.builder()
                        .message(
                                "Something went wrong. Please try again."
                        )
                        .status(500)
                        .errors(null)
                        .timestamp(LocalDateTime.now())
                        .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex
    ) {

        String message = "Unable to save the data.";

        String rootMessage =
                ex.getMostSpecificCause().getMessage();

        if (rootMessage != null) {

            if (rootMessage.contains("unique_company_month")) {
                message =
                        "A financial report for this company already exists for this month.";
            }

            else if (rootMessage.contains("scoring_application_id_key")) {
                message =
                        "Scoring already exists for this application.";
            }

            else if (rootMessage.contains("duplicate key")) {
                message =
                        "This record already exists.";
            }
        }

        ApiErrorResponse error =
                new ApiErrorResponse(
                        message,
                        HttpStatus.CONFLICT.value(),
                        null,
                        LocalDateTime.now()
                );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }
}