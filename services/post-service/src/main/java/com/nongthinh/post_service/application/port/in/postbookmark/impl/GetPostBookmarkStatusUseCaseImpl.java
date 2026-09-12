package com.nongthinh.post_service.application.port.in.postbookmark.impl;

import com.nongthinh.post_service.application.port.in.postbookmark.GetPostBookmarkStatusUseCase;
import com.nongthinh.post_service.application.port.out.CurrentUserProvider;
import com.nongthinh.post_service.application.port.out.repository.BookmarkRepository;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PostBookmarkStatusView;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPostBookmarkStatusUseCaseImpl implements GetPostBookmarkStatusUseCase {
    private final BookmarkRepository repository;
    private final PostUseCaseSupport postSupport;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public PostBookmarkStatusView execute(UUID postId) {
        postSupport.requireInteractablePost(postId);
        UUID userId = currentUserProvider.getCurrentUserId();
        return new PostBookmarkStatusView(postId, repository.exists(postId, userId));
    }
}
