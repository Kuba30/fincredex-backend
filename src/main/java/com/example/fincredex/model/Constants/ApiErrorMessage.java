package com.example.fincredex.model.Constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApiErrorMessage {

    NOT_FOUND_BY_ID("Not found by id"),
    NOT_FOUND_BY_NAME("Not found by name"),

    POST_NOT_FOUND_BY_ID("Post with ID: %s was not found"),
    POST_ALREADY_EXISTS("Post with title: %s already exists"),
    USER_NOT_FOUND_BY_ID("User with ID: %s was not found"),
    USER_ALREADY_EXISTS("User with ID: %s already exists"),
    EMAIL_ALREADY_EXISTS("Email with ID: %s already exists"),
    USER_ROLE_NOT_FOUND("Role with ID: %s was not found"),
    EMAIL_NOT_FOUND("User with email: %s was not found"),
    USERNAME_NOT_FOUND("Username %s was not found"),

    ERROR_DURING_JWT_PROCESSING("An unexpected error occurred during JWT processing"),
    TOKEN_EXPIRED("Token expired."),
    UNEXPECTED_ERROR_OCCURRED("An unexpected error occurred. Please try again later."),
    INVALID_TOKEN_SIGNATURE("Invalid token"),

    AUTHENTICATION_FAILED_FOR_USER("Authentication failed for user: %s."),
    INVALID_USER_OR_PASSWORD("Invalid email or password. Try again"),
    INVALID_USER_REGISTRATION_STATUS("Invalid user registration status %s."),
    NOT_FOUND_REFRESH_TOKEN("Refresh token not found"),

    MISMATCH_PASSWORDS("Password does not match"),
    INVALID_PASSWORD("Invalid password. It must have: "
            + "length at least " + ApiConstants.REQUIRED_MIN_PASSWORD_LENGTH + ", including "
            + ApiConstants.REQUIRED_MIN_LETTERS_NUMBER_EVERY_CASE_IN_PASSWORD + " letter(s) in upper and lower cases, "
            + ApiConstants.REQUIRED_MIN_CHARACTERS_NUMBER_IN_PASSWORD + " character(s), "
            + ApiConstants.REQUIRED_MIN_DIGITS_NUMBER_IN_PASSWORD + " digit(s). "),
    HAVE_NO_ACCESS("You don't necessary permissions.");

    private final String message;

    public String getMessage(Object... args) {
        return String.format(message, args);
    }
}
