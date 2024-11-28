package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import torquehub.torquehub.domain.model.jpa_models.JpaNotification;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.domain.request.notification_dtos.CreateNotificationRequest;
import torquehub.torquehub.domain.request.notification_dtos.NewAnswerNotificationRequest;
import torquehub.torquehub.domain.response.notification_dtos.NotificationResponse;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    JpaNotification toEntity(CreateNotificationRequest createNotificationRequest);

    @Mapping(target = "userId", source = "jpaUser.id")
    @Mapping(target = "voterId", source = "voter.id")
    @Mapping(target = "isRead", source = "read")
    NotificationResponse toResponse(JpaNotification jpaNotification);

    @Mapping(target = "id", ignore = true) // ID is auto-generated, so ignore it
    @Mapping(target = "jpaUser", source = "questionOwner") // Set the question owner as the recipient
    @Mapping(target = "voter", source = "answerAuthor") // Set the answer author as the voter
    @Mapping(target = "message", source = "request.message")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())") // Set current time
    @Mapping(target = "isRead", constant = "false") // Default unread status
    @Mapping(target = "points", source = "points") // Explicitly set the points from the source
    JpaNotification toJpaNotification(NewAnswerNotificationRequest request, JpaUser questionOwner, JpaUser answerAuthor, Integer points);
}
