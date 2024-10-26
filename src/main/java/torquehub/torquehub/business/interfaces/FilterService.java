package torquehub.torquehub.business.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import torquehub.torquehub.domain.response.question_dtos.QuestionSummaryResponse;
import java.util.Set;

public interface FilterService {
    Page<QuestionSummaryResponse> getQuestionsByTags(Set<String> tags, Pageable pageable);
    Page<QuestionSummaryResponse> findAllByOrderByAskedTimeDesc(Pageable pageable);
    Page<QuestionSummaryResponse> findAllByOrderByLastActivityTimeDesc(Pageable pageable);
    Page<QuestionSummaryResponse> findAllByOrderByVotesDesc(Pageable pageable);
    Page<QuestionSummaryResponse> findAllByOrderByViewCountDesc(Pageable pageable);
    Page<QuestionSummaryResponse> findQuestionsWithNoAnswers(Pageable pageable);
}
