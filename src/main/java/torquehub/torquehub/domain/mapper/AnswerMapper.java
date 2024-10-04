package torquehub.torquehub.domain.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import torquehub.torquehub.domain.model.Answer;
import torquehub.torquehub.domain.model.Comment;
import torquehub.torquehub.domain.request.AnswerDtos.AnswerCreateRequest;
import torquehub.torquehub.domain.request.AnswerDtos.AnswerEditRequest;
import torquehub.torquehub.domain.response.AnswerDtos.AnswerResponse;
import torquehub.torquehub.domain.response.CommentDtos.CommentResponse;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {CommentMapper.class})
public interface AnswerMapper {

    Answer toEntity(AnswerCreateRequest answerCreateRequest);
    Answer toEntity(AnswerEditRequest answerEditRequest);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "comments", expression = "java(limitComments(answer.getComments(), 0, commentMapper))")
    @Mapping(target = "postedTime", expression = "java(java.util.Date.from(answer.getAnsweredTime().atZone(java.time.ZoneId.systemDefault()).toInstant()))")
    AnswerResponse toResponse(Answer answer, @Context CommentMapper commentMapper);

    // Method to limit the number of comments and use @Context to access commentMapper
    default List<CommentResponse> limitComments(Set<Comment> comments, int startIndex, @Context CommentMapper commentMapper) {
        PriorityQueue<Comment> priorityQueue = new PriorityQueue<>((c1, c2) -> Integer.compare(c2.getVotes(), c1.getVotes()));

        // Add all comments to the priority queue
        priorityQueue.addAll(comments);

        // Extract and map the top 5 comments based on votes
        return priorityQueue.stream()
                .skip(startIndex) // Skip comments if we want to load more
                .limit(5)         // Limit to 5 comments
                .map(commentMapper::toResponse)  // Map Comment to CommentResponse
                .collect(Collectors.toList());
    }

}
