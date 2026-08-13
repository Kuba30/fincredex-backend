package com.example.fincredex.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data

public class LoginRequest implements Serializable {

    @Email
    @NotNull
    private String email;

    @NotNull
    private String password;
}
