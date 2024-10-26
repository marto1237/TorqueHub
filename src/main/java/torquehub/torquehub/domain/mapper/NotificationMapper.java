package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import torquehub.torquehub.domain.model.jpa_models.JpaNotification;
import torquehub.torquehub.domain.request.notification_dtos.CreateNotificationRequest;
import torquehub.torquehub.domain.response.notification_dtos.NotificationResponse;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    JpaNotification toEntity(CreateNotificationRequest createNotificationRequest);

    @Mapping(target = "userId", source = "jpaUser.id")
    @Mapping(target = "voterId", source = "voter.id")
    NotificationResponse toResponse(JpaNotification jpaNotification);
}
