package com.hanqiu.blog.controller;

import com.hanqiu.blog.domain.dtos.CreateTagsRequest;
import com.hanqiu.blog.domain.dtos.TagDto;
import com.hanqiu.blog.domain.entities.Tag;
import com.hanqiu.blog.mappers.TagMapper;
import com.hanqiu.blog.services.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TagController.class)
public class TagControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TagService tagService;

    @MockitoBean
    private TagMapper tagMapper;


    private Tag tag1;
    private Tag tag2;
    private TagDto tagDto1;
    private TagDto tagDto2;


    @BeforeEach
    void setup() {
        tag1 = Tag.builder()
                .id(UUID.randomUUID()).name("Test One").build();

        tag2 = Tag.builder()
                .id(UUID.randomUUID()).name("Test Two").build();

        tagDto1 = TagDto.builder()
                .id(tag1.getId()).name("Test One").build();

        tagDto2 = TagDto.builder()
                .id(tag2.getId()).name("Test Two").build();


    }

    @Test
    void getAllTags_shouldReturn200() throws Exception {
        when(tagService.getTags())
                .thenReturn(List.of(tag1, tag2));
        when(tagMapper.toTagResponse(tag1))
                .thenReturn(tagDto1);
        when(tagMapper.toTagResponse(tag2))
                .thenReturn(tagDto2);

        mockMvc.perform(get("/api/v1/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test One"))
                .andExpect(jsonPath("$[1].name").value("Test Two"));
    }

    @Test
    void createTagsRequest_shouldReturn201() throws Exception {
        CreateTagsRequest createTagsRequest = CreateTagsRequest.builder()
                        .names(Set.of("Test One", "Test Two")).build();
        when(tagService.createTags(createTagsRequest.getNames()))
                .thenReturn(List.of(tag1, tag2));
        when(tagMapper.toTagResponse(tag1))
                .thenReturn(tagDto1);
        when(tagMapper.toTagResponse(tag2))
                .thenReturn(tagDto2);

        mockMvc.perform(post("/api/v1/tags") .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTagsRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].name").value("Test One"))
                .andExpect(jsonPath("$[1].name").value("Test Two"));

    }

    @Test
    void createTags_shouldReturn400_whenNamesEmpty() throws Exception {
        CreateTagsRequest createTagsRequest = CreateTagsRequest.builder()
                .names(Set.of())
                .build();

        mockMvc.perform(post("/api/v1/tags") .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTagsRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTag_shouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/tags/{id}", id))
                        .andExpect(status().isNoContent());
         verify(tagService).deleteTag(id);
    }
}
