package torquehub.torquehub.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.business.interfaces.AnswerService;
import torquehub.torquehub.domain.request.AnswerDtos.AnswerCreateRequest;
import torquehub.torquehub.domain.request.AnswerDtos.AnswerEditRequest;
import torquehub.torquehub.domain.response.AnswerDtos.AnswerResponse;
import torquehub.torquehub.domain.response.MessageResponse;
import torquehub.torquehub.domain.response.ReputationDtos.ReputationResponse;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/answers")
@Validated
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    @PostMapping
    public ResponseEntity<AnswerResponse> createAnswer(@RequestBody @Validated AnswerCreateRequest answerCreateRequest) {
        AnswerResponse answerResponse = answerService.addAnswer(answerCreateRequest);
        return ResponseEntity.ok(answerResponse);
    }

    @PostMapping("/{answerId}/upvote")
    public ResponseEntity<ReputationResponse> upvoteAnswer(@PathVariable Long answerId, @RequestParam Long userId ) {
        return ResponseEntity.ok(answerService.upvoteAnswer(answerId, userId));
    }

    @PostMapping("/{answerId}/downvote")
    public ResponseEntity<ReputationResponse> downvoteAnswer(@PathVariable Long answerId, @RequestParam Long userId) {
        return ResponseEntity.ok(answerService.downvoteAnswer(answerId, userId));
    }

    @PostMapping("/{answerId}/bestAnswer")
    public ResponseEntity<ReputationResponse> approveBestAnswer(@PathVariable Long answerId, @RequestParam Long questionId, @RequestParam Long userId) {
        answerService.approveBestAnswer(questionId, answerId, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{answerId}")
    public ResponseEntity<AnswerResponse> editAnswer(@PathVariable Long answerId, @RequestBody @Validated AnswerEditRequest answerEditRequest) {
        AnswerResponse answerResponse = answerService.editAnswer(answerId, answerEditRequest);
        return ResponseEntity.ok(answerResponse);
    }

    @GetMapping("/questions/{questionId}")
    public ResponseEntity<List<AnswerResponse>> getAnswersByQuestion(@PathVariable Long questionId) {
        Optional<List<AnswerResponse>> answerResponses = answerService.getAnswersByQuestion(questionId);
        return answerResponses.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
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
