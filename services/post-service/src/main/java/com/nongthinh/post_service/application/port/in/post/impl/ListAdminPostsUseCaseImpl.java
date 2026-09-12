package com.nongthinh.post_service.application.port.in.post.impl;

import com.nongthinh.post_service.application.model.PostPage;
import com.nongthinh.post_service.application.port.in.post.ListAdminPostsUseCase;
import com.nongthinh.post_service.application.port.out.repository.PostRepository;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostView;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListAdminPostsUseCaseImpl implements ListAdminPostsUseCase {
    private final PostRepository repository;
    private final PostUseCaseSupport support;

    @Override
    public PageView<PostView> execute(UUID authorUserId, PostStatus status, String keyword,
                                      Instant from, Instant to, int page, int size) {
        support.validatePage(page, size);
        if (from != null && to != null && !from.isBefore(to)) {
            throw new IllegalArgumentException("from must be before to");
        }
        PostPage result = repository.findAllForAdmin(
                authorUserId,
                status,
                support.normalizeKeyword(keyword),
                from,
                to,
                page,
                size
        );
        return new PageView<>(
                result.items().stream().map(PostView::from).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext()
        );
    }
}
