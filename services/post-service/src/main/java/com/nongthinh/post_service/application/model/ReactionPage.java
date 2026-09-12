package com.nongthinh.post_service.application.model;

import com.nongthinh.post_service.domain.interaction.Reaction;
import java.util.List;

public record ReactionPage(
        List<Reaction> items, int page, int size, long totalElements, int totalPages, boolean hasNext
) {
    public ReactionPage {
        items = List.copyOf(items);
    }
}
