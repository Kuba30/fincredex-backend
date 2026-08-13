package com.example.fincredex.service.impl;

import com.example.fincredex.exception.NotFoundException;
import com.example.fincredex.model.Constants.ApiErrorMessage;
import com.example.fincredex.model.entities.RefreshToken;
import com.example.fincredex.model.entities.User;
import com.example.fincredex.repository.RefreshTokenRepository;
import com.example.fincredex.service.RefreshTokenService;
import com.example.fincredex.utils.ApiUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration-days:7}")
    private long refreshTokenExpirationDays;

    @Override
    public RefreshToken generateOrUpdateRefreshToken(User user) {
        return refreshTokenRepository.findByUserId(user.getId())
                .map(existing -> {
                    // ✅ Rotate token + reset expiry
                    existing.setToken(ApiUtils.generateUuidWithoutDash());
                    existing.setExpiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays));
                    return refreshTokenRepository.save(existing);
                })
                .orElseGet(() -> {
                    // ✅ created handled by @PrePersist, expiresAt explicitly set
                    RefreshToken newToken = new RefreshToken();
                    newToken.setUser(user);
                    newToken.setToken(ApiUtils.generateUuidWithoutDash());
                    newToken.setExpiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays));
                    return refreshTokenRepository.save(newToken);
                });
    }

    @Override
    public RefreshToken validateRefreshToken(String requestRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new NotFoundException(
                        ApiErrorMessage.NOT_FOUND_REFRESH_TOKEN.getMessage()));



        // ✅ Rotate: new token value + new expiry
        refreshToken.setToken(ApiUtils.generateUuidWithoutDash());
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays));
        return refreshTokenRepository.save(refreshToken);
    }
}