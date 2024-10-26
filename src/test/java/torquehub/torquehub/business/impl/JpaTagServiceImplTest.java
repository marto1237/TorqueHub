package torquehub.torquehub.business.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import torquehub.torquehub.business.exeption.tag_exeptions.TagDeleteExeption;
import torquehub.torquehub.business.exeption.tag_exeptions.TagNotFoundException;
import torquehub.torquehub.business.exeption.tag_exeptions.TageCreationExeption;
import torquehub.torquehub.domain.mapper.TagMapper;
import torquehub.torquehub.domain.model.jpa_models.JpaTag;
import torquehub.torquehub.domain.request.tag_dtos.TagCreateRequest;
import torquehub.torquehub.domain.request.tag_dtos.TagUpdateRequest;
import torquehub.torquehub.domain.response.tag_dtos.TagResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaTagRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaTagServiceImplTest {

    @InjectMocks
    private TagServiceImpl tagService;

    @Mock
    private JpaTagRepository tagRepository;

    @Mock
    private TagMapper tagMapper;

    private JpaTag testJpaTag;
    private TagCreateRequest tagCreateRequest;
    private TagUpdateRequest tagUpdateRequest;
    private TagResponse tagResponse;
    private final Long tagId = 1L;

    @BeforeEach
    void setUp() {
        testJpaTag = JpaTag.builder().id(tagId).name("Technology").build();

        tagCreateRequest = TagCreateRequest.builder()
                .name("Technology")
                .build();

        tagUpdateRequest = TagUpdateRequest.builder()
                .name("Science")
                .build();

        tagResponse = TagResponse.builder()
                .id(tagId)
                .name("Technology")
                .build();
    }

    // Test for getting all tags - Positive case
    @Test
    void shouldGetAllTagsSuccessfully() {
        when(tagRepository.findAll()).thenReturn(List.of(testJpaTag));
        when(tagMapper.toResponse(testJpaTag)).thenReturn(tagResponse);

        List<TagResponse> response = tagService.getAllTags();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Technology", response.get(0).getName());
        verify(tagRepository, times(1)).findAll();
        verify(tagMapper, times(1)).toResponse(testJpaTag);
    }

    // Test for creating a new tag successfully
    @Test
    void shouldCreateTagSuccessfully() {
        when(tagRepository.findByName("Technology")).thenReturn(Optional.empty());
        when(tagRepository.save(any(JpaTag.class))).thenReturn(testJpaTag);
        when(tagMapper.toResponse(testJpaTag)).thenReturn(tagResponse);

        TagResponse response = tagService.createTag(tagCreateRequest);

        assertNotNull(response);
        assertEquals("Technology", response.getName());
        assertEquals(tagId, response.getId());
        verify(tagRepository, times(1)).findByName("Technology");
        verify(tagRepository, times(1)).save(any(JpaTag.class));
    }

    // Test for creating a duplicate tag - Negative case
    @Test
    void shouldThrowExceptionWhenDuplicateTagIsCreated() {
        when(tagRepository.findByName("Technology")).thenReturn(Optional.of(testJpaTag));

        TageCreationExeption exception = assertThrows(TageCreationExeption.class, () -> {
            tagService.createTag(tagCreateRequest);
        });

        assertTrue(exception.getMessage().contains("Tag with name 'Technology' already exists."));
        verify(tagRepository, times(1)).findByName("Technology");
        verify(tagRepository, never()).save(any(JpaTag.class));
    }


    // Test for creation failure - General Exception case
    @Test
    void shouldThrowExceptionOnTagCreationFailure() {
        when(tagRepository.findByName("Technology")).thenThrow(new RuntimeException("Database error"));

        TageCreationExeption exception = assertThrows(TageCreationExeption.class, () -> {
            tagService.createTag(tagCreateRequest);
        });

        assertTrue(exception.getMessage().contains("Failed to create tag"));
        verify(tagRepository, times(1)).findByName("Technology");
    }

    // Test for retrieving a tag by ID successfully
    @Test
    void shouldGetTagByIdSuccessfully() {
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(testJpaTag));
        when(tagMapper.toResponse(testJpaTag)).thenReturn(tagResponse);

        Optional<TagResponse> response = tagService.getTagById(tagId);

        assertTrue(response.isPresent());
        assertEquals("Technology", response.get().getName());
        assertEquals(tagId, response.get().getId());
        verify(tagRepository, times(1)).findById(tagId);
        verify(tagMapper, times(1)).toResponse(testJpaTag);
    }

    // Test for retrieving a non-existent tag by ID - Negative case
    @Test
    void shouldReturnEmptyWhenTagByIdDoesNotExist() {
        when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

        Optional<TagResponse> response = tagService.getTagById(tagId);

        assertTrue(response.isEmpty());
        verify(tagRepository, times(1)).findById(tagId);
        verify(tagMapper, never()).toResponse(any(JpaTag.class));
    }

    // Test for updating a tag successfully
    @Test
    void shouldUpdateTagSuccessfully() {
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(testJpaTag));
        when(tagRepository.save(any(JpaTag.class))).thenReturn(testJpaTag);

        boolean response = tagService.updateTagById(tagId, tagUpdateRequest);

        assertTrue(response);
        verify(tagRepository, times(1)).findById(tagId);
        verify(tagRepository, times(1)).save(any(JpaTag.class));
    }

    // Test for updating a non-existent tag - Negative case
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentTag() {
        when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

        TagNotFoundException exception = assertThrows(TagNotFoundException.class, () -> {
            tagService.updateTagById(tagId, tagUpdateRequest);
        });

        assertEquals("Tag with ID 1 not found", exception.getMessage());
        verify(tagRepository, times(1)).findById(tagId);
        verify(tagRepository, never()).save(any(JpaTag.class));
    }

    // Test for deleting a tag successfully
    @Test
    void shouldDeleteTagSuccessfully() {
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(testJpaTag));

        boolean result = tagService.deleteTag(tagId);

        assertTrue(result);
        verify(tagRepository, times(1)).delete(any(JpaTag.class));
    }

    // Test for deleting a non-existent tag - Negative case
    @Test
    void shouldThrowExceptionWhenDeletingNonExistentTag() {
        when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

        TagDeleteExeption exception = assertThrows(TagDeleteExeption.class, () -> {
            tagService.deleteTag(tagId);
        });

        assertEquals("Failed to delete tag: Tag with ID 1 not found", exception.getMessage());
        verify(tagRepository, times(1)).findById(tagId);
        verify(tagRepository, never()).delete(any(JpaTag.class));
    }

    // Test for checking if a tag exists by ID - Positive case
    @Test
    void shouldReturnTrueWhenTagExistsById() {
        when(tagRepository.existsById(tagId)).thenReturn(true);

        boolean exists = tagService.tagExistsById(tagId);

        assertTrue(exists);
        verify(tagRepository, times(1)).existsById(tagId);
    }

    // Test for checking if a tag exists by ID - Negative case
    @Test
    void shouldReturnFalseWhenTagDoesNotExistById() {
        when(tagRepository.existsById(tagId)).thenReturn(false);

        boolean exists = tagService.tagExistsById(tagId);

        assertFalse(exists);
        verify(tagRepository, times(1)).existsById(tagId);
    }
}
