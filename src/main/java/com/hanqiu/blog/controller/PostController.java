package com.hanqiu.blog.controller;

import com.hanqiu.blog.domain.PostStatus;
import com.hanqiu.blog.domain.dtos.PostDto;
import com.hanqiu.blog.domain.entities.Post;
import com.hanqiu.blog.mappers.PostMapper;
import com.hanqiu.blog.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {
    private  final PostService postService;
    private final PostMapper postMapper;

    @GetMapping
    public ResponseEntity<List<PostDto>> getAllPosts(
            @RequestBody(required = false) UUID categoryId,
            @RequestBody(required = false) UUID tagId
    ) {
        List<Post> posts = postService.getAllPosts(categoryId, tagId);
        List<PostDto> postDtos = posts.stream().map(postMapper::toDto).toList();
        return ResponseEntity.ok(postDtos);
    }
}
