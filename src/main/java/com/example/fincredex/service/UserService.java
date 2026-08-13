package com.example.fincredex.service;

import com.example.fincredex.model.dto.UserDTO;
import com.example.fincredex.model.response.IamResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public interface UserService extends UserDetailsService {

    IamResponse<UserDTO> getById(@NotNull Long userId);

//    IamResponse<UserDTO> createUser(@NotNull NewUserRequest newUserRequest);
//
//    IamResponse<UserDTO> updateUser(@NotNull UpdateUserRequest updateUserRequest, @NotNull Integer userId);

//    void deleteUserById(@NotNull Integer userId);

//    IamResponse<PaginationResponse<UserSearchDTO>> getAllUsers(@NotNull Pageable pageable);
//
//    IamResponse<PaginationResponse<UserSearchDTO>> searchUser(@NotNull UserSearchRequest request, @NotNull Pageable pageable);
}
