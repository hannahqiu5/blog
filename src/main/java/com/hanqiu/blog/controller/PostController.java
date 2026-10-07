package com.hanqiu.blog.controller;

import com.hanqiu.blog.domain.CreatePostRequest;
import com.hanqiu.blog.domain.UpdatePostRequest;
import com.hanqiu.blog.domain.dtos.CreatePostRequestDto;
import com.hanqiu.blog.domain.dtos.PostDto;
import com.hanqiu.blog.domain.dtos.UpdatePostRequestDto;
import com.hanqiu.blog.domain.entities.Post;
import com.hanqiu.blog.domain.entities.User;
import com.hanqiu.blog.mappers.PostMapper;
import com.hanqiu.blog.services.PostService;
import com.hanqiu.blog.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final PostMapper postMapper;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<PostDto>> getAllPosts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID tagId
    ) {
        List<Post> posts = postService.getAllPosts(categoryId, tagId);
        List<PostDto> postDtos = posts.stream().map(postMapper::toDto).toList();
        return ResponseEntity.ok(postDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getPost(@PathVariable UUID id) {
        Post post = postService.getPost(id);
        PostDto dto = postMapper.toDto(post);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/drafts")
    public ResponseEntity<List<PostDto>> getDrafts(
            @RequestAttribute UUID userId) {
        User loggedInUser = userService.getUserById(userId);
        List<PostDto> postDtos = postService.getDraftPosts(loggedInUser).stream().map(postMapper::toDto).toList();
        return ResponseEntity.ok(postDtos);
    }

    @PostMapping
    public ResponseEntity<PostDto> createPost(
            @RequestBody CreatePostRequestDto createPostRequestDto,
            @Valid @RequestAttribute UUID userId
    ) {

        User loggedInUser = userService.getUserById(userId);
        CreatePostRequest createPostRequest = postMapper.toCreatePostRequest(createPostRequestDto);
        Post createPost = postService.createPost(loggedInUser, createPostRequest);
        PostDto createdPostDto = postMapper.toDto(createPost);
        return new ResponseEntity<>(createdPostDto, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePostRequestDto updatePostRequestDto
    ) {
        UpdatePostRequest updatePostRequest = postMapper.toUpdatePostRequest(updatePostRequestDto);
        Post updatedPost = postService.updatePost(id, updatePostRequest);
        PostDto dto = postMapper.toDto(updatedPost);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
