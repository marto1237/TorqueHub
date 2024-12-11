package torquehub.torquehub.controllers;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.business.interfaces.FollowService;
import torquehub.torquehub.configuration.utils.TokenUtil;
import torquehub.torquehub.domain.request.follow_dtos.*;
import torquehub.torquehub.domain.response.MessageResponse;
import torquehub.torquehub.domain.response.follow_dtos.FollowResponse;
import torquehub.torquehub.domain.response.follow_dtos.FollowedAnswerResponse;
import torquehub.torquehub.domain.response.follow_dtos.FollowedQuestionResponse;

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
    @CacheEvict(value = {"questionDetailsByIdAndUser", "followedQuestions"}, allEntries = true)
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
    @CacheEvict(value = {"questionDetailsByIdAndUser", "followedAnswers"}, allEntries = true)
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
    @CacheEvict(value = {"followedQuestions", "followedAnswers"}, allEntries = true)
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
    @CacheEvict(value = {"followedQuestions", "followedAnswers"}, allEntries = true)
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

    @PatchMapping("/mute/{followId}")
    @PreAuthorize("isAuthenticated()")
    @CacheEvict(value = {"followedQuestions", "followedAnswers"}, allEntries = true)
    public ResponseEntity<MessageResponse> toggleMute(
            @PathVariable Long followId,
            @RequestParam boolean isMuted,
            @RequestHeader("Authorization") String token) {
        MessageResponse messageResponse = new MessageResponse();
        try {
            Long userId = tokenUtil.getUserIdFromToken(token);
            MuteFollowRequest muteFollowRequest = new MuteFollowRequest(userId, followId, isMuted);
            followService.muteFollow(muteFollowRequest);
            messageResponse.setMessage("Follow muted successfully");
            return ResponseEntity.ok(messageResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/batch-mute")
    @PreAuthorize("isAuthenticated()")
    @CacheEvict(value = {"followedQuestions", "followedAnswers"}, allEntries = true)
    public ResponseEntity<MessageResponse> batchToggleMuteFollows(
            @RequestBody List<MuteFollowRequest> muteRequests,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = tokenUtil.getUserIdFromToken(token);

            // Attach userId to all requests
            muteRequests.forEach(request -> request.setUserId(userId));

            followService.batchToggleMuteFollows(muteRequests);

            MessageResponse messageResponse = new MessageResponse();
            messageResponse.setMessage("Follows muted/unmuted successfully");
            return ResponseEntity.ok(messageResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    @GetMapping("/user/questions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<FollowedQuestionResponse>> getQuestions(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Long userId = tokenUtil.getUserIdFromToken(token);
        PageRequest pageable = PageRequest.of(page, size);
        FollowedQuestionRequest request = new FollowedQuestionRequest(userId, pageable);
        Page<FollowedQuestionResponse> followedQuestions = followService.getQuestions(request);
        return ResponseEntity.ok(followedQuestions);
    }

    @GetMapping("/user/answers")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<FollowedAnswerResponse>> getAnswers(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Long userId = tokenUtil.getUserIdFromToken(token);
        PageRequest pageable = PageRequest.of(page, size);
        FollowedAnswerRequest request = new FollowedAnswerRequest(userId, pageable);
        Page<FollowedAnswerResponse> followedAnswers = followService.getAnswers(request);
        return ResponseEntity.ok(followedAnswers);
    }



}
