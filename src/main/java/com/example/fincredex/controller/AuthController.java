package com.example.fincredex.controller;

import com.example.fincredex.model.Constants.ApiLogMessage;
import com.example.fincredex.model.dto.UserDTO;
import com.example.fincredex.model.dto.UserProfileDTO;
import com.example.fincredex.model.request.LoginRequest;
import com.example.fincredex.model.request.RegistrationUserRequest;
import com.example.fincredex.model.response.IamResponse;
import com.example.fincredex.service.AuthService;
import com.example.fincredex.utils.ApiUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/auth")
@RequiredArgsConstructor
@RestController
public class AuthController {

    private final AuthService authService;


    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user an returns authentication details"
    )
    public ResponseEntity<?> register(
            @RequestBody @Valid RegistrationUserRequest request,
            HttpServletResponse response){
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        IamResponse<UserProfileDTO> result = authService.registerUser(request);
        Cookie authorizationCookie = ApiUtils.createAuthCookie(result.getPayload().getToken());
        response.addCookie(authorizationCookie);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Log in user",
            description = "Log in user an returns authentication details"
    )
    public ResponseEntity<IamResponse<UserProfileDTO>> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response) {

        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        IamResponse<UserProfileDTO> result = authService.login(request);
        Cookie authorizationCookie = ApiUtils.createAuthCookie(result.getPayload().getToken());
        response.addCookie(authorizationCookie);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/refreshToken")
    @Operation(
            summary = "RefreshToken",
            description = "Refresh Token and returns authentication details"
    )
    public ResponseEntity<IamResponse<UserProfileDTO>> refreshToken(
            @RequestParam(name = "token") String refreshToken,
            HttpServletResponse response){
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        IamResponse<UserProfileDTO> result = authService.refreshAccessToken(refreshToken);
        Cookie authorizationCookie = ApiUtils.createAuthCookie(result.getPayload().getToken());
        response.addCookie(authorizationCookie);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/getProfile")
    public ResponseEntity<IamResponse<UserDTO>> getProfile(){
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        IamResponse<UserDTO> response = authService.getProfile();
        return ResponseEntity.ok(response);
    }




}
