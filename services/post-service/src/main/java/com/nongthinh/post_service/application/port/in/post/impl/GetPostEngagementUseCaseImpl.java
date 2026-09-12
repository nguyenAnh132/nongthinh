package com.nongthinh.post_service.application.port.in.post.impl;
import com.nongthinh.post_service.application.port.in.post.GetPostEngagementUseCase;
import com.nongthinh.post_service.application.port.out.CurrentUserProvider;
import com.nongthinh.post_service.application.port.out.PostEngagementRepository;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PostEngagementView;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class GetPostEngagementUseCaseImpl implements GetPostEngagementUseCase {
    private final PostUseCaseSupport support;
    private final PostEngagementRepository repository;
    private final CurrentUserProvider currentUser;
    public PostEngagementView execute(UUID postId) {
        support.requireInteractablePost(postId);
        return repository.snapshot(postId, currentUser.findCurrentUserId().orElse(null));
    }
}
