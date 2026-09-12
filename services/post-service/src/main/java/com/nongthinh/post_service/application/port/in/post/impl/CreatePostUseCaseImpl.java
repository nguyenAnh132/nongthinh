package com.nongthinh.post_service.application.port.in.post.impl;

import com.nongthinh.post_service.application.command.CreatePostCommand;
import com.nongthinh.post_service.application.port.in.post.CreatePostUseCase;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.IdGenerator;
import com.nongthinh.post_service.application.port.out.repository.PostRepository;
import com.nongthinh.post_service.application.service.PostHistoryRecorder;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PostView;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.nongthinh.post_service.domain.history.valueobject.PostHistoryAction;
import com.nongthinh.post_service.domain.post.Post;
import com.nongthinh.post_service.domain.post.valueobject.AuthorUserId;
import com.nongthinh.post_service.domain.post.valueobject.PostId;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreatePostUseCaseImpl implements CreatePostUseCase {
    private final PostRepository repository;
    private final PostUseCaseSupport support;
    private final PostHistoryRecorder historyRecorder;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public PostView execute(CreatePostCommand command) {
        if (command == null || (command.status() != PostStatus.DRAFT
                && command.status() != PostStatus.PUBLISHED)) {
            throw new BusinessException(ErrorCode.POST_STATUS_INVALID);
        }
        AuthorUserId author = support.currentAuthor();
        Instant now = clockProvider.now();
        Post post = command.status() == PostStatus.DRAFT
                ? Post.createDraft(
                        new PostId(idGenerator.generate()), author,
                        support.activePostTypeOrNull(command.postTypeId()),
                        support.requireActiveTopic(command.topicId()),
                        support.content(command.content()), command.locationText(),
                        support.visibility(command.visibility()), List.of(),
                        support.cropTypeIds(command.cropTypeIds()), now, support.maxMedia())
                : Post.createPublished(
                        new PostId(idGenerator.generate()), author,
                        support.activePostTypeOrNull(command.postTypeId()),
                        support.requireActiveTopic(command.topicId()),
                        support.content(command.content()), command.locationText(),
                        support.visibility(command.visibility()), List.of(),
                        support.cropTypeIds(command.cropTypeIds()), now, support.maxMedia());
        Post saved = repository.save(post);
        historyRecorder.record(saved, author.value(), PostHistoryAction.CREATED, null, null, now);
        return PostView.from(saved);
    }
}
