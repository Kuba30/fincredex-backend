package com.example.fincredex.security.validation;


import com.example.fincredex.exception.DataExistException;
import com.example.fincredex.exception.InvalidDataException;
import com.example.fincredex.exception.InvalidPasswordException;
import com.example.fincredex.exception.NotFoundException;
import com.example.fincredex.model.Constants.ApiErrorMessage;
import com.example.fincredex.model.entities.User;
import com.example.fincredex.repository.UserRepository;
import com.example.fincredex.utils.ApiUtils;
import com.example.fincredex.utils.PasswordUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;

@Component
@RequiredArgsConstructor
public class AccessValidator {
    private final UserRepository userRepository;
    private final ApiUtils apiUtils;

    public void validateNewUser(String username, String email, String password, String confirmPassword){
        userRepository.findByUsername(username).ifPresent(existingUser -> {
            throw new DataExistException(ApiErrorMessage.USER_ALREADY_EXISTS.getMessage(username));
        });

        userRepository.findByEmail(email).ifPresent(existingUser -> {
            throw new DataExistException(ApiErrorMessage.EMAIL_ALREADY_EXISTS.getMessage(email));
        });

        if(!password.equals(confirmPassword)){
            throw new InvalidDataException(ApiErrorMessage.MISMATCH_PASSWORDS.getMessage());
        }

        if(PasswordUtils.isNotValidPassword(password)){
            throw new InvalidPasswordException(ApiErrorMessage.INVALID_PASSWORD.getMessage());
        }
    }


    @SneakyThrows
    public void validateAdminOrOwnerAccess(Long ownerId){
        Long currentUserId = apiUtils.getUserIdFromAuthentication();

        if(!currentUserId.equals(ownerId)){
            throw new AccessDeniedException(ApiErrorMessage.HAVE_NO_ACCESS.getMessage());
        }
    }
}
