package torquehub.torquehub.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.business.interfaces.QuestionService;
import torquehub.torquehub.configuration.JWT.token.AccessToken;
import torquehub.torquehub.configuration.JWT.token.AccessTokenDecoder;
import torquehub.torquehub.configuration.JWT.token.exeption.InvalidAccessTokenException;
import torquehub.torquehub.domain.model.User;
import torquehub.torquehub.domain.request.QuestionDtos.QuestionCreateRequest;
import torquehub.torquehub.domain.request.QuestionDtos.QuestionUpdateRequest;
import torquehub.torquehub.domain.response.MessageResponse;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionDetailResponse;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionResponse;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionSummaryResponse;
import torquehub.torquehub.domain.response.ReputationDtos.ReputationResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaUserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/questions")
@Validated
public class QuestionController {

    private final QuestionService questionService;
    private final JpaUserRepository userRepository;
    private final AccessTokenDecoder accessTokenDecoder;

    public QuestionController(QuestionService questionService, JpaUserRepository userRepository, AccessTokenDecoder accessTokenDecoder) {
        this.questionService = questionService;
        this.userRepository = userRepository;
        this.accessTokenDecoder = accessTokenDecoder;
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
            @RequestParam(defaultValue = "10") int size
    ) {
        questionService.incrementQuestionView(id);
        Pageable pageable = PageRequest.of(page, size);
        Optional<QuestionDetailResponse> question = questionService.getQuestionbyId(id, pageable);
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
    public ResponseEntity<ReputationResponse> upvoteQuestion(@PathVariable Long questionId, @RequestHeader("Authorization") String token) {
        // Validate the token
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        try {
            // Decode the JWT token
            AccessToken accessToken = accessTokenDecoder.decode(token.replace("Bearer ", ""));

            // Extract user ID from the token
            Long userId = accessToken.getUserID();

            // Call the service to handle the upvote logic
            ReputationResponse reputationResponse = questionService.upvoteQuestion(questionId, userId);

            // Return the updated reputation information
            return ResponseEntity.ok(reputationResponse);

        } catch (InvalidAccessTokenException e) {
            // Handle invalid or expired token
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @PostMapping("/{questionId}/downvote")
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
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
