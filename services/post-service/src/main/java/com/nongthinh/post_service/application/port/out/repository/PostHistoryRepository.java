package com.nongthinh.post_service.application.port.out.repository;

import com.nongthinh.post_service.application.model.PostHistoryFilter;
import com.nongthinh.post_service.application.model.PostHistoryPage;
import com.nongthinh.post_service.domain.history.PostHistory;
import java.util.Optional;
import java.util.UUID;

public interface PostHistoryRepository {
    PostHistory append(PostHistory history);
    Optional<PostHistory> findById(UUID id);
    PostHistoryPage findAll(PostHistoryFilter filter, int page, int size);
}
