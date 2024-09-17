package torquehub.torquehub.persistence.impl;

import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.User;
import torquehub.torquehub.persistence.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class FakeUserRepoImpl implements UserRepository {
    private static long NEXT_ID = 1;

    private final List<User> savedUsers;

    public FakeUserRepoImpl() {this.savedUsers = new ArrayList<>();}
    @Override
    public List<User> findAll() {
        return Collections.unmodifiableList(savedUsers);
    }

    @Override
    public User findById(long userId) {
        return  savedUsers.stream()
                .filter(user -> user.getId() == userId)
                .findFirst()
                .orElse(null);
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(NEXT_ID);
            NEXT_ID++;
            this.savedUsers.add(user);
        }
        return user;
    }

    @Override
    public void deleteById(long userId) {
        this.savedUsers.removeIf(user -> user.getId() == userId);

    }

    @Override
    public boolean existsById(long userId) {
        return this.savedUsers
                .stream()
                .anyMatch(user -> user.getId() == userId);
    }

    @Override
    public boolean existsByUsername(String username) {
        return this.savedUsers
                .stream()
                .anyMatch(user -> user.getUsername().equals(username));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return this.savedUsers
                .stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public int count() {
        return  this.savedUsers.size();
    }


}
