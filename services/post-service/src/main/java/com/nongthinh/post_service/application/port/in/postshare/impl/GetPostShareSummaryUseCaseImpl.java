package com.nongthinh.post_service.application.port.in.postshare.impl;

import com.nongthinh.post_service.application.port.in.postshare.GetPostShareSummaryUseCase;
import com.nongthinh.post_service.application.port.out.repository.PostShareRepository;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PostShareSummaryView;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPostShareSummaryUseCaseImpl implements GetPostShareSummaryUseCase {
    private final PostShareRepository repository;
    private final PostUseCaseSupport postSupport;

    @Override
    public PostShareSummaryView execute(UUID postId) {
        postSupport.requireInteractablePost(postId);
        return new PostShareSummaryView(postId, repository.countByPostId(postId));
    }
}
