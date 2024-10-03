package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import torquehub.torquehub.domain.model.Answer;
import torquehub.torquehub.domain.request.AnswerDtos.AddAnswerRequest;
import torquehub.torquehub.domain.request.AnswerDtos.EditAnswerRequest;
import torquehub.torquehub.domain.response.AnswerDtos.AnswerResponse;

@Mapper(componentModel = "spring")
public interface AnswerMapper {

    Answer toEntity(AddAnswerRequest addAnswerRequest);
    Answer toEntity(EditAnswerRequest editAnswerRequest);
    AnswerResponse toResponse(Answer answer);
}
