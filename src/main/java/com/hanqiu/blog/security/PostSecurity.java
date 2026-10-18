package com.hanqiu.blog.security;

import com.hanqiu.blog.domain.entities.Post;
import com.hanqiu.blog.domain.entities.User;
import com.hanqiu.blog.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PostSecurity {

    private final PostRepository postRepository;

    public boolean isOwner(UUID postId, Authentication authentication) {
        BlogUserDetails userDetails = (BlogUserDetails) authentication.getPrincipal();
        UUID currentUserId = userDetails.getId();

        Post post = postRepository.findById(postId).orElseThrow();
        User postAuthor = post.getAuthor();
        UUID authorId = postAuthor.getId();

        return currentUserId.equals(authorId);
    }
}
