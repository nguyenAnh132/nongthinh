package com.nongthinh.post_service.application.port.in.posthistory.impl;

import com.nongthinh.post_service.application.model.PostHistoryFilter;
import com.nongthinh.post_service.application.model.PostHistoryPage;
import com.nongthinh.post_service.application.port.in.posthistory.ListPostHistoriesUseCase;
import com.nongthinh.post_service.application.port.out.repository.PostHistoryRepository;
import com.nongthinh.post_service.application.service.PostHistoryUseCaseSupport;
import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostHistoryView;
import com.nongthinh.post_service.domain.history.valueobject.HistoryActorType;
import com.nongthinh.post_service.domain.history.valueobject.PostHistoryAction;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListPostHistoriesUseCaseImpl implements ListPostHistoriesUseCase {
    private final PostHistoryRepository repository;
    private final PostHistoryUseCaseSupport support;

    @Override
    public PageView<PostHistoryView> execute(
            UUID postId,
            UUID postAuthorUserId,
            UUID actorUserId,
            HistoryActorType actorType,
            PostHistoryAction action,
            int page,
            int size
    ) {
        support.validatePage(page, size);
        PostHistoryPage result = repository.findAll(
                new PostHistoryFilter(
                        postId,
                        postAuthorUserId,
                        actorUserId,
                        actorType,
                        action
                ),
                page,
                size
        );
        return new PageView<>(
                result.items().stream().map(PostHistoryView::from).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext()
        );
    }
}
