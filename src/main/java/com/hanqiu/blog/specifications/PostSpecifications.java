package com.hanqiu.blog.specifications;

import com.hanqiu.blog.domain.entities.Post;
import com.hanqiu.blog.domain.enums.PostStatus;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class PostSpecifications {


    public static Specification<Post> isPublished() {
        return ((root, query, criteriaBuilder) ->
        {
            return criteriaBuilder.equal(root.get("status"), PostStatus.PUBLISHED);
        });
    }

    public static Specification<Post> hasCategory(UUID categoryId) {
        return ((root, query, criteriaBuilder) ->
        {
            return criteriaBuilder.equal(root.get("category").get("id"), categoryId);
        });
    }

    public static Specification<Post> hasTag(UUID tagId) {
        return ((root, query, criteriaBuilder) ->
        {
            return criteriaBuilder.equal(root.join("tags").get("id"), tagId);
        });
    }

    public static Specification<Post> hasKeyword(String keyword) {
        return ((root, query, criteriaBuilder) ->
        {

            String pattern = "%" + keyword.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("title")), pattern
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("content")), pattern
                    )
            );
        });
    }
}
