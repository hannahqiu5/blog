package com.hanqiu.blog.controller;

import com.hanqiu.blog.domain.CreatePostRequest;
import com.hanqiu.blog.domain.PostStatus;
import com.hanqiu.blog.domain.UpdatePostRequest;
import com.hanqiu.blog.domain.dtos.*;
import com.hanqiu.blog.domain.entities.Category;
import com.hanqiu.blog.domain.entities.Post;
import com.hanqiu.blog.domain.entities.User;
import com.hanqiu.blog.mappers.PostMapper;
import com.hanqiu.blog.services.PostService;
import com.hanqiu.blog.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(PostController.class)
public class PostControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private PostMapper postMapper;

    @MockitoBean
    private UserService userService;

    private Post post;
    private PostDto postDto;
    private CreatePostRequest request;
    private CreatePostRequestDto requestDto;
    private User user;
    private AuthorDto authorDto;
    private Category category;
    private CategoryDto categoryDto;


    @BeforeEach
    void setup() {
        user = User.builder()
                .id(UUID.randomUUID())
                .email("test@test.com")
                .password("pass123")
                .name("USER1")
                .build();

        authorDto = AuthorDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();

        category = Category.builder()
                .id(UUID.randomUUID())
                .name("Test")
                .build();

        categoryDto = CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .postCount(0)
                .build();

        post = Post.builder()
                .id(UUID.randomUUID())
                .title("Test Title")
                .content("Test Content")
                .author(user)
                .category(category)
                .build();


        postDto = PostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(authorDto)
                .category(categoryDto)
                .build();

        request = CreatePostRequest.builder()
                .title("Test Title")
                .content("Test Content")
                .categoryId(category.getId())
                .build();

        requestDto = CreatePostRequestDto.builder()
                .title("Test Title")
                .content("Test Content")
                .categoryId(category.getId())
                .build();
    }

    @Test
    void getAllPosts_shouldReturn200() throws Exception {
        when(postService.getAllPosts(null, null))
                .thenReturn(List.of(post));

        when(postMapper.toDto(post))
                .thenReturn(postDto);

        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Title"))
                .andExpect(jsonPath("$[0].content").value("Test Content"));

        verify(postService).getAllPosts(null, null);
        verify(postMapper).toDto(post);
    }

    @Test
    void getPost_shouldReturn200() throws Exception {
        when(postService.getPost(post.getId()))
                .thenReturn(post);
        when(postMapper.toDto(post))
                .thenReturn(postDto);

        mockMvc.perform(get("/api/v1/posts/{id}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId().toString()))
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.content").value("Test Content"));

        verify(postService).getPost(post.getId());
        verify(postMapper).toDto(post);
    }

    @Test
    void getDrafts_shouldReturn200() throws Exception {
        when(userService.getUserById(user.getId()))
                .thenReturn(user);
        when(postService.getDraftPosts(user))
                .thenReturn(List.of(post));
        when(postMapper.toDto(post))
                .thenReturn(postDto);

        mockMvc.perform(get("/api/v1/posts/drafts")
                        .requestAttr("userId", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Title"))
                .andExpect(jsonPath("$[0].content").value("Test Content"));

        verify(userService).getUserById(user.getId());
        verify(postService).getDraftPosts(user);
        verify(postMapper).toDto(post);
    }

    @Test
    void createPost_shouldReturn201() throws Exception {
        when(userService.getUserById(user.getId()))
                .thenReturn(user);
        when(postMapper.toCreatePostRequest(requestDto))
                .thenReturn(request);
        when(postService.createPost(user, request))
                .thenReturn(post);
        when(postMapper.toDto(post))
                .thenReturn(postDto);

        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .requestAttr("userId", user.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.content").value("Test Content"));

        verify(userService).getUserById(user.getId());
        verify(postMapper).toCreatePostRequest(requestDto);
        verify(postService).createPost(user, request);
        verify(postMapper).toDto(post);
    }

    @Test
    void updatePost_shouldReturn200() throws Exception {
        UpdatePostRequest updatePostRequest = UpdatePostRequest.builder()
                .id(post.getId())
                .title("Updated Title")
                .content("Updated Content")
                .categoryId(category.getId())
                .status(PostStatus.PUBLISHED)
                .build();
        UpdatePostRequestDto updatePostRequestDto = UpdatePostRequestDto.builder()
                .id(post.getId())
                .title("Updated Title")
                .content("Updated Content")
                .categoryId(category.getId())
                .status(PostStatus.PUBLISHED)
                .build();
        Post updatedPost = Post.builder()
                .id(post.getId())
                .title("Updated Title")
                .content("Updated Content")
                .author(user)
                .category(category)
                .build();
        PostDto updatedPostDto = PostDto.builder()
                .id(post.getId())
                .title("Updated Title")
                .content("Updated Content")
                .author(authorDto)
                .category(categoryDto)
                .build();

        when(postMapper.toUpdatePostRequest(updatePostRequestDto))
                .thenReturn(updatePostRequest);
        when(postService.updatePost(post.getId(), updatePostRequest))
                .thenReturn(updatedPost);
        when(postMapper.toDto(updatedPost))
                .thenReturn(updatedPostDto);

        mockMvc.perform(put("/api/v1/posts/{id}", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePostRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.content").value("Updated Content"));

        verify(postMapper).toUpdatePostRequest(updatePostRequestDto);
        verify(postService).updatePost(post.getId(), updatePostRequest);
        verify(postMapper).toDto(updatedPost);
    }


    @Test
    void deletePost_shouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/posts/{id}", id))
                .andExpect(status().isNoContent());
        verify(postService).deletePost(id);
    }

}
