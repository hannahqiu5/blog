package com.hanqiu.blog.repositories;

import com.hanqiu.blog.domain.PostStatus;
import com.hanqiu.blog.domain.entities.Category;
import com.hanqiu.blog.domain.entities.Post;
import com.hanqiu.blog.domain.entities.Tag;
import com.hanqiu.blog.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class TagRepositoryTest {
    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;


    private Category category;

    private User user;

    private Tag tag1;
    private Tag tag2;
    private Tag tag3;

    private Post post1;
    private Post post2;


    @BeforeEach
    void setup() {
        user = User.builder()
                .email("test@test.com")
                .password("password123")
                .name("Test User")
                .build();

        category = Category.builder()
                .name("Test Category")
                .posts(new ArrayList<>())
                .build();

        tag1 = Tag.builder()
                .name("Tag 1")
                .posts(new HashSet<>())
                .build();

        tag2 = Tag.builder()
                .name("Tag 2")
                .posts(new HashSet<>())
                .build();

        tag3 = Tag.builder()
                .name("Tag 3")
                .posts(new HashSet<>())
                .build();

        userRepository.save(user);
        categoryRepository.save(category);
        tagRepository.saveAll(List.of(tag1, tag2, tag3));

        post1 = Post.builder()
                .title("Post 1")
                .content("Content 1")
                .author(user)
                .category(category)
                .status(PostStatus.PUBLISHED)
                .readingTime(1)
                .tags(new HashSet<>(Set.of(tag1)))
                .build();

        post2 = Post.builder()
                .title("Post 2")
                .content("Content 2")
                .author(user)
                .category(category)
                .status(PostStatus.PUBLISHED)
                .readingTime(1)
                .tags(new HashSet<>(Set.of(tag2)))
                .build();


        postRepository.saveAll(List.of(post1, post2));

    }

    @Test
    void findAllWithPosts_shouldReturnTagsWithPosts() {
        List<Tag> result = tagRepository.findAllWithPosts();

        assertEquals(3, result.size());
        assertEquals(List.of(tag1, tag2, tag3), result);
    }

    @Test
    void findByNameIn_shouldReturnTagList() {
        Set<String> tagNames = Set.of("TAg", "TaG", "tAg", "Tag 1", "Tag 2");

        List<Tag> result = tagRepository.findByNameIn(tagNames);

        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(tag1, tag2)));
    }

    @Test
    void saveAll_shouldReturnTagList() {
        Tag newTag1 = Tag.builder()
                .name("New Tag 1")
                .posts(new HashSet<>())
                .build();
        Tag newTag2 = Tag.builder()
                .name("New Tag 2")
                .posts(new HashSet<>())
                .build();
        List<Tag> newTags = List.of(newTag1, newTag2);

        List<Tag> result = tagRepository.saveAll(newTags);

        assertEquals(newTags, result);
    }

    @Test
    void findById_shouldReturnTag() {
        UUID id = tag1.getId();
        Optional<Tag> result = tagRepository.findById(id);

        assertTrue(result.isPresent());
        assertEquals(tag1, result.get());
    }

    @Test
    void findAllById_shouldReturnTagList() {
        Set<UUID> ids = Set.of(tag1.getId(), tag2.getId(), tag3.getId());

        List<Tag> result = tagRepository.findAllById(ids);

        assertEquals(3, result.size());
        assertTrue(result.containsAll(List.of(tag1, tag2, tag3)));
    }

    @Test
    void deleteById() {
        UUID id = tag1.getId();

        tagRepository.deleteById(id);
        Optional<Tag> result = tagRepository.findById(id);

        assertTrue(result.isEmpty());
    }
}
