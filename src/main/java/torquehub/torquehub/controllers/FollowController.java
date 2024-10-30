package torquehub.torquehub.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.business.interfaces.FollowService;
import torquehub.torquehub.configuration.utils.TokenUtil;
import torquehub.torquehub.domain.request.follow_dtos.FollowAnswerRequest;
import torquehub.torquehub.domain.request.follow_dtos.FollowQuestionRequest;
import torquehub.torquehub.domain.request.follow_dtos.FollowedAnswerRequest;
import torquehub.torquehub.domain.request.follow_dtos.FollowedQuestionRequest;
import torquehub.torquehub.domain.response.follow_dtos.FollowResponse;

import java.util.List;

@RestController
@RequestMapping("/follows")
public class FollowController {

    private final FollowService followService;
    private final TokenUtil tokenUtil;

    public FollowController(FollowService followService, TokenUtil tokenUtil) {
        this.followService = followService;
        this.tokenUtil = tokenUtil;
    }

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

    @GetMapping("/questions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<FollowResponse>> getFollowedQuestions(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            Long userId = tokenUtil.getUserIdFromToken(token);
            PageRequest pageable = PageRequest.of(page, size);
            FollowedQuestionRequest followedQuestionRequest = new FollowedQuestionRequest(userId, pageable);
            Page<FollowResponse> followedQuestions = followService.getFollowedQuestions(followedQuestionRequest);
            return ResponseEntity.ok(followedQuestions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @GetMapping("/answers")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<FollowResponse>> getFollowedAnswers(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            Long userId = tokenUtil.getUserIdFromToken(token);
            PageRequest pageable = PageRequest.of(page, size);
            FollowedAnswerRequest followedAnswerRequest = new FollowedAnswerRequest(userId, pageable);
            Page<FollowResponse> followedAnswers = followService.getFollowedAnswers(followedAnswerRequest);
            return ResponseEntity.ok(followedAnswers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @PostMapping("/batch-mute")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> batchMuteFollows(
            @RequestHeader("Authorization") String token,
            @RequestBody List<Long> followIds) {
        try {
            followService.batchMuteFollows(followIds);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping("/batch-unfollow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> batchUnfollow(
            @RequestHeader("Authorization") String token,
            @RequestBody List<Long> followIds) {
        try {
            followService.batchUnfollow(followIds);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}
