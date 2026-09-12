package com.nongthinh.post_service.application.port.in.postmedia.impl;

import com.nongthinh.post_service.application.command.UpdatePostMediaCommand;
import com.nongthinh.post_service.application.model.FileMetadata;
import com.nongthinh.post_service.application.port.in.postmedia.UpdatePostMediaUseCase;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.repository.PostRepository;
import com.nongthinh.post_service.application.service.PostHistoryRecorder;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PostMediaView;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.nongthinh.post_service.domain.history.valueobject.PostHistoryAction;
import com.nongthinh.post_service.domain.post.Post;
import com.nongthinh.post_service.domain.post.PostMedia;
import com.nongthinh.post_service.domain.post.valueobject.AuthorUserId;
import com.nongthinh.post_service.domain.post.valueobject.FileId;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePostMediaUseCaseImpl implements UpdatePostMediaUseCase {
    private final PostRepository repository;
    private final PostUseCaseSupport support;
    private final PostHistoryRecorder historyRecorder;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public PostMediaView execute(UUID postId, UUID mediaId, UpdatePostMediaCommand command) {
        if (command == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_PARAMETER);
        }
        Post post = support.requirePostForUpdate(postId);
        AuthorUserId actor = support.currentAuthor();
        support.assertOwner(post, actor);
        support.assertEditable(post);
        PostMedia current = support.requireMedia(post, mediaId);
        support.assertMediaSlotAvailable(post, mediaId, command.fileId(), command.displayOrder());
        FileMetadata file = support.requireValidPostMedia(command.fileId(), actor.value());
        PostMedia replacement = current.updateSnapshot(
                new FileId(file.id()), file.mediaType(), file.publicUrl(), file.contentType(),
                null, null, file.sizeBytes(), command.displayOrder(), command.caption()
        );
        PostStatus previousStatus = post.getStatus();
        PostVisibility previousVisibility = post.getVisibility();
        Instant now = clockProvider.now();
        post.replaceMedia(actor, mediaId, replacement, support.maxMedia(), now);
        Post saved = repository.save(post);
        historyRecorder.record(saved, actor.value(), PostHistoryAction.UPDATED,
                previousStatus, previousVisibility, now);
        return PostMediaView.from(replacement);
    }
}
