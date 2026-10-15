package com.hanqiu.blog.services;

import com.hanqiu.blog.domain.dtos.LoginRequest;
import com.hanqiu.blog.domain.dtos.LoginResponse;
import com.hanqiu.blog.domain.dtos.RegisterRequest;
import com.hanqiu.blog.domain.entities.User;

import java.util.UUID;

public interface UserService {
    User getUserById(UUID id);

}
