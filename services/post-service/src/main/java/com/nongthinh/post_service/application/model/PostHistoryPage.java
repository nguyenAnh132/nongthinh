package com.nongthinh.post_service.application.model;

import com.nongthinh.post_service.domain.history.PostHistory;
import java.util.List;

public record PostHistoryPage(
        List<PostHistory> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public PostHistoryPage {
        items = List.copyOf(items);
    }
}
