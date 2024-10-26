package torquehub.torquehub.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.business.interfaces.FollowService;
import torquehub.torquehub.configuration.utils.TokenUtil;
import torquehub.torquehub.domain.request.follow_dtos.FollowAnswerRequest;
import torquehub.torquehub.domain.request.follow_dtos.FollowQuestionRequest;
import torquehub.torquehub.domain.response.follow_dtos.FollowResponse;

@RestController
@RequestMapping("/follows")
public class FollowController {

    private final FollowService followService;
    private final TokenUtil tokenUtil;

    public FollowController(FollowService followService, TokenUtil tokenUtil) {
        this.followService = followService;
        this.tokenUtil = tokenUtil;
    }

    // Follow a question
    @PostMapping("/questions/{questionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FollowResponse> toggleFollowQuestion(
            @PathVariable Long questionId,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = tokenUtil.getUserIdFromToken(token);
            FollowQuestionRequest followQuestionRequest = new FollowQuestionRequest(userId, questionId);
            FollowResponse followResponse = followService.toggleFollowQuestion(followQuestionRequest);
            return ResponseEntity.ok(followResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    // Follow an answer
    @PostMapping("/answers/{answerId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FollowResponse> followAnswer(
            @PathVariable Long answerId,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = tokenUtil.getUserIdFromToken(token);
            FollowAnswerRequest followAnswerRequest = new FollowAnswerRequest(userId, answerId);
            FollowResponse followResponse = followService.toggleFollowAnswer(followAnswerRequest);
            return ResponseEntity.ok(followResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

}
