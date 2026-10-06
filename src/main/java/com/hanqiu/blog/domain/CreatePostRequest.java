package com.hanqiu.blog.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {

    private String title;


    private String content;

    @NotNull(message = "Category ID is required!")
    private UUID categoryId;


    @Builder.Default
    private Set<UUID> tagIds = new HashSet<>();

    private PostStatus status;
}
