package com.nongthinh.post_service.application.port.in.postbookmark.impl;

import com.nongthinh.post_service.application.port.in.postbookmark.SavePostBookmarkUseCase;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.CurrentUserProvider;
import com.nongthinh.post_service.application.port.out.repository.BookmarkRepository;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PostBookmarkView;
import com.nongthinh.post_service.domain.interaction.Bookmark;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SavePostBookmarkUseCaseImpl implements SavePostBookmarkUseCase {
    private final BookmarkRepository repository;
    private final PostUseCaseSupport postSupport;
    private final CurrentUserProvider currentUserProvider;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public PostBookmarkView execute(UUID postId) {
        postSupport.requireInteractablePost(postId);
        Bookmark bookmark = new Bookmark(
                postId, currentUserProvider.getCurrentUserId(), clockProvider.now());
        return PostBookmarkView.from(repository.save(bookmark));
    }
}
