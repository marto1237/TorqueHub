package torquehub.torquehub.persistence.jpa.impl;

import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.*;
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
    public Optional<Vote> findByUserAndAnswer(User user, Answer answer) {
        return voteRepository.findByUserAndAnswer(user, answer);
    }

    @Override
    public boolean delete(Vote vote) {
        if (voteRepository.existsById(vote.getId())) {
            voteRepository.delete(vote);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Vote save(Vote vote) {
        return voteRepository.save(vote);
    }

    @Override
    public Optional<Vote> findByUserAndComment(User user, Comment comment) {
        return voteRepository.findByUserAndComment(user, comment);
    }

    @Override
    public Optional<Vote> findByUserAndQuestion(User user, Question question) {
        return voteRepository.findByUserAndQuestion(user, question);
    }


}
