package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import torquehub.torquehub.domain.model.jpa_models.JpaComment;
import torquehub.torquehub.domain.model.jpa_models.JpaVote;
import torquehub.torquehub.domain.request.comment_dtos.CommentCreateRequest;
import torquehub.torquehub.domain.request.comment_dtos.CommentEditRequest;
import torquehub.torquehub.domain.response.comment_dtos.CommentResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaVoteRepository;

import java.util.Optional;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    JpaComment toEntity(CommentCreateRequest commentCreateRequest);
    JpaComment toEntity(CommentEditRequest commentEditRequest);

    @Mapping(target = "username", source = "jpaComment.jpaUser.username")
    @Mapping(target = "postedTime", source = "jpaComment.commentedTime")
    @Mapping(target = "userPoints", source = "jpaComment.jpaUser.points")
    @Mapping(target = "userVote", expression = "java(getUserVote(jpaComment, userId, voteRepository))")
    CommentResponse toResponse(JpaComment jpaComment, Long userId, JpaVoteRepository voteRepository);

    default String getUserVote(JpaComment jpaComment, Long userId, JpaVoteRepository voteRepository) {
        if (userId == null) return null;
        Optional<JpaVote> userVoteOptional = voteRepository.findByUserIdAndCommentId(userId, jpaComment.getId());
        return userVoteOptional.map(vote -> vote.isUpvote() ? "up" : "down").orElse(null);
    }
}
