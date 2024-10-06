package torquehub.torquehub.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.business.interfaces.QuestionService;
import torquehub.torquehub.domain.request.QuestionDtos.QuestionCreateRequest;
import torquehub.torquehub.domain.request.QuestionDtos.QuestionUpdateRequest;
import torquehub.torquehub.domain.response.MessageResponse;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionDetailResponse;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionResponse;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionSummaryResponse;
import torquehub.torquehub.domain.response.ReputationDtos.ReputationResponse;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/questions")
@Validated
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @GetMapping
    public ResponseEntity<Page<QuestionSummaryResponse>> getAllQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionSummaryResponse> questions = questionService.getAllQuestions(pageable);
        return ResponseEntity.ok(questions);
    }
    @GetMapping("/tags")
    public ResponseEntity<Page<QuestionSummaryResponse>> getQuestionsByTags(
            @RequestParam Set<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionSummaryResponse> questions = questionService.getQuestionsByTags(tags, pageable);
        return ResponseEntity.ok(questions);
    }


    @GetMapping("/{id}")
    public ResponseEntity<QuestionDetailResponse> getQuestionById(@PathVariable Long id) {
        Optional<QuestionDetailResponse> question = questionService.getQuestionbyId(id);
        return question.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<QuestionSummaryResponse>> getQuestionsByUser(@PathVariable Long userId) {
        Optional<List<QuestionSummaryResponse>> questions = questionService.getQuestionsByUser(userId);
        return questions.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> askQuestion(@Valid @RequestBody QuestionCreateRequest questionCreateRequest) {
        QuestionResponse createdQuestion = questionService.askQuestion(questionCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestion);
    }

    @PostMapping("/{questionId}/upvote")
    public ResponseEntity<ReputationResponse> upvoteAnswer(@PathVariable Long questionId, @RequestParam Long userId) {
        return ResponseEntity.ok(questionService.upvoteQuestion(questionId, userId));
    }

    @PostMapping("/{questionId}/downvote")
    public ResponseEntity<ReputationResponse> downvoteAnswer(@PathVariable Long questionId, @RequestParam Long userId) {
        return ResponseEntity.ok(questionService.downvoteQuestion(questionId, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionUpdateRequest updateDto) {
        MessageResponse response = new MessageResponse();
        if (questionService.updateQuestion(id, updateDto)) {
            response.setMessage("Question updated successfully.");
        } else {
            response.setMessage("Question with ID " + id + " not found.");
        }
        return  ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteQuestion(@PathVariable Long id) {
        MessageResponse response = new MessageResponse();
        boolean deleted = questionService.deleteQuestion(id);
        if (deleted) {
            response.setMessage("Question deleted successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.setMessage("Question with ID " + id + " not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }


}
