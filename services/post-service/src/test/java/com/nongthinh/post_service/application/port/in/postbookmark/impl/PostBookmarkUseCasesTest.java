package com.nongthinh.post_service.application.port.in.postbookmark.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nongthinh.post_service.application.model.PostPage;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.CurrentUserProvider;
import com.nongthinh.post_service.application.port.out.repository.BookmarkRepository;
import com.nongthinh.post_service.application.port.out.repository.PostRepository;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.domain.interaction.Bookmark;
import com.nongthinh.post_service.domain.post.Post;
import com.nongthinh.post_service.domain.post.valueobject.AuthorUserId;
import com.nongthinh.post_service.domain.post.valueobject.PostContent;
import com.nongthinh.post_service.domain.post.valueobject.PostId;
import com.nongthinh.post_service.domain.post.valueobject.PostTypeId;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostBookmarkUseCasesTest {
    private static final UUID POST_ID = UUID.fromString("82e40593-897b-4cb2-b8fd-59a6ef7b7c86");
    private static final UUID USER_ID = UUID.fromString("c3673fe7-48d4-4226-a9b5-599091d88c63");
    private static final UUID POST_TYPE_ID = UUID.fromString("ce2c7e30-1291-421d-84a0-426dd09bd535");
    private static final Instant NOW = Instant.parse("2026-08-25T00:00:00Z");

    @Mock private BookmarkRepository bookmarkRepository;
    @Mock private PostRepository postRepository;
    @Mock private PostUseCaseSupport postSupport;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private ClockProvider clockProvider;

    @Test
    void savesBookmarkForCurrentUser() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(clockProvider.now()).thenReturn(NOW);
        when(bookmarkRepository.save(any(Bookmark.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var view = new SavePostBookmarkUseCaseImpl(
                bookmarkRepository, postSupport, currentUserProvider, clockProvider)
                .execute(POST_ID);

        assertThat(view.postId()).isEqualTo(POST_ID);
        assertThat(view.userId()).isEqualTo(USER_ID);
        assertThat(view.createdAt()).isEqualTo(NOW);
        verify(postSupport).requireInteractablePost(POST_ID);
    }

    @Test
    void reportsCurrentUsersBookmarkStatus() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(bookmarkRepository.exists(POST_ID, USER_ID)).thenReturn(true);

        var status = new GetPostBookmarkStatusUseCaseImpl(
                bookmarkRepository, postSupport, currentUserProvider).execute(POST_ID);

        assertThat(status.postId()).isEqualTo(POST_ID);
        assertThat(status.bookmarked()).isTrue();
        verify(postSupport).requireInteractablePost(POST_ID);
    }

    @Test
    void removingMissingBookmarkRemainsIdempotent() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(USER_ID);

        new RemovePostBookmarkUseCaseImpl(
                bookmarkRepository, postSupport, currentUserProvider).execute(POST_ID);

        verify(bookmarkRepository).delete(POST_ID, USER_ID);
    }

    @Test
    void listsOnlyPostsReturnedByBookmarkedPostQuery() {
        Post post = Post.createPublished(
                new PostId(POST_ID), new AuthorUserId(USER_ID), new PostTypeId(POST_TYPE_ID),
                null, new PostContent("Bookmarked content"), null, PostVisibility.PUBLIC,
                List.of(), Set.of(), NOW, 10);
        when(currentUserProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(postRepository.findBookmarkedByUser(USER_ID, 0, 20))
                .thenReturn(new PostPage(List.of(post), 0, 20, 1, 1, false));

        var page = new ListMyBookmarkedPostsUseCaseImpl(
                postRepository, currentUserProvider, postSupport).execute(0, 20);

        assertThat(page.items()).singleElement().satisfies(view ->
                assertThat(view.id()).isEqualTo(POST_ID));
        assertThat(page.totalElements()).isEqualTo(1);
        verify(postSupport).validatePage(0, 20);
    }
}
