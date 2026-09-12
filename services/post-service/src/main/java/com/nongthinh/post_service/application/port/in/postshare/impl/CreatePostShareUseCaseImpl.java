package com.nongthinh.post_service.application.port.in.postshare.impl;

import com.nongthinh.post_service.application.port.in.postshare.CreatePostShareUseCase;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.CurrentUserProvider;
import com.nongthinh.post_service.application.port.out.IdGenerator;
import com.nongthinh.post_service.application.port.out.repository.PostShareRepository;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PostShareView;
import com.nongthinh.post_service.domain.interaction.PostShare;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreatePostShareUseCaseImpl implements CreatePostShareUseCase {
    private final PostShareRepository repository;
    private final PostUseCaseSupport postSupport;
    private final CurrentUserProvider currentUserProvider;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public PostShareView execute(UUID postId) {
        postSupport.requireInteractablePost(postId);
        PostShare share = new PostShare(
                idGenerator.generate(), postId, currentUserProvider.getCurrentUserId(),
                clockProvider.now());
        return PostShareView.from(repository.save(share));
    }
}
