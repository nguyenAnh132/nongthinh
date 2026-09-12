package com.nongthinh.post_service.application.model;

import java.util.List;

public record CommentPage(
        List<CommentThread> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public CommentPage {
        items = List.copyOf(items);
    }
}
