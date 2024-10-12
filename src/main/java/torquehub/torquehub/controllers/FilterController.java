package torquehub.torquehub.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import torquehub.torquehub.business.interfaces.FilterService;
import torquehub.torquehub.domain.model.Question;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionSummaryResponse;

import java.util.Set;

@RestController
public class FilterController {

    private final FilterService filterService;


    public FilterController(FilterService filterService) {
        this.filterService = filterService;
    }

    @GetMapping("/questions/filter/tags")
    public ResponseEntity<Page<QuestionSummaryResponse>> getQuestionsByTags(
            @RequestParam Set<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionSummaryResponse> questions = filterService.getQuestionsByTags(tags, pageable);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/questions/filter/askedTime")
    public ResponseEntity<Page<QuestionSummaryResponse>> getQuestionsByAskedTime(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionSummaryResponse> questions = filterService.findAllByOrderByAskedTimeDesc(pageable);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/questions/filter/lastActivity")
    public ResponseEntity<Page<QuestionSummaryResponse>> getQuestionsByLastActivity(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionSummaryResponse> questions = filterService.findAllByOrderByLastActivityTimeDesc(pageable);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/questions/filter/votes")
    public ResponseEntity<Page<QuestionSummaryResponse>> getQuestionsByVotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionSummaryResponse> questions = filterService.findAllByOrderByVotesDesc(pageable);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/questions/filter/viewCount")
    public ResponseEntity<Page<QuestionSummaryResponse>> getQuestionsByViewCount(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionSummaryResponse> questions = filterService.findAllByOrderByViewCountDesc(pageable);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/questions/filter/noAnswers")
    public ResponseEntity<Page<QuestionSummaryResponse>> getQuestionsWithNoAnswers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionSummaryResponse> questions = filterService.findQuestionsWithNoAnswers(pageable);
        return ResponseEntity.ok(questions);
    }

}
