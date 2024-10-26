package torquehub.torquehub.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.business.interfaces.AnswerService;
import torquehub.torquehub.configuration.jwt.token.AccessToken;
import torquehub.torquehub.configuration.jwt.token.AccessTokenDecoder;
import torquehub.torquehub.configuration.jwt.token.exeption.InvalidAccessTokenException;
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
    private final AccessTokenDecoder accessTokenDecoder;

    public AnswerController(AnswerService answerService,
                            WebSocketAnswerController webSocketAnswerController,
                            AccessTokenDecoder accessTokenDecoder) {
        this.answerService = answerService;
        this.webSocketAnswerController = webSocketAnswerController;
        this.accessTokenDecoder = accessTokenDecoder;

    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnswerResponse> createAnswer(
            @RequestBody @Validated AnswerCreateRequest answerCreateRequest,
            @RequestHeader("Authorization") String token) {

        // Validate the token
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        try {
            // Decode the JWT token
            AccessToken accessToken = accessTokenDecoder.decode(token.replace("Bearer ", ""));
            Long userId = accessToken.getUserID();

            // Ensure that the user ID in the request matches the user ID from the token
            if (!userId.equals(answerCreateRequest.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            // Proceed with adding the answer
            AnswerResponse answerResponse = answerService.addAnswer(answerCreateRequest);

            // Notify clients via WebSocket
            webSocketAnswerController.notifyClients(answerCreateRequest.getQuestionId(), answerResponse);

            // Return the response
            return ResponseEntity.ok(answerResponse);

        } catch (InvalidAccessTokenException e) {
            // Handle invalid or expired token
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }


    @PostMapping("/{answerId}/upvote")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReputationResponse> upvoteAnswer(@PathVariable Long answerId, @RequestParam Long userId ) {
        return ResponseEntity.ok(answerService.upvoteAnswer(answerId, userId));
    }

    @PostMapping("/{answerId}/downvote")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReputationResponse> downvoteAnswer(@PathVariable Long answerId, @RequestParam Long userId) {
        return ResponseEntity.ok(answerService.downvoteAnswer(answerId, userId));
    }

    @PostMapping("/{answerId}/bestAnswer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReputationResponse> approveBestAnswer(@PathVariable Long answerId, @RequestParam Long questionId, @RequestParam Long userId) {
        answerService.approveBestAnswer(questionId, answerId, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{answerId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnswerResponse> editAnswer(@PathVariable Long answerId, @RequestBody @Validated AnswerEditRequest answerEditRequest) {
        AnswerResponse answerResponse = answerService.editAnswer(answerId, answerEditRequest);
        return ResponseEntity.ok(answerResponse);
    }

    @GetMapping("/questions/{questionId}")
    public ResponseEntity<Page<AnswerResponse>> getAnswersByQuestion(@PathVariable Long questionId,Pageable pageable ) {
        Page<AnswerResponse> answerResponses = answerService.getAnswersByQuestion(questionId, pageable);
        return ResponseEntity.ok(answerResponses);
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<MessageResponse> deleteAnswer(@PathVariable Long answerId) {
        MessageResponse response = new MessageResponse();
        boolean deleted = answerService.deleteAnswer(answerId);
        if(deleted) {
            response.setMessage("Answer deleted successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.setMessage("Answer with ID " + answerId + " not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

    }



}
