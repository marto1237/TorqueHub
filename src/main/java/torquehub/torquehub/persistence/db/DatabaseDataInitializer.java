package torquehub.torquehub.persistence.db;

import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import torquehub.torquehub.domain.User;
import torquehub.torquehub.persistence.repository.UserRepository;

@Component
@AllArgsConstructor
public class DatabaseDataInitializer {

    private UserRepository userRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void populateDatabaseInitialDummyData() {
        if (userRepository.count() == 0) {
            userRepository.save(User.builder().username("joe").email("joeMama@gmail.com").build());
        }
    }
}

