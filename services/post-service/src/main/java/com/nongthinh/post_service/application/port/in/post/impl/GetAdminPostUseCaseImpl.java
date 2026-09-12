package com.nongthinh.post_service.application.port.in.post.impl;

import com.nongthinh.post_service.application.port.in.post.GetAdminPostUseCase;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PostView;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetAdminPostUseCaseImpl implements GetAdminPostUseCase {
    private final PostUseCaseSupport support;

    @Override
    public PostView execute(UUID postId) {
        return PostView.from(support.requirePostIncludingDeleted(postId));
    }
}
