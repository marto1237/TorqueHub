package torquehub.torquehub.persistence.jpa.impl;

import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.jpa_models.JpaUserPromotionLog;
import torquehub.torquehub.persistence.jpa.interfaces.SpringDataJpaUserPromotionLogRepository;
import torquehub.torquehub.persistence.repository.UserPromotionLogRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaUserPromotionLogRepository implements UserPromotionLogRepository {

    private final SpringDataJpaUserPromotionLogRepository userPromotionLogRepository;

    public JpaUserPromotionLogRepository(SpringDataJpaUserPromotionLogRepository springDataJpaUserPromotionLogRepository) {
        this.userPromotionLogRepository = springDataJpaUserPromotionLogRepository;
    }

    @Override
    public List<JpaUserPromotionLog> findAll() {
        return userPromotionLogRepository.findAll();
    }

    @Override
    public JpaUserPromotionLog save(JpaUserPromotionLog userPromotionLog) {
        return userPromotionLogRepository.save(userPromotionLog);
    }

    @Override
    public Optional<JpaUserPromotionLog> findById(Long id) {
        return userPromotionLogRepository.findById(id);
    }

    @Override
    public boolean delete(JpaUserPromotionLog userPromotionLog) {
        if (userPromotionLogRepository.existsById(userPromotionLog.getId())) {
            userPromotionLogRepository.delete(userPromotionLog);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean existsById(Long id) {
        return userPromotionLogRepository.existsById(id);
    }
}
