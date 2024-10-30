package torquehub.torquehub.controllers;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.business.interfaces.QuestionService;
import torquehub.torquehub.configuration.jwt.token.exeption.InvalidAccessTokenException;
import torquehub.torquehub.configuration.utils.TokenUtil;
import torquehub.torquehub.domain.request.question_dtos.QuestionCreateRequest;
import torquehub.torquehub.domain.request.question_dtos.QuestionUpdateRequest;
import torquehub.torquehub.domain.response.MessageResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionDetailResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionSummaryResponse;
import torquehub.torquehub.domain.response.reputation_dtos.ReputationResponse;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/questions")
@Validated
public class QuestionController {

    private final QuestionService questionService;
    private final TokenUtil tokenUtil;

    public QuestionController(QuestionService questionService,
                              TokenUtil tokenUtil) {
        this.questionService = questionService;
        this.tokenUtil = tokenUtil;
    }

    @GetMapping
    public ResponseEntity<Page<QuestionSummaryResponse>> getAllQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionSummaryResponse> questions = questionService.getAllQuestions(pageable);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionDetailResponse> getQuestionById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "Authorization", required = false) String token // Authorization is optional
    ) {
        Long userId = null; // Default to null for non-logged-in users
        if (token != null && !token.isEmpty()) {
            try {
                userId = tokenUtil.getUserIdFromToken(token); // Extract userId from token
            } catch (InvalidAccessTokenException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
        }

        questionService.incrementQuestionView(id);
        Pageable pageable = PageRequest.of(page, size);
        Optional<QuestionDetailResponse> question = questionService.getQuestionbyId(id, pageable, userId); // Pass userId (can be null)
        return question.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }



    @GetMapping("/user/{userId}")
    public ResponseEntity<List<QuestionSummaryResponse>> getQuestionsByUser(@PathVariable Long userId) {
        Optional<List<QuestionSummaryResponse>> questions = questionService.getQuestionsByUser(userId);
        return questions.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuestionResponse> askQuestion(@Valid @RequestBody QuestionCreateRequest questionCreateRequest) {
        QuestionResponse createdQuestion = questionService.askQuestion(questionCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestion);
    }



    @PostMapping("/{questionId}/upvote")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReputationResponse> upvoteQuestion(@PathVariable Long questionId, @RequestHeader("Authorization") String token) {
        try {
            Long userId = tokenUtil.getUserIdFromToken(token);
            ReputationResponse reputationResponse = questionService.upvoteQuestion(questionId, userId);
            return ResponseEntity.ok(reputationResponse);
        } catch (InvalidAccessTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/{questionId}/downvote")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReputationResponse> downvoteAnswer(@PathVariable Long questionId, @RequestHeader("Authorization") String token) {
        try {
            Long userId = tokenUtil.getUserIdFromToken(token);
            ReputationResponse reputationResponse = questionService.downvoteQuestion(questionId, userId);
            return ResponseEntity.ok(reputationResponse);
        } catch (InvalidAccessTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MODERATOR')")
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
