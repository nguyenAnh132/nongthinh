package com.nongthinh.post_service.application.port.in.postcomment.impl;

import com.nongthinh.post_service.application.model.CommentPage;
import com.nongthinh.post_service.application.port.in.postcomment.ListPostCommentsUseCase;
import com.nongthinh.post_service.application.port.out.repository.CommentRepository;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostCommentThreadView;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListPostCommentsUseCaseImpl implements ListPostCommentsUseCase {
    private final CommentRepository repository;
    private final PostUseCaseSupport postSupport;

    @Override
    public PageView<PostCommentThreadView> execute(UUID postId, int page, int size) {
        postSupport.requireInteractablePost(postId);
        postSupport.validatePage(page, size);
        CommentPage result = repository.findPublishedThreads(postId, page, size);
        return new PageView<>(
                result.items().stream().map(PostCommentThreadView::from).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages(),
                result.hasNext()
        );
    }
}
