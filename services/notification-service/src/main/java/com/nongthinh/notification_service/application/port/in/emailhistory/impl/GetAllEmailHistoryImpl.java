package com.nongthinh.notification_service.application.port.in.emailhistory.impl;

import org.springframework.stereotype.Service;
import com.nongthinh.notification_service.application.port.in.emailhistory.GetAllEmailHistory;
import com.nongthinh.notification_service.application.view.EmailHistoryView;
import com.nongthinh.notification_service.application.port.out.repository.EmailHistoryRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAllEmailHistoryImpl implements GetAllEmailHistory {

    private final EmailHistoryRepository emailHistoryRepository;

    @Override
    public List<EmailHistoryView> execute() {
        return emailHistoryRepository.findAll()
            .stream()
            .map(EmailHistoryView::fromDomain)
            .collect(Collectors.toList());
    }
}
