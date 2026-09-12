package com.nongthinh.post_service.application.port.in.post.impl;

import com.nongthinh.post_service.application.model.PostPage;
import com.nongthinh.post_service.application.port.in.post.ListPublicPostsUseCase;
import com.nongthinh.post_service.application.port.out.repository.PostRepository;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostView;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListPublicPostsUseCaseImpl implements ListPublicPostsUseCase {
    private final PostRepository repository;
    private final PostUseCaseSupport support;

    @Override
    public PageView<PostView> execute(UUID postTypeId, UUID topicId, UUID cropTypeId,
                                      String keyword, int page, int size) {
        support.validatePage(page, size);
        PostPage result = repository.findPublic(
                postTypeId, topicId, cropTypeId, support.normalizeKeyword(keyword), page, size);
        return new PageView<>(result.items().stream().map(PostView::from).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages(),
                result.hasNext());
    }
}
