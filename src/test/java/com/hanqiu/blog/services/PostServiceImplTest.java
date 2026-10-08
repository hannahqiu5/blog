package com.hanqiu.blog.services;

import com.hanqiu.blog.domain.CreatePostRequest;
import com.hanqiu.blog.domain.PostStatus;
import com.hanqiu.blog.domain.UpdatePostRequest;
import com.hanqiu.blog.domain.entities.Category;
import com.hanqiu.blog.domain.entities.Post;
import com.hanqiu.blog.domain.entities.Tag;
import com.hanqiu.blog.domain.entities.User;
import com.hanqiu.blog.repositories.PostRepository;
import com.hanqiu.blog.services.impl.CategoryServiceImpl;
import com.hanqiu.blog.services.impl.PostServiceImpl;
import com.hanqiu.blog.services.impl.TagServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PostServiceImplTest {
    @Mock
    private PostRepository postRepository;

    @Mock
    private CategoryServiceImpl categoryService;

    @Mock
    private TagServiceImpl tagService;

    @InjectMocks
    private PostServiceImpl postService;

    private Post post1, post2;
    private Category category;
    private Tag tag;
    private User user;
    private List<Post> posts;

    @BeforeEach
    void setup() {
        category = Category.builder()
                .id(UUID.randomUUID())
                .name("Test Category")
                .build();

        tag = Tag.builder()
                .id(UUID.randomUUID())
                .name("Test Tag")
                .build();
        user = User.builder()
                .id(UUID.randomUUID())
                .email("test@test.com")
                .password("pass123")
                .name("u1")
                .build();

        post1 = Post.builder()
                .id(UUID.randomUUID())
                .title("Post1")
                .content("Content of post1")
                .author(user)
                .category(category)
                .tags(Set.of(tag))
                .build();

        post2 = Post.builder()
                .id(UUID.randomUUID())
                .title("Post2")
                .content("Content of post2")
                .author(user)
                .category(category)
                .tags(Set.of(tag))
                .build();

        posts = List.of(post1, post2);
    }

    @Test
    void getAllPosts() {
        when(categoryService.getCategoryById(category.getId()))
                .thenReturn(category);
        when(tagService.getTagById(tag.getId()))
                .thenReturn(tag);
        when(postRepository.findAllByStatusAndCategoryAndTagsContaining(PostStatus.PUBLISHED, category, tag))
                .thenReturn(posts);

        List<Post> result = postService.getAllPosts(category.getId(), tag.getId());

        assertEquals(posts, result);

        verify(categoryService).getCategoryById(category.getId());
        verify(tagService).getTagById(tag.getId());
    }

    @Test
    void getDraftPosts() {
        when(postRepository.findAllByAuthorAndStatus(user, PostStatus.DRAFT))
                .thenReturn(posts);

        List<Post> result = postService.getDraftPosts(user);
        assertEquals(posts, result);
    }

    @Test
    void createPost() {
        CreatePostRequest createPostRequest = CreatePostRequest.builder()
                .title("New Post")
                .content("abc def")
                .categoryId(category.getId())
                .tagIds(Set.of(tag.getId()))
                .build();

        Post newPost = Post.builder()
                .id(UUID.randomUUID())
                .title("New Post")
                .content("abc def")
                .author(user)
                .category(category)
                .tags(Set.of(tag))
                .build();

        when(categoryService.getCategoryById(category.getId()))
                .thenReturn(category);
        when(tagService.getTagByIds(Set.of(tag.getId())))
                .thenReturn(List.of(tag));
        when(postRepository.save(any(Post.class)))
                .thenReturn(newPost);

        Post result = postService.createPost(user, createPostRequest);

        assertEquals(newPost, result);

        ArgumentCaptor<Post> captor =
                ArgumentCaptor.forClass(Post.class);

        verify(postRepository).save(captor.capture());

        Post savedPost = captor.getValue();

        assertEquals("New Post", savedPost.getTitle());
        assertEquals("abc def", savedPost.getContent());
        assertEquals(user, savedPost.getAuthor());
        assertEquals(category, savedPost.getCategory());
        assertEquals(Set.of(tag), savedPost.getTags());
    }

    @Test
    void getPost() {
        when(postRepository.findById(post1.getId()))
                .thenReturn(Optional.ofNullable(post1));

        Post result = postService.getPost(post1.getId());

        assertEquals(post1, result);
    }

    @Test
    void updatePost() {
        UUID postId = post1.getId();

        Category newCategory = Category.builder()
                .id(UUID.randomUUID())
                .name("New Category")
                .build();


        Tag newTag = Tag.builder()
                .id(UUID.randomUUID())
                .name("new")
                .build();

        UpdatePostRequest updatePostRequest = UpdatePostRequest.builder()
                .title("Updated Title")
                .content("This is the updated content")
                .categoryId(newCategory.getId())
                .tagIds(Set.of(newTag.getId()))
                .status(PostStatus.PUBLISHED)
                .build();

        when(postRepository.findById(postId))
                .thenReturn(Optional.of(post1));
        when(categoryService.getCategoryById(newCategory.getId()))
                .thenReturn(newCategory);
        when(postRepository.save(post1))
                .thenReturn(post1);

        Post updatedPost = postService.updatePost(postId,
                updatePostRequest);

        assertEquals(post1, updatedPost);

        verify(postRepository).findById(postId);
        verify(categoryService).getCategoryById(newCategory.getId());
        verify(tagService).getTagByIds(Set.of(newTag.getId()));
        verify(postRepository).save(post1);
    }

    @Test
    void deletePost() {
        when(postRepository.existsById(post1.getId()))
                .thenReturn(true);
        postService.deletePost(post1.getId());

        verify(postRepository).deleteById(post1.getId());
    }
}
