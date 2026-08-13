package com.example.fincredex.model.dto;

import com.example.fincredex.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class UserProfileDTO implements Serializable {
    private Integer id;
    private String username;
    private String email;
    private String token;
    private String refreshToken;
    private Role role;
}
