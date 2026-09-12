package com.nongthinh.post_service.application.port.in.post.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nongthinh.post_service.application.command.CreatePostCommand;
import com.nongthinh.post_service.application.command.CreatePostMediaCommand;
import com.nongthinh.post_service.application.model.FileMetadata;
import com.nongthinh.post_service.application.port.in.postmedia.impl.CreatePostMediaUseCaseImpl;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.CropTypeQuery;
import com.nongthinh.post_service.application.port.out.CurrentUserProvider;
import com.nongthinh.post_service.application.port.out.FileQuery;
import com.nongthinh.post_service.application.port.out.IdGenerator;
import com.nongthinh.post_service.application.port.out.repository.PostHistoryRepository;
import com.nongthinh.post_service.application.port.out.repository.PostRepository;
import com.nongthinh.post_service.application.port.out.repository.PostTopicRepository;
import com.nongthinh.post_service.application.port.out.repository.PostTypeRepository;
import com.nongthinh.post_service.application.service.PostHistoryRecorder;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.configuration.PostFeaturesProperties;
import com.nongthinh.post_service.configuration.PostLimitsProperties;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.nongthinh.post_service.domain.history.PostHistory;
import com.nongthinh.post_service.domain.post.Post;
import com.nongthinh.post_service.domain.post.PostType;
import com.nongthinh.post_service.domain.post.valueobject.AuthorUserId;
import com.nongthinh.post_service.domain.post.valueobject.CropTypeId;
import com.nongthinh.post_service.domain.post.valueobject.MediaType;
import com.nongthinh.post_service.domain.post.valueobject.PostContent;
import com.nongthinh.post_service.domain.post.valueobject.PostId;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostTypeId;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
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
class PostEndpointUseCasesTest {
    private static final UUID AUTHOR_ID = UUID.fromString("83b339ee-23b8-47bd-81d6-d37f81529789");
    private static final UUID POST_ID = UUID.fromString("cd68bb20-dcd5-4659-8712-45f4ea8550cf");
    private static final UUID TYPE_ID = UUID.fromString("27d58caf-626e-4965-ab0a-d5a7ac88a939");
    private static final UUID FILE_ID = UUID.fromString("b387bf49-53a7-45c2-8830-d188efa9f14c");
    private static final UUID MEDIA_ID = UUID.fromString("52955509-4cce-48d0-bf7d-7890cc9d5c69");
    private static final UUID CROP_TYPE_ID = UUID.fromString("a0de07cf-af5f-4f11-ab36-af622e431d7f");
    private static final UUID HISTORY_ID = UUID.fromString("deab8ba0-47c8-4693-883b-402552a1f0aa");
    private static final Instant NOW = Instant.parse("2026-08-24T00:00:00Z");

    @Mock private PostRepository postRepository;
    @Mock private PostTypeRepository postTypeRepository;
    @Mock private PostTopicRepository postTopicRepository;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private FileQuery fileQuery;
    @Mock private CropTypeQuery cropTypeQuery;
    @Mock private PostHistoryRepository historyRepository;
    @Mock private IdGenerator idGenerator;
    @Mock private ClockProvider clockProvider;
    private PostUseCaseSupport support;
    private PostHistoryRecorder historyRecorder;

    @BeforeEach
    void setUp() {
        support = new PostUseCaseSupport(
                postRepository, postTypeRepository, postTopicRepository,
                currentUserProvider, fileQuery, cropTypeQuery,
                new PostLimitsProperties(2_000, 10, 100),
                new PostFeaturesProperties(Set.of(PostVisibility.PUBLIC), Set.of(MediaType.IMAGE))
        );
        historyRecorder = new PostHistoryRecorder(historyRepository, idGenerator);
    }

