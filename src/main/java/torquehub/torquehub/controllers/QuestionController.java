package torquehub.torquehub.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.business.interfaces.QuestionService;
import torquehub.torquehub.business.interfaces.RoleService;
import torquehub.torquehub.domain.request.QuestionDtos.QuestionCreateRequest;
import torquehub.torquehub.domain.request.QuestionDtos.QuestionUpdateRequest;
import torquehub.torquehub.domain.request.RoleDtos.RoleCreateRequest;
import torquehub.torquehub.domain.request.RoleDtos.RoleUpdateRequest;
import torquehub.torquehub.domain.request.UserDtos.UserUpdateRequest;
import torquehub.torquehub.domain.response.MessageResponse;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionResponse;
import torquehub.torquehub.domain.response.RoleDtos.RoleResponse;
import torquehub.torquehub.domain.response.UserDtos.UserResponse;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/questions")
@Validated
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @GetMapping
    public List<QuestionResponse> getAllQuestions() {
        return questionService.getAllQuestions();
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponse> getQuestionById(@PathVariable Long id) {
        Optional<QuestionResponse> question = questionService.getQuestionbyId(id);
        return question.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<QuestionResponse>> getQuestionsByUser(@PathVariable Long userId) {
        Optional<List<QuestionResponse>> questions = questionService.getQuestionsByUser(userId);
        return questions.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> askQuestion(@Valid @RequestBody QuestionCreateRequest questionCreateRequest) {
        QuestionResponse createdQuestion = questionService.askQuestion(questionCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestion);
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
        Optional<QuestionResponse> question = questionService.getQuestionbyId(id);
        if (question.isPresent()) {
            questionService.deleteQuestion(id);
            response.setMessage("Question deleted successfully.");
        } else {
            response.setMessage("Question with ID " + id + " not found.");
        }
        return ResponseEntity.ok(response);
    }


}
