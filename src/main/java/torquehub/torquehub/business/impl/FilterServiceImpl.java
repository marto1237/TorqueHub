package torquehub.torquehub.business.impl;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import torquehub.torquehub.business.interfaces.FilterService;
import torquehub.torquehub.domain.mapper.QuestionMapper;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.model.jpa_models.JpaTag;
import torquehub.torquehub.domain.response.question_dtos.QuestionSummaryResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaQuestionRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaTagRepository;

import java.util.List;
import java.util.Set;

@Service
public class FilterServiceImpl implements FilterService {

    private final JpaQuestionRepository questionRepository;
    private final JpaTagRepository tagRepository;
    private final QuestionMapper questionMapper;

    public FilterServiceImpl(JpaQuestionRepository jpaQuestionRepository, JpaTagRepository jpaTagRepository, QuestionMapper questionMapper) {
        this.questionRepository = jpaQuestionRepository;
        this.tagRepository = jpaTagRepository;
        this.questionMapper = questionMapper;
    }

    @Override
    @Cacheable(value = "questionsByTags", key = "#tags.toString() + '-' + #pageable.pageNumber")
    public Page<QuestionSummaryResponse> getQuestionsByTags(Set<String> tags, Pageable pageable) {
        // Collect Tag entities by unwrapping the Optional<Tag> from the repository
        List<JpaTag> jpaTagEntities = tags.stream()
                .map(tagName -> tagRepository.findByName(tagName)
                        .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagName)))
                .toList();

        // Query the questions and map them to QuestionSummaryResponse
        Page<JpaQuestion> filteredQuestions = questionRepository.findQuestionsByTags(jpaTagEntities, pageable);
        return filteredQuestions.map(questionMapper::toSummaryResponse);
    }

    @Override
    @Cacheable(value = "recentQuestions", key = "#pageable.pageNumber")
    public Page<QuestionSummaryResponse> findAllByOrderByAskedTimeDesc(Pageable pageable) {
        Page<JpaQuestion> questions = questionRepository.findAllByOrderByAskedTimeDesc(pageable);
        return questions.map(questionMapper::toSummaryResponse);
    }

    @Override
    @Cacheable(value = "recentActivityQuestions", key = "#pageable.pageNumber")
    public Page<QuestionSummaryResponse> findAllByOrderByLastActivityTimeDesc(Pageable pageable) {
        Page<JpaQuestion> questions = questionRepository.findAllByOrderByLastActivityTimeDesc(pageable);
        return questions.map(questionMapper::toSummaryResponse);
    }

    @Override
    @Cacheable(value = "topVotedQuestions", key = "#pageable.pageNumber")
    public Page<QuestionSummaryResponse> findAllByOrderByVotesDesc(Pageable pageable) {
        Page<JpaQuestion> questions = questionRepository.findAllByOrderByVotesDesc(pageable);
        return questions.map(questionMapper::toSummaryResponse);
    }

    @Override
    @Cacheable(value = "popularQuestions", key = "#pageable.pageNumber")
    public Page<QuestionSummaryResponse> findAllByOrderByViewCountDesc(Pageable pageable) {
        Page<JpaQuestion> questions = questionRepository.findAllByOrderByViewCountDesc(pageable);
        return questions.map(questionMapper::toSummaryResponse);
    }

    @Override
    @Cacheable(value = "questionsWithoutAnswers", key = "#pageable.pageNumber")
    public Page<QuestionSummaryResponse> findQuestionsWithNoAnswers(Pageable pageable) {
        Page<JpaQuestion> questions = questionRepository.findQuestionsWithNoAnswers(pageable);
        return questions.map(questionMapper::toSummaryResponse);
    }

    @Override
    public Page<QuestionSummaryResponse> filterQuestions(Set<String> tags, Boolean noAnswers, Boolean noAcceptedAnswer, String sortOption, Pageable pageable) {
        // Build your dynamic query based on these parameters
        Page<JpaQuestion> questions;

        if (tags != null && !tags.isEmpty()) {
            // Find questions by tags if tags are specified
            List<JpaTag> jpaTagEntities = tags.stream()
                    .map(tagName -> tagRepository.findByName(tagName)
                            .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagName)))
                    .toList();
            questions = questionRepository.findQuestionsByTags(jpaTagEntities, pageable);
        } else {
            // Use sorting and no answers/accepted answers filters
            if (sortOption.equals("newest")) {
                questions = questionRepository.findAllByOrderByAskedTimeDesc(pageable);
            } else if (sortOption.equals("recentActivity")) {
                questions = questionRepository.findAllByOrderByLastActivityTimeDesc(pageable);
            } else if (sortOption.equals("mostLiked")) {
                questions = questionRepository.findAllByOrderByVotesDesc(pageable);
            } else if (sortOption.equals("mostViews")) {
                questions = questionRepository.findAllByOrderByViewCountDesc(pageable);
            } else {
                questions = questionRepository.findAll(pageable); // Default fallback
            }
        }

        if (noAnswers != null && noAnswers) {
            questions = questionRepository.findQuestionsWithNoAnswers(pageable);
        }

        return questions.map(questionMapper::toSummaryResponse);
    }

}
