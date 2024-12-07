package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import torquehub.torquehub.domain.response.answer_dtos.AnswerResponse;
import torquehub.torquehub.domain.response.profile_dtos.ProfileResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionSummaryResponse;
import torquehub.torquehub.domain.response.user_dtos.UserResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    @Mapping(target = "user", source = "userResponse")
    @Mapping(target = "questions", source = "questions")
    @Mapping(target = "answers", source = "answers")
    @Mapping(target = "questionCount", source = "questionCount")
    @Mapping(target = "answerCount", source = "answerCount")

    ProfileResponse toProfileResponse(
            UserResponse userResponse,
            List<QuestionSummaryResponse> questions,
            List<AnswerResponse> answers,
            Long questionCount,
            Long answerCount
    );
}
