package com.hanqiu.blog.repositories;

import com.hanqiu.blog.domain.PostStatus;
import com.hanqiu.blog.domain.entities.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PostRepositoryTest {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

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
                .build();
        user2 = User.builder()
                .email("u2@test.com")
                .password("password123")
                .name("Test user2")
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

        List<Post> result = postRepository.findAllByStatusAndCategoryAndTagsContaining(
                PostStatus.PUBLISHED,
                cat,
                tag
        );

        assertEquals(4, result.size());
        assertTrue(result.contains(postWithCatTagPublished));
        assertTrue(result.contains(publishedPostWithCatTag));
        assertTrue(result.contains(postWithCat1TagPublished ));
        assertTrue(result.contains(postWithCatTagPublishedByUser2 ));

    }


    @Test
    void findAllByStatusAndCategory_shouldReturnPosts() {
        List<Post> result = postRepository.findAllByStatusAndCategory(
                PostStatus.DRAFT,
                cat
        );

        Assertions.assertEquals(1, result.size());
        assertTrue(result.contains(draftPostWithCatTag));
    }

    @Test
    void findAllByStatusAndTagsContaining_shouldReturnPosts() {
        List<Post> result = postRepository.findAllByStatusAndTagsContaining(
                PostStatus.PUBLISHED,
                tag2
        );

        Assertions.assertEquals(1, result.size());
        assertTrue(result.contains(postWithCatTag2Published));
    }



    @Test
    void findAllByStatus_shouldReturnPosts() {
        List<Post> result = postRepository.findAllByStatus(
                PostStatus.DRAFT
        );

        Assertions.assertEquals(1, result.size());
        assertTrue(result.contains(draftPostWithCatTag));
    }


    @Test
    void findAllByAuthorAndStatus_shouldReturnPosts() {
        List<Post> result = postRepository.findAllByAuthorAndStatus(
                user2,
                PostStatus.PUBLISHED
        );

        Assertions.assertEquals(1, result.size());
        assertTrue(result.contains(postWithCatTagPublishedByUser2));
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

}
