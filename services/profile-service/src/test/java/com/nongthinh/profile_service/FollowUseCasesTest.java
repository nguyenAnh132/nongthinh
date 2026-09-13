package com.nongthinh.profile_service;
import com.nongthinh.profile_service.application.port.in.follow.impl.*;
import com.nongthinh.profile_service.application.port.out.*;
import com.nongthinh.profile_service.application.port.out.repository.UserFollowRepository;
import com.nongthinh.profile_service.application.service.FollowSupport;
import com.nongthinh.profile_service.application.view.FollowProfileView;
import com.nongthinh.profile_service.common.currentuser.*;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import com.nongthinh.profile_service.domain.follow.UserFollow;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FollowUseCasesTest {
    private final UUID actor = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID target = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private final UUID eventId = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private final Instant now = Instant.parse("2026-09-12T00:00:00Z");
    private final UserFollowRepository repository = mock(UserFollowRepository.class);
    private final FollowNotificationRepository notifications = mock(FollowNotificationRepository.class);
    private final CurrentUserProvider users = mock(CurrentUserProvider.class);
    private final FollowSupport support = new FollowSupport(users, repository);
    private final FollowUserUseCaseImpl follow = new FollowUserUseCaseImpl(support, repository, notifications, () -> eventId, () -> now);
    @BeforeEach
    void user() {
        when(users.getCurrentUser()).thenReturn(new CurrentUser(actor, "subject", "", Set.of("ROLE_FARMER"), Set.of()));
    }
    private void profiles() {
        when(repository.profile(actor, actor)).thenReturn(Optional.of(new FollowProfileView(actor, "Farmer", null, "FARMER", 0, 0, false)));
        when(repository.profile(target, actor)).thenReturn(Optional.of(new FollowProfileView(target, "Brand", null, "BRAND", 1, 0, true)));
    }
    @Test void followCreatesOneNotificationOnlyWhenInserted() {
        profiles();
        when(repository.add(any())).thenReturn(true, false);
        follow.execute(target);
        follow.execute(target);
        verify(notifications, times(1)).newFollower(eventId, actor, target, now);
    }
    @Test void selfFollowIsRejectedBeforePersistence() {
        assertThatThrownBy(() -> follow.execute(actor)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot follow yourself");
        verifyNoInteractions(repository, notifications);
    }
    @Test void missingOrInactiveTargetCannotBeFollowed() {
        when(repository.profile(actor, actor)).thenReturn(Optional.of(new FollowProfileView(actor, "Farmer", null, "FARMER", 0, 0, false)));
        when(repository.profile(target, actor)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> follow.execute(target)).isInstanceOf(BusinessException.class);
        verify(repository, never()).add(any(UserFollow.class));
        verifyNoInteractions(notifications);
    }
    @Test void brandCanFollowFarmer() {
        when(users.getCurrentUser()).thenReturn(new CurrentUser(actor, "subject", "", Set.of("ROLE_BRAND"), Set.of()));
        profiles();
        follow.execute(target);
        verify(repository).add(any(UserFollow.class));
    }
    @Test void adminCannotFollow() {
        when(users.getCurrentUser()).thenReturn(new CurrentUser(actor, "subject", "", Set.of("ROLE_ADMIN"), Set.of()));
        assertThatThrownBy(() -> follow.execute(target)).isInstanceOf(BusinessException.class);
        verifyNoInteractions(repository);
    }
    @Test void unfollowIsIdempotentAndAllowsInactiveTarget() {
        var unfollow = new UnfollowUserUseCaseImpl(support, repository);
        unfollow.execute(target);
        unfollow.execute(target);
        verify(repository, times(2)).remove(actor, target);
        verifyNoInteractions(notifications);
    }
    @Test void paginationRejectsUnboundedRequests() {
        var list = new ListFollowsUseCaseImpl(support, repository);
        assertThatThrownBy(() -> list.execute(target, true, -1, 20)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> list.execute(target, false, 0, 101)).isInstanceOf(BusinessException.class);
        verifyNoInteractions(repository);
    }
    @Test void batchStatusIsBounded() {
        var status = new GetFollowingStatusUseCaseImpl(support, repository);
        assertThatThrownBy(() -> status.execute(Collections.nCopies(101, target))).isInstanceOf(BusinessException.class);
    }
}
