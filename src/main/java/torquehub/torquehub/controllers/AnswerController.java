package torquehub.torquehub.controllers;

import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.business.interfaces.AnswerService;
import torquehub.torquehub.configuration.jwt.token.exeption.InvalidAccessTokenException;
import torquehub.torquehub.configuration.utils.TokenUtil;
import torquehub.torquehub.controllers.websocketcontrollers.WebSocketAnswerController;
import torquehub.torquehub.domain.request.answer_dtos.AnswerCreateRequest;
import torquehub.torquehub.domain.request.answer_dtos.AnswerEditRequest;
import torquehub.torquehub.domain.response.answer_dtos.AnswerResponse;
import torquehub.torquehub.domain.response.MessageResponse;
import torquehub.torquehub.domain.response.reputation_dtos.ReputationResponse;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/answers")
@Validated
public class AnswerController {

    private final AnswerService answerService;
    private final WebSocketAnswerController webSocketAnswerController;
    private final TokenUtil tokenUtil;

    public AnswerController(AnswerService answerService,
                            WebSocketAnswerController webSocketAnswerController,
                            TokenUtil tokenUtil) {
        this.answerService = answerService;
        this.webSocketAnswerController = webSocketAnswerController;
        this.tokenUtil = tokenUtil;

    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @CacheEvict(value = {"questionDetailsByIdAndUser"}, key = "#answerCreateRequest.questionId")
    public ResponseEntity<AnswerResponse> createAnswer(
            @RequestBody @Validated AnswerCreateRequest answerCreateRequest,
            @RequestHeader("Authorization") String token) {

        try {
            Long userId = tokenUtil.getUserIdFromToken(token);
            answerCreateRequest.setUserId(userId);
            AnswerResponse createdAnswer = answerService.addAnswer(answerCreateRequest);

            webSocketAnswerController.notifyClients(answerCreateRequest.getQuestionId(), createdAnswer);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAnswer);
        } catch (InvalidAccessTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }


    @PostMapping("/{answerId}/upvote")
    @PreAuthorize("isAuthenticated()")
    @CacheEvict(value = "questionDetailsByIdAndUser", allEntries = true)
    public ResponseEntity<ReputationResponse> upvoteAnswer(@PathVariable Long answerId,
                                                           @RequestHeader("Authorization") String token) {
        try {
            Long userId = tokenUtil.getUserIdFromToken(token);
            ReputationResponse reputationResponse = answerService.upvoteAnswer(answerId, userId);
            return ResponseEntity.ok(reputationResponse);
        } catch (InvalidAccessTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @PostMapping("/{answerId}/downvote")
    @PreAuthorize("isAuthenticated()")
    @CacheEvict(value = "questionDetailsByIdAndUser", allEntries = true)
    public ResponseEntity<ReputationResponse> downvoteAnswer(@PathVariable Long answerId,
                                                             @RequestHeader("Authorization") String token) {
        try {
            Long userId = tokenUtil.getUserIdFromToken(token);
            ReputationResponse reputationResponse = answerService.downvoteAnswer(answerId, userId);
            return ResponseEntity.ok(reputationResponse);
        } catch (InvalidAccessTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/{questionId}/{answerId}/approve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReputationResponse> approveBestAnswer(@PathVariable Long questionId,
                                                                @PathVariable Long answerId,
                                                                @RequestHeader("Authorization") String token) {
        try {
            Long userId = tokenUtil.getUserIdFromToken(token);
            ReputationResponse reputationResponse = answerService.approveBestAnswer(questionId, answerId, userId);
            return ResponseEntity.ok(reputationResponse);
        } catch (InvalidAccessTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @PutMapping("/{answerId}")
    @PreAuthorize("@answerService.isAnswerOwner(#answerId, authentication.name) or hasAuthority('ADMIN')")
    @CacheEvict(value = "questionDetailsByIdAndUser", allEntries = true)
    public ResponseEntity<AnswerResponse> editAnswer(@PathVariable Long answerId,
                                                     @Valid @RequestBody AnswerEditRequest answerEditRequest,
                                                     @RequestHeader("Authorization") String token) {
        try {
            Long userId = tokenUtil.getUserIdFromToken(token);
            answerEditRequest.setUserId(userId);
            AnswerResponse updatedAnswer = answerService.editAnswer(answerId, answerEditRequest);
            return ResponseEntity.ok(updatedAnswer);
        } catch (InvalidAccessTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }


    @GetMapping("/question/{questionId}")
    public ResponseEntity<Page<AnswerResponse>> getAnswersByQuestion(
            @PathVariable Long questionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "Authorization", required = false) String token) {

        Long userId = null;
        if (token != null && !token.isEmpty()) {
            try {
                userId = tokenUtil.getUserIdFromToken(token);
            } catch (InvalidAccessTokenException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
        }

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<AnswerResponse> answers = answerService.getAnswersByQuestion(questionId, pageable, userId);
            return ResponseEntity.ok(answers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AnswerResponse>> getAnswersByUser(@PathVariable Long userId) {
        Optional<List<AnswerResponse>> answerResponses = answerService.getAnswersByUser(userId);
        return answerResponses.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/byId/{answerId}")
    public ResponseEntity<AnswerResponse> getAnswerById(@PathVariable Long answerId) {
        AnswerResponse answerResponse = answerService.getAnswerById(answerId);
        return ResponseEntity.ok(answerResponse);
    }

    @DeleteMapping("/{answerId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MODERATOR')")
    @CacheEvict(value = "questionDetailsByIdAndUser", allEntries = true)
    public ResponseEntity<MessageResponse> deleteAnswer(@PathVariable Long answerId) {
        MessageResponse response = new MessageResponse();
        boolean deleted = answerService.deleteAnswer(answerId);
        if (deleted) {
            response.setMessage("Answer deleted successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.setMessage("Answer with ID " + answerId + " not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }



}