    @Test
    void createsPublishedPostForCurrentUserAndAppendsHistory() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(AUTHOR_ID);
        when(postTypeRepository.findById(TYPE_ID)).thenReturn(Optional.of(postType()));
        when(idGenerator.generate()).thenReturn(POST_ID, HISTORY_ID);
        when(clockProvider.now()).thenReturn(NOW);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.append(any(PostHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var view = new CreatePostUseCaseImpl(
                postRepository, support, historyRecorder, idGenerator, clockProvider)
                .execute(new CreatePostCommand(
                        TYPE_ID, null, "  Kinh nghiệm trồng lúa  ", "  Cần Thơ  ",
                        PostVisibility.PUBLIC, PostStatus.PUBLISHED, Set.of()));

        assertThat(view.id()).isEqualTo(POST_ID);
        assertThat(view.authorUserId()).isEqualTo(AUTHOR_ID);
        assertThat(view.content()).isEqualTo("Kinh nghiệm trồng lúa");
        assertThat(view.status()).isEqualTo(PostStatus.PUBLISHED);
        verify(historyRepository).append(any(PostHistory.class));
    }

    @Test
    void createsPublishedPostWithoutPostType() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(AUTHOR_ID);
        when(idGenerator.generate()).thenReturn(POST_ID, HISTORY_ID);
        when(clockProvider.now()).thenReturn(NOW);
        when(postRepository.save(any(Post.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.append(any(PostHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var view = new CreatePostUseCaseImpl(
                postRepository, support, historyRecorder, idGenerator, clockProvider)
                .execute(new CreatePostCommand(
                        null, null, "Bài viết chung", null, PostVisibility.PUBLIC,
                        PostStatus.PUBLISHED, Set.of()));

        assertThat(view.postTypeId()).isNull();
        verifyNoInteractions(postTypeRepository);
        verify(historyRepository).append(any(PostHistory.class));
    }

    @Test
    void createsMediaFromTrustedFileMetadata() {
        Post post = publishedPost();
        when(currentUserProvider.getCurrentUserId()).thenReturn(AUTHOR_ID);
        when(postRepository.findByIdForUpdate(new PostId(POST_ID))).thenReturn(Optional.of(post));
        when(fileQuery.getById(FILE_ID)).thenReturn(file(AUTHOR_ID));
        when(idGenerator.generate()).thenReturn(MEDIA_ID, HISTORY_ID);
        when(clockProvider.now()).thenReturn(NOW.plusSeconds(1));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.append(any(PostHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var view = new CreatePostMediaUseCaseImpl(
                postRepository, support, historyRecorder, idGenerator, clockProvider)
                .execute(POST_ID, new CreatePostMediaCommand(FILE_ID, 0, "Ruộng lúa"));

        assertThat(view.id()).isEqualTo(MEDIA_ID);
        assertThat(view.mediaUrl()).isEqualTo("https://files.example.test/public/" + FILE_ID);
        assertThat(post.getMedia()).hasSize(1);
        verify(postRepository).save(post);
    }

    @Test
    void rejectsPostWhenAnyCropTypeIsMissingOrInactive() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(AUTHOR_ID);
        when(postTypeRepository.findById(TYPE_ID)).thenReturn(Optional.of(postType()));
        when(cropTypeQuery.findActiveIds(Set.of(CROP_TYPE_ID))).thenReturn(Set.of());
        when(idGenerator.generate()).thenReturn(POST_ID);
        when(clockProvider.now()).thenReturn(NOW);

        assertThatThrownBy(() -> new CreatePostUseCaseImpl(
                postRepository, support, historyRecorder, idGenerator, clockProvider)
                .execute(new CreatePostCommand(
                        TYPE_ID, null, "Ná»™i dung", null, PostVisibility.PUBLIC,
                        PostStatus.PUBLISHED, Set.of(CROP_TYPE_ID))))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getErrorCode())
                                .isEqualTo(ErrorCode.POST_CROP_TYPE_NOT_FOUND));

        verify(postRepository, never()).save(any());
    }

    @Test
    void preservesExistingCropReferenceWithoutRemoteRevalidation() {
        assertThat(support.cropTypeIds(
                Set.of(CROP_TYPE_ID), Set.of(new CropTypeId(CROP_TYPE_ID))))
                .containsExactly(new CropTypeId(CROP_TYPE_ID));

        verifyNoInteractions(cropTypeQuery);
    }

    @Test
    void rejectsMediaOwnedByAnotherUser() {
        Post post = publishedPost();
        when(currentUserProvider.getCurrentUserId()).thenReturn(AUTHOR_ID);
        when(postRepository.findByIdForUpdate(new PostId(POST_ID))).thenReturn(Optional.of(post));
        when(fileQuery.getById(FILE_ID)).thenReturn(file(UUID.randomUUID()));

        assertThatThrownBy(() -> new CreatePostMediaUseCaseImpl(
                postRepository, support, historyRecorder, idGenerator, clockProvider)
                .execute(POST_ID, new CreatePostMediaCommand(FILE_ID, 0, null)))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.POST_MEDIA_FILE_INVALID));

        verify(postRepository, never()).save(any());
    }

    private static PostType postType() {
        return PostType.create(TYPE_ID, "EXPERIENCE", "Kinh nghiệm", null, 10, NOW);
    }

    private static Post publishedPost() {
        return Post.createPublished(
                new PostId(POST_ID), new AuthorUserId(AUTHOR_ID), new PostTypeId(TYPE_ID),
                null, new PostContent("Nội dung"), null, PostVisibility.PUBLIC,
                List.of(), Set.of(), NOW, 10
        );
    }

    private static FileMetadata file(UUID ownerId) {
        return new FileMetadata(
                FILE_ID, ownerId, "POST_IMAGE", "image/jpeg", 10_000,
                "https://files.example.test/public/" + FILE_ID,
                "PUBLIC", "UPLOADED"
        );
    }
}
