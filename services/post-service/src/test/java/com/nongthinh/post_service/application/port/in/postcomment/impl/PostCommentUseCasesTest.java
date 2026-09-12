package com.nongthinh.post_service.application.port.in.postcomment.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nongthinh.post_service.application.command.CreatePostCommentCommand;
import com.nongthinh.post_service.application.command.UpdatePostCommentCommand;
import com.nongthinh.post_service.application.model.CommentPage;
import com.nongthinh.post_service.application.model.CommentThread;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.CurrentUserProvider;
import com.nongthinh.post_service.application.port.out.IdGenerator;
import com.nongthinh.post_service.application.port.out.repository.CommentRepository;
import com.nongthinh.post_service.application.service.PostCommentUseCaseSupport;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.comment.Comment;
import com.nongthinh.post_service.domain.exception.BusinessException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostCommentUseCasesTest {
    private static final UUID POST_ID = UUID.fromString("c979228d-84e5-4a14-b580-1db0ff216c1f");
    private static final UUID ROOT_ID = UUID.fromString("d070cbda-aeee-4d49-a96f-a80384a91e03");
    private static final UUID REPLY_ID = UUID.fromString("df62c5ee-8309-4666-ac03-658e6c259d51");
    private static final UUID AUTHOR_ID = UUID.fromString("e5183aa6-d4d0-4877-bd02-b8d10cc9243f");
    private static final UUID OTHER_USER_ID = UUID.fromString("abceeb48-f4d0-432c-8e48-90cd7628a0de");
    private static final Instant NOW = Instant.parse("2026-08-24T00:00:00Z");

    @Mock private CommentRepository repository;
    @Mock private PostUseCaseSupport postSupport;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private IdGenerator idGenerator;
    @Mock private ClockProvider clockProvider;
    @Mock private com.nongthinh.post_service.application.service.PostEngagementRecorder engagement;
    @Mock private com.nongthinh.post_service.domain.post.Post post;
    private PostCommentUseCaseSupport commentSupport;

    @BeforeEach
    void setUp() {
        commentSupport = new PostCommentUseCaseSupport(repository, currentUserProvider);
        org.mockito.Mockito.lenient().when(postSupport.requirePostForUpdate(POST_ID)).thenReturn(post);
        org.mockito.Mockito.lenient().when(post.getAuthorUserId()).thenReturn(
                new com.nongthinh.post_service.domain.post.valueobject.AuthorUserId(AUTHOR_ID));
        org.mockito.Mockito.lenient().when(engagement.record(any(), any(), org.mockito.ArgumentMatchers.anyBoolean(),
                org.mockito.ArgumentMatchers.nullable(UUID.class), org.mockito.ArgumentMatchers.nullable(String.class)))
                .thenReturn(new com.nongthinh.post_service.application.view.PostEngagementView(
                        POST_ID, java.util.Map.of(), 0, 0, null, 1, 2, 5));
    }

    @Test
    void createsReplyUnderRootComment() {
        Comment root = rootComment(AUTHOR_ID);
        when(repository.findByIdAndPostId(ROOT_ID, POST_ID)).thenReturn(Optional.of(root));
        when(currentUserProvider.getCurrentUserId()).thenReturn(OTHER_USER_ID);
        when(idGenerator.generate()).thenReturn(REPLY_ID);
        when(clockProvider.now()).thenReturn(NOW.plusSeconds(1));
        when(repository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var view = new CreatePostCommentUseCaseImpl(
                repository, postSupport, commentSupport, idGenerator, clockProvider, engagement)
                .execute(POST_ID, new CreatePostCommentCommand("  Tráº£ lá»i  ", ROOT_ID));

        assertThat(view.id()).isEqualTo(REPLY_ID);
        assertThat(view.parentCommentId()).isEqualTo(ROOT_ID);
        assertThat(view.commentRootTotal()).isEqualTo(1);
        assertThat(view.commentTotal()).isEqualTo(2);
        assertThat(view.commentVersion()).isEqualTo(5);
        verify(engagement).record(post, OTHER_USER_ID, false, AUTHOR_ID, "COMMENT_REPLY");
        assertThat(view.authorUserId()).isEqualTo(OTHER_USER_ID);
        assertThat(view.content()).isEqualTo("Tráº£ lá»i");
        verify(postSupport).requireInteractablePost(POST_ID);
    }

    @Test
    void rejectsReplyToAnotherReply() {
        Comment root = rootComment(AUTHOR_ID);
        Comment reply = Comment.createReply(
                REPLY_ID, root, OTHER_USER_ID, "Reply", NOW.plusSeconds(1));
        when(repository.findByIdAndPostId(REPLY_ID, POST_ID)).thenReturn(Optional.of(reply));
        when(currentUserProvider.getCurrentUserId()).thenReturn(AUTHOR_ID);
        when(clockProvider.now()).thenReturn(NOW.plusSeconds(2));

        assertThatThrownBy(() -> new CreatePostCommentUseCaseImpl(
                repository, postSupport, commentSupport, idGenerator, clockProvider, engagement)
                .execute(POST_ID, new CreatePostCommentCommand("Nested", REPLY_ID)))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getErrorCode())
                                .isEqualTo(ErrorCode.COMMENT_REPLY_DEPTH_EXCEEDED));

        verify(repository, never()).save(any());
    }

    @Test
    void rejectsEditingCommentOwnedByAnotherUser() {
        Comment comment = rootComment(OTHER_USER_ID);
        when(repository.findByIdAndPostIdForUpdate(ROOT_ID, POST_ID))
                .thenReturn(Optional.of(comment));
        when(currentUserProvider.getCurrentUserId()).thenReturn(AUTHOR_ID);

        assertThatThrownBy(() -> new UpdatePostCommentUseCaseImpl(
                repository, postSupport, commentSupport, clockProvider, engagement)
                .execute(POST_ID, ROOT_ID, new UpdatePostCommentCommand("Changed")))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getErrorCode())
                                .isEqualTo(ErrorCode.COMMENT_ACCESS_DENIED));

        verify(repository, never()).save(any());
    }

    @Test
    void softDeletesCommentOwnedByCurrentUser() {
        Comment comment = rootComment(AUTHOR_ID);
        when(repository.findByIdAndPostIdForUpdate(ROOT_ID, POST_ID))
                .thenReturn(Optional.of(comment));
        when(currentUserProvider.getCurrentUserId()).thenReturn(AUTHOR_ID);
        when(clockProvider.now()).thenReturn(NOW.plusSeconds(1));
        when(repository.save(comment)).thenReturn(comment);

        new DeletePostCommentUseCaseImpl(
                repository, postSupport, commentSupport, clockProvider, engagement)
                .execute(POST_ID, ROOT_ID);

        assertThat(comment.isDeleted()).isTrue();
        assertThat(comment.getDeletedAt()).isEqualTo(NOW.plusSeconds(1));
        verify(repository).save(comment);
    }

    @Test
    void listsRootThreadsWithReplies() {
        Comment root = rootComment(AUTHOR_ID);
        Comment reply = Comment.createReply(
                REPLY_ID, root, OTHER_USER_ID, "Reply", NOW.plusSeconds(1));
        when(repository.findPublishedThreads(POST_ID, 0, 20))
                .thenReturn(new CommentPage(
                        List.of(new CommentThread(root, List.of(reply))),
                        0, 20, 1, 1, false));

        var page = new ListPostCommentsUseCaseImpl(repository, postSupport)
                .execute(POST_ID, 0, 20);

        assertThat(page.items()).singleElement().satisfies(thread -> {
            assertThat(thread.comment().id()).isEqualTo(ROOT_ID);
            assertThat(thread.replies()).singleElement()
                    .extracting(item -> item.id())
                    .isEqualTo(REPLY_ID);
        });
        verify(postSupport).validatePage(0, 20);
    }

    private static Comment rootComment(UUID authorId) {
        return Comment.createRoot(ROOT_ID, POST_ID, authorId, "Root", NOW);
    }
}
