package torquehub.torquehub.persistence.jpa.impl;

import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.jpa_models.*;
import torquehub.torquehub.persistence.jpa.interfaces.SpringDataJpaVoteRepository;
import torquehub.torquehub.persistence.repository.VoteRepository;

import java.util.Optional;

@Repository
public class JpaVoteRepository implements VoteRepository {

    private final SpringDataJpaVoteRepository voteRepository;

    public JpaVoteRepository(SpringDataJpaVoteRepository voteRepository) {
        this.voteRepository = voteRepository;
    }

    @Override
    public Optional<JpaVote> findByUserAndJpaAnswer(JpaUser jpaUser, JpaAnswer jpaAnswer) {
        return voteRepository.findByJpaUserAndJpaAnswer(jpaUser, jpaAnswer);
    }

    @Override
    public Optional<JpaVote> findByUserIdAndAnswerId(Long userId, Long answerId) {
        return voteRepository.findByJpaUserIdAndJpaAnswerId(userId, answerId);
    }

    @Override
    public boolean delete(JpaVote jpaVote) {
        if (voteRepository.existsById(jpaVote.getId())) {
            voteRepository.delete(jpaVote);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public JpaVote save(JpaVote jpaVote) {
        return voteRepository.save(jpaVote);
    }

    @Override
    public Optional<JpaVote> findTopByJpaUserAndJpaQuestionOrderByVotedAtDesc(JpaUser jpaUser, JpaQuestion jpaQuestion) {
        return voteRepository.findTopByJpaUserAndJpaQuestionOrderByVotedAtDesc(jpaUser, jpaQuestion);
    }

    @Override
    public Optional<JpaVote> findByUserAndJpaComment(JpaUser jpaUser, JpaComment jpaComment) {
        return voteRepository.findByJpaUserAndJpaComment(jpaUser, jpaComment);
    }

    @Override
    public Optional<JpaVote> findByUserAndJpaQuestion(JpaUser jpaUser, JpaQuestion jpaQuestion) {
        return voteRepository.findByJpaUserAndJpaQuestion(jpaUser, jpaQuestion);
    }

    @Override
    public Optional<JpaVote> findByUserIdAndCommentId(Long userId, Long commentId) {
        return voteRepository.findByJpaUserIdAndJpaCommentId(userId, commentId);
    }

}
