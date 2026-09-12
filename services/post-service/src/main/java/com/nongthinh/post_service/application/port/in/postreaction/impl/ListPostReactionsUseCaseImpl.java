package com.nongthinh.post_service.application.port.in.postreaction.impl;

import com.nongthinh.post_service.application.port.in.postreaction.ListPostReactionsUseCase;
import com.nongthinh.post_service.application.port.out.repository.ReactionRepository;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostReactionView;
import com.nongthinh.post_service.domain.interaction.valueobject.ReactionType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListPostReactionsUseCaseImpl implements ListPostReactionsUseCase {
    private final ReactionRepository repository;
    private final PostUseCaseSupport postSupport;

    @Override
    public PageView<PostReactionView> execute(UUID postId, ReactionType reactionType, int page, int size) {
        postSupport.requireInteractablePost(postId);
        postSupport.validatePage(page, size);
        var result = repository.findByPostId(postId, reactionType, page, size);
        return new PageView<>(result.items().stream().map(PostReactionView::from).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages(), result.hasNext());
    }
}
