package com.example.fincredex.service.impl;

import com.example.fincredex.exception.InvalidDataException;
import com.example.fincredex.exception.NotFoundException;
import com.example.fincredex.mapper.UserMapper;
import com.example.fincredex.model.Constants.ApiErrorMessage;
import com.example.fincredex.model.dto.UserDTO;
import com.example.fincredex.model.dto.UserProfileDTO;
import com.example.fincredex.model.entities.RefreshToken;
import com.example.fincredex.model.entities.User;
import com.example.fincredex.model.enums.Role;
import com.example.fincredex.model.request.LoginRequest;
import com.example.fincredex.model.request.RegistrationUserRequest;
import com.example.fincredex.model.response.IamResponse;
import com.example.fincredex.repository.UserRepository;
import com.example.fincredex.security.JwtService;
import com.example.fincredex.security.validation.AccessValidator;
import com.example.fincredex.service.AuthService;
import com.example.fincredex.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AccessValidator accessValidator;




    @Override
    public IamResponse<UserProfileDTO> registerUser(@NotNull RegistrationUserRequest request) {
        accessValidator.validateNewUser(request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getConfirmPassword());

        User newUser = userMapper.fromDto(request);
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        newUser.setRole(Role.USER);
        newUser.setCreated(LocalDateTime.now());
        userRepository.save(newUser);

        RefreshToken refreshToken = refreshTokenService.generateOrUpdateRefreshToken(newUser);
        String token = jwtService.generateAccessToken(newUser);
        UserProfileDTO userProfileDTO = userMapper.toUserProfileDTO(newUser, token, refreshToken.getToken());
        userProfileDTO.setToken(token);

        return IamResponse.createSuccessfulWithNewToken(userProfileDTO);
    }


    @Override
    public IamResponse<UserProfileDTO> login(@NotNull LoginRequest request) {
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

        } catch (BadCredentialsException e){
            throw new InvalidDataException(ApiErrorMessage.INVALID_USER_OR_PASSWORD.getMessage());
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidDataException(ApiErrorMessage.INVALID_USER_OR_PASSWORD.getMessage()));

        RefreshToken refreshToken = refreshTokenService.generateOrUpdateRefreshToken(user);
        String token = jwtService.generateAccessToken(user);
        UserProfileDTO userProfileDTO = userMapper.toUserProfileDTO(user, token, refreshToken.getToken());
        userProfileDTO.setToken(token);

        return IamResponse.createSuccessfulWithNewToken(userProfileDTO);
    }

    @Override
    public IamResponse<UserProfileDTO> refreshAccessToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenValue);
        User user = refreshToken.getUser();

        String accessToken = jwtService.generateRefreshToken(user);

        return IamResponse.createSuccessfulWithNewToken(
                userMapper.toUserProfileDTO(user, accessToken, refreshToken.getToken())
        );
    }

    @Override
    public IamResponse<UserDTO> getProfile() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        UserDTO userDTO = userMapper.toUserDTO(user);
        return IamResponse.createSuccessful(userDTO);
    }
}
