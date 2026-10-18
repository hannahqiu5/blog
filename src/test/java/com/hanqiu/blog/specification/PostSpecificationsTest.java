package com.hanqiu.blog.specification;

import com.hanqiu.blog.domain.entities.Category;
import com.hanqiu.blog.domain.entities.Post;
import com.hanqiu.blog.domain.entities.Tag;
import com.hanqiu.blog.domain.entities.User;
import com.hanqiu.blog.domain.enums.PostStatus;
import com.hanqiu.blog.domain.enums.Role;
import com.hanqiu.blog.repositories.PostRepository;
import com.hanqiu.blog.specifications.PostSpecifications;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class PostSpecificationsTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private EntityManager entityManager;

    private User author;
    private Category category;

    @BeforeEach
    void setUp() {
        author = User.builder()
                .email("test@example.com")
                .password("password")
                .name("Test User")
                .role(Role.USER)
                .build();

        category = Category.builder()
                .name("Technology")
                .build();

        entityManager.persist(author);
        entityManager.persist(category);
        entityManager.flush();
    }


    @Test
    void hasKeyword_shouldMatchTitleIgnoringCase() {
        createPost("Spring Boot Guide", "A programming tutorial");
        createPost("Java Guide", "Learn Java programming");

        entityManager.flush();
        entityManager.clear();

        Specification<Post> specification =
                PostSpecifications.isPublished()
                        .and(PostSpecifications.hasKeyword("SPRING"));

        Page<Post> result = postRepository.findAll(
                specification,
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals("Spring Boot Guide",
                result.getContent().get(0).getTitle());
    }

    @Test
    void hasKeyword_shouldMatchContentIgnoringCase() {
        createPost("Spring Guide", "Learn JAVA programming");
        createPost("Python Guide", "Learn Python programming");

        entityManager.flush();
        entityManager.clear();

        Specification<Post> specification =
                PostSpecifications.isPublished()
                        .and(PostSpecifications.hasKeyword("java"));

        Page<Post> result = postRepository.findAll(
                specification,
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals("Spring Guide", result.getContent().get(0).getTitle());
    }

    @Test
    void isPublished_shouldReturnOnlyPublishedPosts() {
        createPost("Published Post", "This post is published");

        Post draftPost = Post.builder()
                .title("Draft Post")
                .content("This post is a draft")
                .author(author)
                .category(category)
                .status(PostStatus.DRAFT)
                .readingTime(2)
                .build();

        entityManager.persist(draftPost);

        entityManager.flush();
        entityManager.clear();

        Page<Post> result = postRepository.findAll(
                PostSpecifications.isPublished(),
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals("Published Post", result.getContent().get(0).getTitle());
    }

    @Test
    void hasCategory_shouldReturnPostsInSpecifiedCategory() {
        createPost("Spring Guide", "Learn Spring Boot");

        Category anotherCategory = Category.builder()
                .name("Food")
                .build();

        entityManager.persist(anotherCategory);

        Post foodPost = Post.builder()
                .title("Cooking Guide")
                .content("Learn cooking")
                .author(author)
                .category(anotherCategory)
                .status(PostStatus.PUBLISHED)
                .readingTime(3)
                .build();

        entityManager.persist(foodPost);

        entityManager.flush();
        entityManager.clear();

        Specification<Post> specification =
                PostSpecifications.isPublished()
                        .and(PostSpecifications.hasCategory(category.getId()));

        Page<Post> result = postRepository.findAll(
                specification,
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals("Spring Guide", result.getContent().get(0).getTitle());
    }

    @Test
    void hasTag_shouldReturnPostsWithSpecifiedTag() {
        Tag javaTag = Tag.builder()
                .name("Java")
                .build();

        Tag springTag = Tag.builder()
                .name("Spring")
                .build();

        entityManager.persist(javaTag);
        entityManager.persist(springTag);

        Post javaPost = Post.builder()
                .title("Java Guide")
                .content("Learn Java")
                .author(author)
                .category(category)
                .status(PostStatus.PUBLISHED)
                .readingTime(2)
                .tags(Set.of(javaTag))
                .build();

        Post springPost = Post.builder()
                .title("Spring Guide")
                .content("Learn Spring")
                .author(author)
                .category(category)
                .status(PostStatus.PUBLISHED)
                .readingTime(3)
                .tags(Set.of(springTag))
                .build();

        entityManager.persist(javaPost);
        entityManager.persist(springPost);

        entityManager.flush();
        entityManager.clear();

        Specification<Post> specification =
                PostSpecifications.isPublished()
                        .and(PostSpecifications.hasTag(javaTag.getId()));

        Page<Post> result = postRepository.findAll(
                specification,
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals("Java Guide", result.getContent().get(0).getTitle());
    }

    private Post createPost(String title, String content) {
        Post post = Post.builder()
                .title(title)
                .content(content)
                .author(author)
                .category(category)
                .status(PostStatus.PUBLISHED)
                .readingTime(2)
                .build();

        entityManager.persist(post);
        return post;
    }
}
