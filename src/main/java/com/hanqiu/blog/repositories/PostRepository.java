package com.hanqiu.blog.repositories;

import com.hanqiu.blog.domain.entities.Category;
import com.hanqiu.blog.domain.entities.Post;
import com.hanqiu.blog.domain.entities.Tag;
import com.hanqiu.blog.domain.entities.User;
import com.hanqiu.blog.domain.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    Page<Post> findAllByStatusAndCategoryAndTagsContaining(PostStatus status, Category category, Tag tag, Pageable pageable);

    Page<Post> findAllByStatusAndCategory(PostStatus status, Category category, Pageable pageable);

    Page<Post> findAllByStatusAndTagsContaining(PostStatus status, Tag tag, Pageable pageable);

    Page<Post> findAllByStatus(PostStatus status, Pageable pageable);

    List<Post> findAllByAuthorAndStatus(User user, PostStatus postStatus);
}
