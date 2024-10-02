package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import torquehub.torquehub.domain.model.Question;
import torquehub.torquehub.domain.request.QuestionDtos.QuestionCreateRequest;
import torquehub.torquehub.domain.request.QuestionDtos.QuestionUpdateRequest;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionResponse;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    Question toEntity(QuestionCreateRequest questionCreateRequest);
    Question toEntity(QuestionUpdateRequest questionUpdateRequest);
    QuestionResponse toResponse(Question question);
}
