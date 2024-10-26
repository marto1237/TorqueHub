package torquehub.torquehub.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.business.interfaces.TagService;
import torquehub.torquehub.configuration.jwt.token.exeption.InvalidAccessTokenException;
import torquehub.torquehub.configuration.utils.TokenUtil;
import torquehub.torquehub.domain.request.tag_dtos.TagCreateRequest;
import torquehub.torquehub.domain.request.tag_dtos.TagUpdateRequest;
import torquehub.torquehub.domain.response.MessageResponse;
import torquehub.torquehub.domain.response.tag_dtos.TagResponse;

import java.util.List;

@RestController
@RequestMapping("/tags")
@Validated
public class TagController {

    private final TagService tagService;
    private final TokenUtil tokenUtil;

    public TagController(TagService tagService,
                         TokenUtil tokenUtil) {
        this.tagService = tagService;
        this.tokenUtil = tokenUtil;
    }

    @GetMapping
    public List<TagResponse> getAllTags() {
        return tagService.getAllTags();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> getTagById(@PathVariable Long id) {
        return tagService.getTagById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MODERATOR') or hasAuthority('ADMIN')")
    public ResponseEntity<TagResponse> createTag(
            @Valid @RequestBody TagCreateRequest tagCreateRequest,
            @RequestHeader("Authorization") String token) {

        try{
            Long userId = tokenUtil.getUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            TagResponse createdTag = tagService.createTag(tagCreateRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTag);
        } catch (InvalidAccessTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MODERATOR') or hasAuthority('ADMIN')")
    public ResponseEntity<MessageResponse> deleteTag(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        MessageResponse response = new MessageResponse();

        try {
            Long userId = tokenUtil.getUserIdFromToken(token);
            if (userId == null) {
                response.setMessage("Unauthorized: Invalid token.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if (tagService.getTagById(id).isPresent()) {
                tagService.deleteTag(id);
                response.setMessage("Tag deleted successfully.");
                return ResponseEntity.ok(response);
            } else {
                response.setMessage("Tag with ID " + id + " not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (InvalidAccessTokenException e) {
            response.setMessage("Invalid token.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.setMessage("An unexpected error occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MODERATOR') or hasAuthority('ADMIN')")
    public ResponseEntity<MessageResponse> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody TagUpdateRequest tagUpdateRequest,
            @RequestHeader("Authorization") String token) {
        MessageResponse response = new MessageResponse();

        try {
            Long userId = tokenUtil.getUserIdFromToken(token);
            if (userId == null) {
                response.setMessage("Unauthorized: Invalid token.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if (tagService.getTagById(id).isPresent()) {
                tagService.updateTagById(id, tagUpdateRequest);
                response.setMessage("Tag updated successfully.");
                return ResponseEntity.ok(response);
            } else {
                response.setMessage("Tag with ID " + id + " not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (InvalidAccessTokenException e) {
            response.setMessage("Invalid token.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (IllegalArgumentException e) {
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.setMessage("An unexpected error occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
