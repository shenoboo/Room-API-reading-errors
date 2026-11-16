package com.dsu.roomapi;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final long ttlMinutes;

    public AuthService(AppUserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtEncoder jwtEncoder,
                       @Value("${app.jwt.ttl-minutes:30}") long ttlMinutes) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.ttlMinutes = ttlMinutes;
    }

    @Transactional
    public AppUser signup(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("username " + username + " is taken");
        }
        return userRepository.save(new AppUser(
                username, passwordEncoder.encode(password), "USER"));
    }

    @Transactional
    public TokenResponse login(String username, String password) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("wrong username or password"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("wrong username or password");
        }

        String accessToken = generateAccessToken(user);
        String refreshToken = createOrUpdateRefreshToken(user);

        return new TokenResponse(accessToken, "Bearer", ttlMinutes * 60, refreshToken);
    }

    @Transactional
    public TokenResponse refreshToken(String refreshTokenString) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshTokenString)
                .orElseThrow(() -> new BadCredentialsException("token is invalid or expired"));

        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new BadCredentialsException("token is invalid or expired");
        }

        AppUser user = token.getUser();
        String newAccessToken = generateAccessToken(user);
        return new TokenResponse(newAccessToken, "Bearer", ttlMinutes * 60, token.getToken());
    }

    @Transactional
    public void logout(String refreshTokenString) {
        if (refreshTokenString != null && !refreshTokenString.isBlank()) {
            refreshTokenRepository.deleteByToken(refreshTokenString);
        }
    }

    @Transactional
    public void logoutUser(String username) {
        userRepository.findByUsername(username)
                .ifPresent(refreshTokenRepository::deleteByUser);
    }

    private String generateAccessToken(AppUser user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("room-api")
                .issuedAt(now)
                .expiresAt(now.plus(ttlMinutes, ChronoUnit.MINUTES))
                .subject(user.getUsername())
                .claim("role", user.getRole())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private String createOrUpdateRefreshToken(AppUser user) {
        refreshTokenRepository.deleteByUser(user);
        String tokenString = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plus(7, ChronoUnit.DAYS);
        RefreshToken refreshToken = new RefreshToken(tokenString, user, expiryDate);
        refreshTokenRepository.save(refreshToken);
        return tokenString;
    }
}

