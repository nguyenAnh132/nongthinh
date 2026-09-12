package com.nongthinh.post_service.application.port.in.postmedia.impl;

import com.nongthinh.post_service.application.port.in.postmedia.ListPostMediaUseCase;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PostMediaView;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListPostMediaUseCaseImpl implements ListPostMediaUseCase {
    private final PostUseCaseSupport support;

    @Override
    public List<PostMediaView> execute(UUID postId) {
        return support.requireReadablePost(postId).getMedia().stream()
                .sorted(Comparator.comparingInt(item -> item.getDisplayOrder()))
                .map(PostMediaView::from)
                .toList();
    }
}
