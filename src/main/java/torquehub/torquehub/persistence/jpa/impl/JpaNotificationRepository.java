package torquehub.torquehub.persistence.jpa.impl;

import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.Notification;
import torquehub.torquehub.persistence.jpa.interfaces.SpringDataJpaNotificationRepository;
import torquehub.torquehub.persistence.repository.NotificationRepository;

@Repository
public class JpaNotificationRepository implements NotificationRepository {

    private final SpringDataJpaNotificationRepository notificationRepository;

    public JpaNotificationRepository(SpringDataJpaNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }
}
