package com.hanqiu.blog.services;

import com.hanqiu.blog.domain.entities.Tag;
import com.hanqiu.blog.repositories.TagRepository;
import com.hanqiu.blog.services.impl.TagServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TagServiceImplTest {
    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagServiceImpl tagService;

    private Tag tag1;
    private Tag tag2;
    private List<Tag> tags;
    private Set<String> tagNames;

    @BeforeEach
    void setup() {
        tag1 = Tag.builder()
                .id(UUID.randomUUID())
                .name("tag1")
                .posts(new HashSet<>())
                .build();

        tag2 = Tag.builder()
                .id(UUID.randomUUID())
                .name("tag2")
                .posts(new HashSet<>())
                .build();

        tags = List.of(tag1, tag2);
    }

    @Test
    void createTags() {
        Set<String> tagNames = Set.of("tag1", "tag2", "tag3");

        when(tagRepository.findByNameIn(tagNames))
                .thenReturn(tags);

        Tag newTag = Tag.builder()
                .id(UUID.randomUUID())
                .name("tag3")
                .build();

        when(tagRepository.saveAll(anyList()))
                .thenReturn(new ArrayList<>(List.of(newTag)));
        List<Tag> result = tagService.createTags(tagNames);

        assertEquals(3, result.size());
        assertTrue(result.contains(tag1));
        assertTrue(result.contains(tag2));
        assertTrue(result.contains(newTag));
    }

    @Test
    void getTagById() {
        when(tagRepository.findById(tag1.getId()))
                .thenReturn(Optional.ofNullable(tag1));

        Tag result = tagService.getTagById(tag1.getId());

        assertEquals(tag1, result);
    }

    @Test
    void getTagByIds() {
        Set<UUID> ids = Set.of(tag1.getId(), tag2.getId());
        when(tagRepository.findAllById(ids))
                .thenReturn(tags);

        List<Tag> result = tagService.getTagByIds(ids);

        assertEquals(tags, result);
    }

    @Test
    void deleteTag() {
        when(tagRepository.findById(tag1.getId()))
                .thenReturn(Optional.of(tag1));
        tagService.deleteTag(tag1.getId());

        verify(tagRepository).deleteById(tag1.getId());
    }
}
