package com.nongthinh.post_service.application.port.in.postreport.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nongthinh.post_service.application.command.CompletePostReportCommand;
import com.nongthinh.post_service.application.command.CreatePostReportCommand;
import com.nongthinh.post_service.application.model.PostReportPage;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.CurrentUserProvider;
import com.nongthinh.post_service.application.port.out.IdGenerator;
import com.nongthinh.post_service.application.port.out.repository.PostRepository;
import com.nongthinh.post_service.application.port.out.repository.PostReportRepository;
import com.nongthinh.post_service.application.service.PostHistoryRecorder;
import com.nongthinh.post_service.application.service.PostReportNotificationRecorder;
import com.nongthinh.post_service.application.service.PostReportUseCaseSupport;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.configuration.PostLimitsProperties;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.nongthinh.post_service.domain.post.Post;
import com.nongthinh.post_service.domain.post.valueobject.AuthorUserId;
import com.nongthinh.post_service.domain.post.valueobject.PostContent;
import com.nongthinh.post_service.domain.post.valueobject.PostId;
import com.nongthinh.post_service.domain.post.valueobject.PostTypeId;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import com.nongthinh.post_service.domain.report.PostReport;
import com.nongthinh.post_service.domain.report.valueobject.ReportReason;
import com.nongthinh.post_service.domain.report.valueobject.ReportStatus;
import com.nongthinh.post_service.domain.report.valueobject.PostModerationAction;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostReportUseCasesTest {
    private static final UUID POST_ID = UUID.fromString("a68bbce3-eb91-4eed-9eb4-690c92de33ad");
    private static final UUID POST_TYPE_ID = UUID.fromString("651b0f1b-8a30-4127-93bf-e214184e5595");
    private static final UUID AUTHOR_ID = UUID.fromString("a95e9cb2-f9db-47f5-88cd-773a413e9ad8");
    private static final UUID REPORTER_ID = UUID.fromString("ad254e6c-3e87-4247-a21d-c6b508b915f6");
    private static final UUID MODERATOR_ID = UUID.fromString("eaa6479d-b00e-434b-91c8-46ac05995c6a");
    private static final UUID REPORT_ID = UUID.fromString("10a55220-5d57-4afb-b9f1-45b8de39a93b");
    private static final Instant NOW = Instant.parse("2026-08-26T00:00:00Z");

    @Mock private PostReportRepository repository;
    @Mock private PostRepository postRepository;
    @Mock private PostUseCaseSupport postSupport;
    @Mock private PostHistoryRecorder historyRecorder;
    @Mock private PostReportNotificationRecorder notificationRecorder;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private IdGenerator idGenerator;
    @Mock private ClockProvider clockProvider;
    private PostReportUseCaseSupport reportSupport;

    @BeforeEach
    void setUp() {
        reportSupport = new PostReportUseCaseSupport(
                repository,
                new PostLimitsProperties(2_000, 10, 100)
        );
    }

    @Test
    void createsOnePendingReportForAnotherUsersPost() {
        when(postSupport.requireInteractablePost(POST_ID)).thenReturn(publishedPost(AUTHOR_ID));
        when(currentUserProvider.getCurrentUserId()).thenReturn(REPORTER_ID);
        when(idGenerator.generate()).thenReturn(REPORT_ID);
        when(clockProvider.now()).thenReturn(NOW);
        when(repository.save(any(PostReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        var view = new CreatePostReportUseCaseImpl(
                repository, postSupport, reportSupport, currentUserProvider,
                idGenerator, clockProvider
        ).execute(POST_ID, new CreatePostReportCommand(ReportReason.SPAM, "  Repeated ads  "));

        assertThat(view.id()).isEqualTo(REPORT_ID);
        assertThat(view.reporterId()).isEqualTo(REPORTER_ID);
        assertThat(view.reasonDetail()).isEqualTo("Repeated ads");
        assertThat(view.status()).isEqualTo(ReportStatus.PENDING);
    }

    @Test
    void rejectsSelfReportBeforePersistence() {
        when(postSupport.requireInteractablePost(POST_ID)).thenReturn(publishedPost(REPORTER_ID));
        when(currentUserProvider.getCurrentUserId()).thenReturn(REPORTER_ID);

        assertThatThrownBy(() -> new CreatePostReportUseCaseImpl(
                repository, postSupport, reportSupport, currentUserProvider,
                idGenerator, clockProvider
        ).execute(POST_ID, new CreatePostReportCommand(ReportReason.OTHER, "Reason")))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.POST_REPORT_SELF_NOT_ALLOWED));

        verify(repository, never()).save(any());
    }

    @Test
    void rejectsDuplicateReportBeforePersistence() {
        when(postSupport.requireInteractablePost(POST_ID)).thenReturn(publishedPost(AUTHOR_ID));
        when(currentUserProvider.getCurrentUserId()).thenReturn(REPORTER_ID);
        when(repository.existsByPostIdAndReporterId(POST_ID, REPORTER_ID)).thenReturn(true);

        assertThatThrownBy(() -> new CreatePostReportUseCaseImpl(
                repository, postSupport, reportSupport, currentUserProvider,
                idGenerator, clockProvider
        ).execute(POST_ID, new CreatePostReportCommand(ReportReason.SPAM, null)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.POST_ALREADY_REPORTED));

        verify(repository, never()).save(any());
    }

    @Test
    void listsReportsByStatusUsingRepositoryPagination() {
        PostReport report = pendingReport();
        when(repository.findAll(ReportStatus.PENDING, 0, 20))
                .thenReturn(new PostReportPage(List.of(report), 0, 20, 1, 1, false));

        var page = new ListPostReportsUseCaseImpl(repository, reportSupport)
                .execute(ReportStatus.PENDING, 0, 20);

        assertThat(page.items()).singleElement().satisfies(view ->
                assertThat(view.id()).isEqualTo(REPORT_ID));
        assertThat(page.totalElements()).isEqualTo(1);
    }

    @Test
    void reviewsThenResolvesReportWithCurrentModerator() {
        PostReport report = pendingReport();
        Post moderatedPost = publishedPost(AUTHOR_ID);
        when(repository.findByIdForUpdate(REPORT_ID)).thenReturn(Optional.of(report));
        when(repository.save(any(PostReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(clockProvider.now()).thenReturn(NOW.plusSeconds(1), NOW.plusSeconds(2));
        when(currentUserProvider.getCurrentUserId()).thenReturn(MODERATOR_ID);
        when(postSupport.requirePostForUpdateIncludingDeleted(POST_ID))
                .thenReturn(moderatedPost);
        when(postRepository.save(any(Post.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var reviewView = new StartPostReportReviewUseCaseImpl(
                repository, reportSupport, clockProvider
        ).execute(REPORT_ID);
        var resolvedView = new ResolvePostReportUseCaseImpl(
                repository, reportSupport, postSupport, postRepository, historyRecorder,
                notificationRecorder, currentUserProvider, clockProvider
        ).execute(REPORT_ID, new CompletePostReportCommand(
                "  Confirmed violation  ", PostModerationAction.HIDE));

        assertThat(reviewView.status()).isEqualTo(ReportStatus.UNDER_REVIEW);
        assertThat(resolvedView.status()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(resolvedView.resolvedBy()).isEqualTo(MODERATOR_ID);
        assertThat(resolvedView.resolutionNote()).isEqualTo("Confirmed violation");
        assertThat(moderatedPost.getStatus().name()).isEqualTo("HIDDEN");
        verify(historyRecorder).recordModeration(
                any(Post.class), any(UUID.class), any(), any(), any(), any(), any(), any(), any());
        verify(notificationRecorder).recordResolved(
                report, moderatedPost, MODERATOR_ID, PostModerationAction.HIDE,
                NOW.plusSeconds(2));
    }

    @Test
    void rejectsOpenReportAndPreventsFurtherTransition() {
        PostReport report = pendingReport();
        when(repository.findByIdForUpdate(REPORT_ID)).thenReturn(Optional.of(report));
        when(repository.save(any(PostReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(clockProvider.now()).thenReturn(NOW.plusSeconds(1));
        when(currentUserProvider.getCurrentUserId()).thenReturn(MODERATOR_ID);
        when(postSupport.requirePostIncludingDeleted(POST_ID)).thenReturn(publishedPost(AUTHOR_ID));

        var rejectedView = new RejectPostReportUseCaseImpl(
                repository, reportSupport, postSupport, notificationRecorder,
                currentUserProvider, clockProvider
        ).execute(REPORT_ID, new CompletePostReportCommand(null));

        assertThat(rejectedView.status()).isEqualTo(ReportStatus.REJECTED);
        assertThatThrownBy(() -> new StartPostReportReviewUseCaseImpl(
                repository, reportSupport, clockProvider
        ).execute(REPORT_ID))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.POST_REPORT_STATUS_CONFLICT));
    }

    private static PostReport pendingReport() {
        return PostReport.create(
                REPORT_ID, POST_ID, REPORTER_ID, ReportReason.SPAM, null, NOW
        );
    }

    private static Post publishedPost(UUID authorId) {
        return Post.createPublished(
                new PostId(POST_ID),
                new AuthorUserId(authorId),
                new PostTypeId(POST_TYPE_ID),
                null,
                new PostContent("Published content"),
                null,
                PostVisibility.PUBLIC,
                List.of(),
                Set.of(),
                NOW,
                10
        );
    }
}
