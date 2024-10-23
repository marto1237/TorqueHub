package torquehub.torquehub.persistence.jpa.impl;

import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.persistence.jpa.interfaces.SpringDataJpaUserRepository;
import torquehub.torquehub.persistence.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaUserRepository implements UserRepository {

    private final SpringDataJpaUserRepository userRepository;

    public JpaUserRepository(SpringDataJpaUserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public List<JpaUser> findAll() {
        return userRepository.findAll();
    }

    @Override
    public JpaUser save(JpaUser jpaUser) {
        return userRepository.save(jpaUser);
    }

    @Override
    public Optional<JpaUser> findById(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public boolean delete(JpaUser jpaUser) {
        if (userRepository.existsById(jpaUser.getId())) {
            userRepository.delete(jpaUser);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Optional<JpaUser> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<JpaUser> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
