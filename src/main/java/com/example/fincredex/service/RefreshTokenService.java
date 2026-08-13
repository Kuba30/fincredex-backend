package com.example.fincredex.service;

import com.example.fincredex.model.entities.RefreshToken;
import com.example.fincredex.model.entities.User;

public interface RefreshTokenService {

    RefreshToken generateOrUpdateRefreshToken (User user);

    RefreshToken validateRefreshToken (String refreshToken);
}
