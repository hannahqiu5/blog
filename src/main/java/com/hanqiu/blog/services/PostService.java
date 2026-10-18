package com.hanqiu.blog.services;

import com.hanqiu.blog.domain.CreatePostRequest;
import com.hanqiu.blog.domain.UpdatePostRequest;
import com.hanqiu.blog.domain.entities.Post;
import com.hanqiu.blog.domain.entities.User;
import com.hanqiu.blog.domain.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PostService {

    Page<Post> getAllPosts(UUID categoryId, UUID tagId, Pageable pageable);

    List<Post> getDraftPosts(User user);

    Post createPost(User user, CreatePostRequest createPostRequest);

    Post updatePost(UUID id, UpdatePostRequest updatePostRequest);

    Post getPost(UUID id);

    void deletePost(UUID id);

}
