package torquehub.torquehub.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.business.interfaces.TagService;
import torquehub.torquehub.domain.mapper.TagMapper;
import torquehub.torquehub.domain.request.TagDtos.TagCreateRequest;
import torquehub.torquehub.domain.request.TagDtos.TagUpdateRequest;
import torquehub.torquehub.domain.response.MessageResponse;
import torquehub.torquehub.domain.response.TagDtos.TagResponse;

import java.util.List;

@RestController
@RequestMapping("/tags")
@Validated
public class TagController {

    @Autowired
    private TagService tagService;

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
    public ResponseEntity<TagResponse> createTag(@Valid @RequestBody TagCreateRequest tagCreateRequest) {
        TagResponse createdTag = tagService.createTag(tagCreateRequest);
        return ResponseEntity.ok(createdTag);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteTag(@PathVariable Long id) {
        MessageResponse response = new MessageResponse();
        if (tagService.getTagById(id).isPresent()) {
            tagService.deleteTag(id);
            response.setMessage("Tag deleted successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.setMessage("Tag with ID " + id + " not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> updateTag(@PathVariable Long id, @Valid @RequestBody TagUpdateRequest tagUpdateRequest) {
        MessageResponse response = new MessageResponse();
        if (tagService.getTagById(id).isPresent()) {
            tagService.updateTagById(id, tagUpdateRequest);
            response.setMessage("Tag updated successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.setMessage("Tag with ID " + id + " not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
