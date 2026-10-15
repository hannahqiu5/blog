package com.hanqiu.blog.services.impl;

import com.hanqiu.blog.domain.dtos.LoginRequest;
import com.hanqiu.blog.domain.dtos.LoginResponse;
import com.hanqiu.blog.domain.dtos.RegisterRequest;
import com.hanqiu.blog.domain.entities.User;
import com.hanqiu.blog.repositories.UserRepository;
import com.hanqiu.blog.services.AuthenticationService;
import com.hanqiu.blog.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(("User not found by id " + id)));
    }


}
