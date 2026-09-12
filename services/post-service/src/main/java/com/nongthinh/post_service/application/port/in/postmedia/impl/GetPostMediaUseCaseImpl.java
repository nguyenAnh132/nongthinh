package com.nongthinh.post_service.application.port.in.postmedia.impl;

import com.nongthinh.post_service.application.port.in.postmedia.GetPostMediaUseCase;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PostMediaView;
import com.nongthinh.post_service.domain.post.Post;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPostMediaUseCaseImpl implements GetPostMediaUseCase {
    private final PostUseCaseSupport support;

    @Override
    public PostMediaView execute(UUID postId, UUID mediaId) {
        Post post = support.requireReadablePost(postId);
        return PostMediaView.from(support.requireMedia(post, mediaId));
    }
}
