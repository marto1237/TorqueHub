package torquehub.torquehub.domain.mapper;

import org.springframework.data.domain.Pageable;
import torquehub.torquehub.persistence.jpa.impl.JpaBookmarkRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaFollowRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaVoteRepository;

public class QuestionMapperContext {
    private final CommentMapper commentMapper;
    private final AnswerMapper answerMapper;
    private final JpaBookmarkRepository bookmarkRepository;
    private final JpaFollowRepository followRepository;
    private final JpaVoteRepository voteRepository;
    private final Long userId;
    private final Pageable pageable;

    public QuestionMapperContext(CommentMapper commentMapper, AnswerMapper answerMapper,
                                 JpaBookmarkRepository bookmarkRepository, JpaFollowRepository followRepository,
                                 JpaVoteRepository voteRepository, Long userId, Pageable pageable) {
        this.commentMapper = commentMapper;
        this.answerMapper = answerMapper;
        this.bookmarkRepository = bookmarkRepository;
        this.followRepository = followRepository;
        this.voteRepository = voteRepository;
        this.userId = userId;
        this.pageable = pageable;
    }

    public CommentMapper getCommentMapper() { return commentMapper; }
    public AnswerMapper getAnswerMapper() { return answerMapper; }
    public JpaBookmarkRepository getBookmarkRepository() { return bookmarkRepository; }
    public JpaFollowRepository getFollowRepository() { return followRepository; }
    public JpaVoteRepository getVoteRepository() { return voteRepository; }
    public Long getUserId() { return userId; }
    public Pageable getPageable() { return pageable; }
}
