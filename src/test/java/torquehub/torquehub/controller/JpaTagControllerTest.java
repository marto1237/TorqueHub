package torquehub.torquehub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import torquehub.torquehub.business.exeption.tag_exeptions.DuplicateTagException;
import torquehub.torquehub.business.interfaces.TagService;
import torquehub.torquehub.configuration.SecurityConfig;
import torquehub.torquehub.configuration.jwt.token.AccessToken;
import torquehub.torquehub.configuration.jwt.token.AccessTokenDecoder;
import torquehub.torquehub.configuration.jwt.token.exeption.InvalidAccessTokenException;
import torquehub.torquehub.configuration.jwt.token.impl.AccessTokenEncoderDecoderImpl;
import torquehub.torquehub.configuration.jwt.token.impl.BlacklistService;
import torquehub.torquehub.configuration.utils.TokenUtil;
import torquehub.torquehub.controllers.TagController;
import torquehub.torquehub.domain.request.tag_dtos.TagCreateRequest;
import torquehub.torquehub.domain.request.tag_dtos.TagUpdateRequest;
import torquehub.torquehub.domain.response.tag_dtos.TagResponse;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TagController.class)
@Import({SecurityConfig.class, AccessTokenEncoderDecoderImpl.class, BlacklistService.class})
class JpaTagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TagService tagService;

    @Autowired
    private ObjectMapper objectMapper;

    private TagResponse tagResponse;

    @MockBean
    private AccessTokenDecoder accessTokenDecoder;

    @MockBean
    private BlacklistService blacklistService;

    @MockBean
    private TokenUtil tokenUtil;

    private static final String VALID_TOKEN = "Bearer valid-token";
    private static final String INVALID_TOKEN = "Bearer invalid-token";

    @BeforeEach
    void setUp() {
        tagResponse = TagResponse.builder().id(1L).name("BMW").build();

        // Mock the AccessToken for authorization
        AccessToken mockAccessToken = mock(AccessToken.class);
        given(mockAccessToken.getUsername()).willReturn("user");
        given(mockAccessToken.getRole()).willReturn("MODERATOR");

        // Return a valid token when a valid token string is provided
        given(accessTokenDecoder.decode("valid-token")).willReturn(mockAccessToken);

        // Throw exception for invalid token
        given(accessTokenDecoder.decode("invalid-token")).willThrow(new InvalidAccessTokenException("Invalid token"));

        // Mock user ID from token util
        given(tokenUtil.getUserIdFromToken(VALID_TOKEN)).willReturn(1L);
    }

    @Test
    @WithMockUser(authorities = {"MODERATOR"})
    void shouldReturnAllTagsSuccessfully() throws Exception {
        List<TagResponse> tagList = List.of(tagResponse, new TagResponse(2L, "Mercedes"));
        given(tagService.getAllTags()).willReturn(tagList);

        mockMvc.perform(get("/tags"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(tagList.size()))
                .andExpect(jsonPath("$[0].name").value(tagList.get(0).getName()))
                .andExpect(jsonPath("$[1].name").value(tagList.get(1).getName()));
    }

    @Test
    @WithMockUser(authorities = {"MODERATOR"})
    void shouldReturnEmptyList_whenNoTagsExist() throws Exception {
        given(tagService.getAllTags()).willReturn(List.of());

        mockMvc.perform(get("/tags"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    @WithMockUser(authorities = {"MODERATOR"})
    void shouldGetTagByIdSuccessfully() throws Exception {
        given(tagService.getTagById(1L)).willReturn(Optional.of(tagResponse));

        mockMvc.perform(get("/tags/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(tagResponse.getName()));
    }

    @Test
    @WithMockUser(authorities = {"MODERATOR"})
    void shouldReturnNotFound_whenTagDoesNotExist() throws Exception {
        given(tagService.getTagById(1L)).willReturn(Optional.empty());

        mockMvc.perform(get("/tags/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"MODERATOR"})
    void shouldCreateTagSuccessfully() throws Exception {
        TagCreateRequest tagCreateRequest = TagCreateRequest.builder().name("BMW").build();
        given(tagService.createTag(tagCreateRequest)).willReturn(tagResponse);

        mockMvc.perform(post("/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", VALID_TOKEN)
                        .content(objectMapper.writeValueAsString(tagCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(tagResponse.getName()));
    }

    @Test
    @WithMockUser(authorities = {"MODERATOR"})
    void shouldReturnBadRequestWhenCreateTagValidationFails() throws Exception {
        TagCreateRequest invalidRequest = new TagCreateRequest(); // Missing required fields

        mockMvc.perform(post("/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", VALID_TOKEN)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"MODERATOR"})
    void shouldReturnInternalServerError_whenDuplicateTagIsCreated() throws Exception {
        TagCreateRequest tagCreateRequest = TagCreateRequest.builder().name("BMW").build();
        given(tagService.createTag(tagCreateRequest)).willThrow(new DuplicateTagException("Duplicate Tag"));

        mockMvc.perform(post("/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", VALID_TOKEN)
                        .content(objectMapper.writeValueAsString(tagCreateRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(authorities = {"MODERATOR"})
    void shouldDeleteTagSuccessfully() throws Exception {
        given(tagService.getTagById(1L)).willReturn(Optional.of(tagResponse));

        mockMvc.perform(delete("/tags/1")
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tag deleted successfully."));
    }

    @Test
    @WithMockUser(authorities = {"MODERATOR"})
    void shouldReturnNotFound_whenTagDoesNotExistForDeletion() throws Exception {
        given(tagService.getTagById(1L)).willReturn(Optional.empty());

        mockMvc.perform(delete("/tags/1")
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Tag with ID 1 not found."));
    }

    @Test
    @WithMockUser(authorities = {"MODERATOR"})
    void shouldReturnUnauthorized_whenDeleteTagWithInvalidToken() throws Exception {
        mockMvc.perform(delete("/tags/1")
                        .header("Authorization", INVALID_TOKEN))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {"MODERATOR"})
    void shouldUpdateTagSuccessfully() throws Exception {
        TagUpdateRequest tagUpdateRequest = TagUpdateRequest.builder()
                .id(1L)
                .name("UpdatedTag")
                .build();
        given(tagService.getTagById(1L)).willReturn(Optional.of(tagResponse));

        mockMvc.perform(put("/tags/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", VALID_TOKEN)
                        .content(objectMapper.writeValueAsString(tagUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tag updated successfully."));
    }

    @Test
    @WithMockUser(authorities = {"MODERATOR"})
    void shouldReturnNotFound_whenTagDoesNotExistForUpdate() throws Exception {
        TagUpdateRequest tagUpdateRequest = TagUpdateRequest.builder()
                .id(1L)
                .name("UpdatedTag")
                .build();
        given(tagService.getTagById(1L)).willReturn(Optional.empty());

        mockMvc.perform(put("/tags/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", VALID_TOKEN)
                        .content(objectMapper.writeValueAsString(tagUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Tag with ID 1 not found."));
    }

    @Test
    @WithMockUser(authorities = {"MODERATOR"})
    void shouldReturnBadRequest_whenUpdateTagWithInvalidData() throws Exception {
        TagUpdateRequest invalidRequest = TagUpdateRequest.builder().name("").build(); // Missing required ID field

        mockMvc.perform(put("/tags/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", VALID_TOKEN)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"MODERATOR"})
    void shouldReturnUnauthorized_whenUpdateTagWithInvalidToken() throws Exception {
        TagUpdateRequest tagUpdateRequest = TagUpdateRequest.builder()
                .id(1L)
                .name("UpdatedTag")
                .build();

        mockMvc.perform(put("/tags/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", INVALID_TOKEN)
                        .content(objectMapper.writeValueAsString(tagUpdateRequest)))
                .andExpect(status().isUnauthorized());
    }
}
