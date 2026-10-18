package com.hanqiu.blog.repositories;

import com.hanqiu.blog.domain.entities.Category;
import com.hanqiu.blog.domain.entities.Post;
import com.hanqiu.blog.domain.entities.Tag;
import com.hanqiu.blog.domain.entities.User;
import com.hanqiu.blog.domain.enums.PostStatus;
import com.hanqiu.blog.domain.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class PostRepositoryTest {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TagRepository tagRepository;

    private User user1;
    private User user2;
    private Category cat;
    private Category cat2;
    private Tag tag;
    private Tag tag2;

    private Post postWithCatTagPublished;
    private Post postWithCatTag2Published;

    private Post publishedPostWithCatTag;
    private Post draftPostWithCatTag;

    private Post postWithCat1TagPublished;
    private Post postWithCat2TagPublished;

    private Post postWithCatTagPublishedByUser2;


    @BeforeEach
    void setup() {

        user1 = User.builder()
                .email("u1@test.com")
                .password("password123")
                .name("Test user1")
                .role(Role.ADMIN)
                .build();
        user2 = User.builder()
                .email("u2@test.com")
                .password("password123")
                .name("Test user2")
                .role(Role.ADMIN)
                .build();

        cat = Category.builder()
                .name("Category 1")
                .posts(new ArrayList<>())
                .build();

        cat2 = Category.builder()
                .name("Category 2")
                .posts(new ArrayList<>())
                .build();

        tag = Tag.builder()
                .name("Tag 1")
                .build();

        tag2 = Tag.builder()
                .name("Tag 2")
                .build();


        // Different tags
        postWithCatTagPublished = Post.builder()
                .title("Test Post")
                .content("Test content")
                .author(user1)
                .category(cat)
                .tags(new HashSet<>(Set.of(tag)))
                .status(PostStatus.PUBLISHED)
                .readingTime(1)
                .build();

        postWithCatTag2Published = Post.builder()
                .title("Test Post")
                .content("Test content")
                .author(user1)
                .category(cat)
                .tags(new HashSet<>(Set.of(tag2)))
                .status(PostStatus.PUBLISHED)
                .readingTime(1)
                .build();


        // Different status
        publishedPostWithCatTag = Post.builder()
                .title("Test Post")
                .content("Test content")
                .author(user1)
                .category(cat)
                .tags(new HashSet<>(Set.of(tag)))
                .status(PostStatus.PUBLISHED)
                .readingTime(1)
                .build();

        draftPostWithCatTag = Post.builder()
                .title("Test Post")
                .content("Test content")
                .author(user1)
                .category(cat)
                .tags(new HashSet<>(Set.of(tag)))
                .status(PostStatus.DRAFT)
                .readingTime(1)
                .build();

        // Different categories
        postWithCat1TagPublished = Post.builder()
                .title("Test Post")
                .content("Test content")
                .author(user1)
                .category(cat)
                .tags(new HashSet<>(Set.of(tag)))
                .status(PostStatus.PUBLISHED)
                .readingTime(1)
                .build();

        postWithCat2TagPublished = Post.builder()
                .title("Test Post")
                .content("Test content")
                .author(user1)
                .category(cat2)
                .tags(new HashSet<>(Set.of(tag)))
                .status(PostStatus.PUBLISHED)
                .readingTime(1)
                .build();

        // Different user
        postWithCatTagPublishedByUser2 = Post.builder()
                .title("Test Post")
                .content("Test content")
                .author(user2)
                .category(cat)
                .tags(new HashSet<>(Set.of(tag)))
                .status(PostStatus.PUBLISHED)
                .readingTime(1)
                .build();

        userRepository.save(user1);
        userRepository.save(user2);
        categoryRepository.saveAll(List.of(cat, cat2));
        tagRepository.saveAll(List.of(tag, tag2));

        postRepository.saveAll(List.of(
                postWithCatTagPublished,
                postWithCatTag2Published,
                publishedPostWithCatTag,
                draftPostWithCatTag,
                postWithCat1TagPublished,
                postWithCat2TagPublished,
                postWithCatTagPublishedByUser2
        ));
    }

    @Test
    void findAllByStatusAndCategoryAndTagsContaining_shouldReturnPosts() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Post> result =
                postRepository.findAllByStatusAndCategoryAndTagsContaining(
                        PostStatus.PUBLISHED,
                        cat,
                        tag,
                        pageable
                );

        assertEquals(4, result.getTotalElements());

        assertTrue(result.getContent().contains(postWithCatTagPublished));
        assertTrue(result.getContent().contains(publishedPostWithCatTag));
        assertTrue(result.getContent().contains(postWithCat1TagPublished));
        assertTrue(result.getContent().contains(postWithCatTagPublishedByUser2));

        assertTrue(result.getContent().stream()
                .allMatch(post -> post.getStatus() == PostStatus.PUBLISHED));
    }

    @Test
    void findAllByStatusAndCategory_shouldReturnPosts() {
        Pageable pageable = PageRequest.of(0, 5);

        Page<Post> result = postRepository.findAllByStatusAndCategory(
                PostStatus.PUBLISHED,
                cat,
                pageable
        );

        assertEquals(5, result.getTotalElements());
        assertTrue(result.getContent().contains(postWithCatTagPublished));
        assertTrue(result.getContent().contains(publishedPostWithCatTag));
    }

    @Test
    void findAllByStatus_shouldReturnDraftPosts() {
        Pageable pageable = PageRequest.of(0, 5);

        Page<Post> result = postRepository.findAllByStatus(
                PostStatus.DRAFT,
                pageable
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        assertTrue(result.getContent().contains(draftPostWithCatTag));

        assertTrue(result.getContent().stream()
                .allMatch(post -> post.getStatus() == PostStatus.DRAFT));
    }

    @Test
    void save_shouldReturnSavedPost() {
        Post newPost = Post.builder()
                .title("New Post")
                .content("New content")
                .author(user1)
                .category(cat)
                .tags(new HashSet<>(Set.of(tag)))
                .status(PostStatus.PUBLISHED)
                .readingTime(1)
                .build();

        Post result = postRepository.save(newPost);

        assertEquals(newPost, result);
    }


    @Test
    void findAllByStatus_ReturnsPagedPublishedPosts() {
        Pageable pageable = PageRequest.of(
                0,
                2,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Post> result = postRepository.findAllByStatus(
                PostStatus.PUBLISHED,
                pageable
        );

        // Verify page size
        assertEquals(2, result.getContent().size());

        // Verify total number of published posts
        assertEquals(6, result.getTotalElements());

        // Verify total pages
        assertEquals(3, result.getTotalPages());

        // Verify current page (zero-based)
        assertEquals(0, result.getNumber());

        // Verify page size
        assertEquals(2, result.getSize());

        // Verify all posts are published
        assertTrue(result.getContent().stream()
                .allMatch(post -> post.getStatus() == PostStatus.PUBLISHED));
    }

    @Test
    void findAllByStatus_ReturnsSecondPage() {
        Pageable firstPageRequest = PageRequest.of(
                0,
                2,
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Pageable secondPageRequest = PageRequest.of(
                1,
                2,
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Page<Post> firstPage = postRepository.findAllByStatus(
                PostStatus.PUBLISHED, firstPageRequest
        );

        Page<Post> secondPage = postRepository.findAllByStatus(
                PostStatus.PUBLISHED, secondPageRequest
        );

        assertEquals(0, firstPage.getNumber());
        assertEquals(1, secondPage.getNumber());

        assertEquals(2, firstPage.getContent().size());
        assertEquals(2, secondPage.getContent().size());

        Set<java.util.UUID> firstPageIds = firstPage.getContent()
                .stream()
                .map(Post::getId)
                .collect(java.util.stream.Collectors.toSet());

        assertTrue(secondPage.getContent().stream()
                .map(Post::getId)
                .noneMatch(firstPageIds::contains));
    }

    @Test
    void findAllByStatus_ReturnsEmptyPageWhenNoPostsMatch() {
        Pageable pageable = PageRequest.of(0, 5);

        Page<Post> result = postRepository.findAllByStatus(
                null,
                pageable
        );

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
    }
}
