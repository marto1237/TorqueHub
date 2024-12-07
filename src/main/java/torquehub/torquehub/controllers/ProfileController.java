package torquehub.torquehub.controllers;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.business.interfaces.AnswerService;
import torquehub.torquehub.business.interfaces.QuestionService;
import torquehub.torquehub.business.interfaces.UserService;
import torquehub.torquehub.domain.mapper.ProfileMapper;
import torquehub.torquehub.domain.response.answer_dtos.AnswerResponse;
import torquehub.torquehub.domain.response.profile_dtos.ProfileResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionSummaryResponse;
import torquehub.torquehub.domain.response.user_dtos.UserResponse;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/profile")
@Validated
public class ProfileController {

    private final UserService userService;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final ProfileMapper profileMapper;


    public ProfileController(UserService userService,
                             QuestionService questionService, AnswerService answerService, ProfileMapper profileMapper) {
        this.userService = userService;
        this.questionService = questionService;
        this.answerService = answerService;
        this.profileMapper = profileMapper;
    }

    @Cacheable(value = "profiles", key = "#id")
    @GetMapping("/data/{id}")
    public ResponseEntity<ProfileResponse> getSiteData(@PathVariable Long id) {
        Optional<UserResponse> userOptional = userService.getUserById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        UserResponse user = userOptional.get();
        List<QuestionSummaryResponse> questions = questionService.getQuestionsByUser(id).orElse(List.of());
        List<AnswerResponse> answers = answerService.getAnswersByUser(id).orElse(List.of());
        Long totalQuestions = questionService.getQuestionCountOfUser(id);
        Long totalAnswers = answerService.getAnswerCountOfUser(id);


        ProfileResponse response = profileMapper.toProfileResponse(user, questions, answers, totalQuestions, totalAnswers);

        return ResponseEntity.ok(response);
    }
}

