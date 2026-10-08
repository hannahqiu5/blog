package com.hanqiu.blog.repositories;

import com.hanqiu.blog.domain.PostStatus;
import com.hanqiu.blog.domain.entities.Category;
import com.hanqiu.blog.domain.entities.Post;
import com.hanqiu.blog.domain.entities.Tag;
import com.hanqiu.blog.domain.entities.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class CategoryRepositoryTest {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private EntityManager entityManager;

    private Category cat;

    @BeforeEach
    void setup() {

        cat = Category.builder()
                .name("cat")
                .posts(new ArrayList<>())
                .build();

    }

    @Test
    void existsByNameIgnoreCase_whenNameExists_shouldReturnsTrue() {
        categoryRepository.save(cat);

        boolean result = categoryRepository.existsByNameIgnoreCase(cat.getName());

        assertTrue(result);
    }

    @Test
    void existsByNameIgnoreCase_whenNameDoesNotExist_shouldReturnsFalse() {
        categoryRepository.save(cat);

        boolean result = categoryRepository.existsByNameIgnoreCase("random cat name");

        assertFalse(result);
    }

    @Test
    void save_shouldReturnSavedCategory() {
        Category savedCategory = categoryRepository.save(cat);

        assertEquals(cat, savedCategory);
        assertEquals(cat.getId(), savedCategory.getId());
        assertEquals(cat.getName(), savedCategory.getName());
        assertEquals(cat.getPosts(), savedCategory.getPosts());
    }

    @Test
    void deleteById() {
        categoryRepository.save(cat);

        categoryRepository.deleteById(cat.getId());

        Optional<Category> returnedCategory = categoryRepository.findById(cat.getId());

        assertTrue(returnedCategory.isEmpty());
    }

    @Test
    void findAllWithPostCount_shouldReturnsCategoriesWithPosts() {
        Tag tag = Tag.builder()
                .name("Test Tag")
                .build();

        User user = User.builder()
                .email("test@test.com")
                .password("pass123")
                .name("u1")
                .build();

        categoryRepository.save(cat);
        tagRepository.save(tag);
        userRepository.save(user);

        Post post1 = Post.builder()
                .title("Post1")
                .content("Content 1")
                .author(user)
                .category(cat)
                .tags(Set.of(tag))
                .readingTime(1)
                .status(PostStatus.PUBLISHED)
                .build();

        Post post2 = Post.builder()
                .title("Post2")
                .content("Content 2")
                .author(user)
                .category(cat)
                .tags(Set.of(tag))
                .readingTime(1)
                .status(PostStatus.PUBLISHED)
                .build();

        postRepository.saveAll(List.of(post1, post2));
        entityManager.flush();
        entityManager.clear();

        List<Category> result = categoryRepository.findAllWithPostCount();

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getPosts().size());
    }

}
