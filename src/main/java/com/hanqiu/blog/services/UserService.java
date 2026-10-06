package com.hanqiu.blog.services;

import com.hanqiu.blog.domain.entities.User;

import java.util.UUID;

public interface UserService {
    User getUserById(UUID id);
}
