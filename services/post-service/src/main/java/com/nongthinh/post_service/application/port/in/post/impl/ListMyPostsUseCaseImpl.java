package com.nongthinh.post_service.application.port.in.post.impl;

import com.nongthinh.post_service.application.model.PostPage;
import com.nongthinh.post_service.application.port.in.post.ListMyPostsUseCase;
import com.nongthinh.post_service.application.port.out.CurrentUserProvider;
import com.nongthinh.post_service.application.port.out.repository.PostRepository;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostView;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListMyPostsUseCaseImpl implements ListMyPostsUseCase {
    private final PostRepository repository;
    private final CurrentUserProvider currentUserProvider;
    private final PostUseCaseSupport support;

    @Override
    public PageView<PostView> execute(PostStatus status, int page, int size) {
        support.validatePage(page, size);
        PostPage result = repository.findByAuthor(
                currentUserProvider.getCurrentUserId(), status, page, size);
        return new PageView<>(result.items().stream().map(PostView::from).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages(),
                result.hasNext());
    }
}
