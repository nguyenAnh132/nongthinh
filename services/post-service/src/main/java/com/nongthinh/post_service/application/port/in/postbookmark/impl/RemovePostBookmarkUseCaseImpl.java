package com.nongthinh.post_service.application.port.in.postbookmark.impl;

import com.nongthinh.post_service.application.port.in.postbookmark.RemovePostBookmarkUseCase;
import com.nongthinh.post_service.application.port.out.CurrentUserProvider;
import com.nongthinh.post_service.application.port.out.repository.BookmarkRepository;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemovePostBookmarkUseCaseImpl implements RemovePostBookmarkUseCase {
    private final BookmarkRepository repository;
    private final PostUseCaseSupport postSupport;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public void execute(UUID postId) {
        postSupport.requireInteractablePost(postId);
        repository.delete(postId, currentUserProvider.getCurrentUserId());
    }
}
