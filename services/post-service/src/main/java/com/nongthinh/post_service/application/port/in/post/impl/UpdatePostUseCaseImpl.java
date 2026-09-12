package com.nongthinh.post_service.application.port.in.post.impl;

import com.nongthinh.post_service.application.command.UpdatePostCommand;
import com.nongthinh.post_service.application.port.in.post.UpdatePostUseCase;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.repository.PostRepository;
import com.nongthinh.post_service.application.service.PostHistoryRecorder;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PostView;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.nongthinh.post_service.domain.history.valueobject.PostHistoryAction;
import com.nongthinh.post_service.domain.post.Post;
import com.nongthinh.post_service.domain.post.valueobject.AuthorUserId;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePostUseCaseImpl implements UpdatePostUseCase {
    private final PostRepository repository;
    private final PostUseCaseSupport support;
    private final PostHistoryRecorder historyRecorder;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public PostView execute(UUID id, UpdatePostCommand command) {
        if (command == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_PARAMETER);
        }
        Post post = support.requirePostForUpdate(id);
        AuthorUserId actor = support.currentAuthor();
        support.assertOwner(post, actor);
        support.assertEditable(post);
        PostStatus previousStatus = post.getStatus();
        PostVisibility previousVisibility = post.getVisibility();
        Instant now = clockProvider.now();
        post.edit(actor, support.activePostTypeOrNull(command.postTypeId()),
                support.requireActiveTopic(command.topicId()), support.content(command.content()),
                command.locationText(), support.visibility(command.visibility()), post.getMedia(),
                support.cropTypeIds(command.cropTypeIds(), post.getCropTypeIds()),
                support.maxMedia(), now);
        Post saved = repository.save(post);
        historyRecorder.record(saved, actor.value(), PostHistoryAction.UPDATED,
                previousStatus, previousVisibility, now);
        return PostView.from(saved);
    }
}
