package com.nongthinh.post_service.application.port.in.postbookmark.impl;

import com.nongthinh.post_service.application.model.PostPage;
import com.nongthinh.post_service.application.port.in.postbookmark.ListMyBookmarkedPostsUseCase;
import com.nongthinh.post_service.application.port.out.CurrentUserProvider;
import com.nongthinh.post_service.application.port.out.repository.PostRepository;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListMyBookmarkedPostsUseCaseImpl implements ListMyBookmarkedPostsUseCase {
    private final PostRepository repository;
    private final CurrentUserProvider currentUserProvider;
    private final PostUseCaseSupport postSupport;

    @Override
    public PageView<PostView> execute(int page, int size) {
        postSupport.validatePage(page, size);
        PostPage result = repository.findBookmarkedByUser(
                currentUserProvider.getCurrentUserId(), page, size);
        return new PageView<>(result.items().stream().map(PostView::from).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages(),
                result.hasNext());
    }
}
