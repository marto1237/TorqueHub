package torquehub.torquehub.domain.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import torquehub.torquehub.domain.model.jpa_models.JpaAnswer;
import torquehub.torquehub.domain.model.jpa_models.JpaComment;
import torquehub.torquehub.domain.model.jpa_models.JpaVote;
import torquehub.torquehub.domain.request.answer_dtos.AnswerCreateRequest;
import torquehub.torquehub.domain.request.answer_dtos.AnswerEditRequest;
import torquehub.torquehub.domain.response.answer_dtos.AnswerResponse;
import torquehub.torquehub.domain.response.comment_dtos.CommentResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaBookmarkRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaFollowRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaVoteRepository;

import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring", uses = {CommentMapper.class})
public interface AnswerMapper {

    JpaAnswer toEntity(AnswerCreateRequest answerCreateRequest);
    JpaAnswer toEntity(AnswerEditRequest answerEditRequest);

    @Mapping(target = "id", source = "jpaAnswer.id")
    @Mapping(target = "username", source = "jpaAnswer.jpaUser.username")
    @Mapping(target = "userPoints", source = "jpaAnswer.jpaUser.points")
    @Mapping(target = "isEdited", source = "jpaAnswer.edited")
    @Mapping(target = "comments", expression = "java(limitComments(jpaAnswer.getJpaComments(), 0, commentMapper, userId, voteRepository))")
    @Mapping(target = "postedTime", expression = "java(java.util.Date.from(jpaAnswer.getAnsweredTime().atZone(java.time.ZoneId.systemDefault()).toInstant()))")
    @Mapping(target = "isBookmarked", expression = "java(isBookmarked(jpaAnswer.getId(), userId, bookmarkRepository))")
    @Mapping(target = "isFollowing", expression = "java(isFollowing(jpaAnswer.getId(), userId, followRepository))")
    @Mapping(target = "userVote", expression = "java(getUserVote(jpaAnswer, userId, voteRepository))")
    AnswerResponse toResponse(JpaAnswer jpaAnswer, Long userId, JpaBookmarkRepository bookmarkRepository, JpaFollowRepository followRepository,JpaVoteRepository voteRepository, CommentMapper commentMapper);

    default List<CommentResponse> limitComments(List<JpaComment> jpaComments, int startIndex, @Context CommentMapper commentMapper, Long userId, JpaVoteRepository voteRepository) {
        if (jpaComments == null || jpaComments.isEmpty()) {
            return List.of();
        }

        // Limit to the top 5 comments, starting at startIndex
        return jpaComments.stream()
                .skip(startIndex) // Skip comments if we want to paginate
                .limit(5)         // Limit to 5 comments
                .map(comment -> commentMapper.toResponse(comment, userId, voteRepository))  // Map Comment to CommentResponse
                .toList();
    }

    default boolean isBookmarked(Long answerId, Long userId, JpaBookmarkRepository bookmarkRepository) {
        return userId != null && bookmarkRepository.findByUserIdAndAnswerId(userId, answerId).isPresent();
    }

    default boolean isFollowing(Long answerId, Long userId, JpaFollowRepository followRepository) {
        return userId != null && followRepository.findByUserIdAndAnswerId(userId, answerId).isPresent();
    }

    default String getUserVote(JpaAnswer jpaAnswer, Long userId, JpaVoteRepository voteRepository) {
        if (userId == null) return null;
        Optional<JpaVote> userVoteOptional = voteRepository.findByUserIdAndAnswerId(userId, jpaAnswer.getId());
        return userVoteOptional.map(vote -> vote.isUpvote() ? "up" : "down").orElse(null);
    }

}
