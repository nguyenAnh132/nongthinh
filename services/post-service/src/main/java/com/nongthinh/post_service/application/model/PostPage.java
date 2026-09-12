package com.nongthinh.post_service.application.model;

import com.nongthinh.post_service.domain.post.Post;
import java.util.List;

public record PostPage(
        List<Post> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public PostPage {
        items = List.copyOf(items);
    }
}
