package com.example.stockgestion.services;

import com.example.stockgestion.Dto.request.LoginRequestDto;
import com.example.stockgestion.Dto.response.AuthResponseDto;
import com.example.stockgestion.config.JwtConfigProperties;
import com.example.stockgestion.models.RefreshToken;
import com.example.stockgestion.models.User;
import com.example.stockgestion.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Service for authentication operations
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtConfigProperties jwtConfig;

    /**
     * Authenticate user and generate tokens
     */
    public AuthResponseDto login(LoginRequestDto request) {
        // Authenticate user credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        // Load user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        User user = userDetailsService.loadUserEntityByEmail(request.getEmail());

        // Generate access token
        String accessToken = jwtService.generateAccessToken(
                userDetails,
                user.getRole().name(),
                user.getClient() != null ? user.getClient().getId() : null);

        // Generate refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // Build response
        return AuthResponseDto.builder()
                .email(user.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getAccessTokenExpiry() / 1000) // Convert to seconds
                .role(user.getRole())
                .clientId(user.getClient() != null ? user.getClient().getId().toString() : null)
                .build();
    }

    /**
     * Refresh access token using refresh token
     */
    public AuthResponseDto refreshAccessToken(String refreshTokenStr) {
        // Find and validate refresh token
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        refreshToken = refreshTokenService.verifyExpiration(refreshToken);

        // Get user
        User user = refreshToken.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        // Generate new access token
        String accessToken = jwtService.generateAccessToken(
                userDetails,
                user.getRole().name(),
                user.getClient() != null ? user.getClient().getId() : null);

        // Rotate refresh token (delete old, create new)
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken);

        // Build response
        return AuthResponseDto.builder()
                .email(user.getEmail())
                .accessToken(accessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getAccessTokenExpiry() / 1000) // Convert to seconds
                .role(user.getRole())
                .clientId(user.getClient() != null ? user.getClient().getId().toString() : null)
                .build();
    }

    /**
     * Logout user by revoking refresh token
     */
    public void logout(String refreshTokenStr) {
        refreshTokenService.revokeToken(refreshTokenStr);
    }
}
