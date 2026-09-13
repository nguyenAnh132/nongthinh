package com.nongthinh.notification_service;
import com.nongthinh.notification_service.application.event.SocialNotificationEvent;
import com.nongthinh.notification_service.application.port.in.notification.impl.HandleSocialNotificationUseCaseImpl;
import com.nongthinh.notification_service.application.port.out.repository.NotificationRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
class SocialNotificationUseCaseTest {
    private final NotificationRepository repository = mock(NotificationRepository.class);
    private final Instant now = Instant.parse("2026-09-12T00:00:00Z");
    private final UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID actor = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private final UUID recipient = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private final HandleSocialNotificationUseCaseImpl handler = new HandleSocialNotificationUseCaseImpl(repository, () -> id, () -> now);
    @Test void duplicateDeliveryDoesNotCreateAnotherNotification() {
        var event = new SocialNotificationEvent(id, 1, now, actor, recipient, "NEW_FOLLOWER", actor);
        when(repository.claimEvent(id, now)).thenReturn(true, false);
        when(repository.createSocial(event, id)).thenReturn(true);
        handler.execute(event);
        handler.execute(event);
        verify(repository, times(1)).createSocial(event, id);
        verify(repository, times(1)).changed(recipient, id, "notification.created");
    }
    @Test void newPostNotificationUpdatesUnreadState() {
        var event = new SocialNotificationEvent(id, 1, now, actor, recipient, "FOLLOWED_USER_POST", id);
        when(repository.claimEvent(id, now)).thenReturn(true);
        when(repository.createSocial(event, id)).thenReturn(true);
        handler.execute(event);
        verify(repository).changed(recipient, id, "notification.created");
    }
    @Test void invalidEventIsRejected() {
        assertThatThrownBy(() -> handler.execute(new SocialNotificationEvent(id, 1, now, actor, recipient, "INVALID", id)))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repository);
    }
}
