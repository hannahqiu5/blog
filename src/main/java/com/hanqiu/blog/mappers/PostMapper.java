package com.hanqiu.blog.mappers;

import com.hanqiu.blog.domain.CreatePostRequest;
import com.hanqiu.blog.domain.UpdatePostRequest;
import com.hanqiu.blog.domain.dtos.CreatePostRequestDto;
import com.hanqiu.blog.domain.dtos.PostDto;
import com.hanqiu.blog.domain.dtos.UpdatePostRequestDto;
import com.hanqiu.blog.domain.entities.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface PostMapper {

    @Mapping(target = "author", source = "author")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "tags", source = "tags")
    PostDto toDto(Post post);

    CreatePostRequest toCreatePostRequest(CreatePostRequestDto dto);

    UpdatePostRequest toUpdatePostRequest(UpdatePostRequestDto dto);
}
