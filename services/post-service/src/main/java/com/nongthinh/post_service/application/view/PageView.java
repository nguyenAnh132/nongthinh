package com.nongthinh.post_service.application.view;

import java.util.List;

public record PageView<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public PageView {
        items = List.copyOf(items);
    }
}
