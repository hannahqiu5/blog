package com.hanqiu.blog.services.impl;

import com.hanqiu.blog.domain.dtos.LoginRequest;
import com.hanqiu.blog.domain.dtos.LoginResponse;
import com.hanqiu.blog.domain.dtos.RegisterRequest;
import com.hanqiu.blog.domain.entities.User;
import com.hanqiu.blog.repositories.UserRepository;
import com.hanqiu.blog.services.AuthenticationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Long jwtExpiryMs = 86400000L;
    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    public UserDetails authenticate(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        return userDetailsService.loadUserByUsername(email);
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiryMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public UserDetails validateToken(String token) {
        String username = extractUsername(token);
        return userDetailsService.loadUserByUsername(username);
    }

    @Override
    public User register(RegisterRequest request) {
        Optional<User> user = userRepository.findByEmail(request.getEmail());
        if(user.isPresent()) {
            throw new IllegalStateException("User already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User newUser = User.builder()
                .email(request.getEmail())
                .name(request.getUsername())
                .password(encodedPassword)
                .createdAt(LocalDateTime.now())
                .build();
        return   userRepository.save(newUser);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        UserDetails userDetails =
                authenticate(
                        request.getEmail(),
                        request.getPassword()
                );

        String jwt =
                generateToken(userDetails);

        return new LoginResponse(jwt, jwtExpiryMs);
    }

    private String extractUsername(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
