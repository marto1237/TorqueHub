package torquehub.torquehub.persistence.repository;

import torquehub.torquehub.domain.model.Notification;


public interface NotificationRepository {
     Notification save(Notification notification);
}
