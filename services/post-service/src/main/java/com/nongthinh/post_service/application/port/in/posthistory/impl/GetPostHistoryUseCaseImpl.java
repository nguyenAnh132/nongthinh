package com.nongthinh.post_service.application.port.in.posthistory.impl;

import com.nongthinh.post_service.application.port.in.posthistory.GetPostHistoryUseCase;
import com.nongthinh.post_service.application.service.PostHistoryUseCaseSupport;
import com.nongthinh.post_service.application.view.PostHistoryView;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPostHistoryUseCaseImpl implements GetPostHistoryUseCase {
    private final PostHistoryUseCaseSupport support;

    @Override
    public PostHistoryView execute(UUID historyId) {
        return PostHistoryView.from(support.requireHistory(historyId));
    }
}
