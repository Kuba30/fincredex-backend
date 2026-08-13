package com.example.fincredex.service;

import com.example.fincredex.model.dto.UserDTO;
import com.example.fincredex.model.dto.UserProfileDTO;
import com.example.fincredex.model.request.LoginRequest;
import com.example.fincredex.model.request.RegistrationUserRequest;
import com.example.fincredex.model.response.IamResponse;

public interface AuthService {
    IamResponse<UserProfileDTO> registerUser(RegistrationUserRequest request);

    IamResponse<UserProfileDTO> login(LoginRequest request);

    IamResponse<UserProfileDTO> refreshAccessToken(String refreshToken);

    IamResponse<UserDTO> getProfile();
}
