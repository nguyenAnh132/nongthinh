package com.nongthinh.notification_service.application.port.in.emailhistory.impl;

import org.springframework.stereotype.Service;
import com.nongthinh.notification_service.application.port.in.emailhistory.GetEmailHistoryById;
import com.nongthinh.notification_service.application.view.EmailHistoryView;
import com.nongthinh.notification_service.common.exception.ErrorCode;
import com.nongthinh.notification_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import com.nongthinh.notification_service.application.port.out.repository.EmailHistoryRepository;

@Service
@RequiredArgsConstructor
public class GetEmailHistoryByIdImpl implements GetEmailHistoryById {

    private final EmailHistoryRepository emailHistoryRepository;

    @Override
    public EmailHistoryView execute(Long id) {
        return emailHistoryRepository.findById(id)
            .map(EmailHistoryView::fromDomain)
            .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_HISTORY_NOT_FOUND));
    }
}
