package com.nongthinh.post_service.application.service;

import com.nongthinh.post_service.application.port.out.repository.PostHistoryRepository;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.configuration.PostLimitsProperties;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.nongthinh.post_service.domain.history.PostHistory;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostHistoryUseCaseSupport {
    private final PostHistoryRepository repository;
    private final PostLimitsProperties limits;

    public PostHistory requireHistory(UUID historyId) {
        return repository.findById(historyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_HISTORY_NOT_FOUND));
    }

    public void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > limits.pageSize()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_PARAMETER);
        }
    }
}
