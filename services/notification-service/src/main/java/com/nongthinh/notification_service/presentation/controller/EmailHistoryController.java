package com.nongthinh.notification_service.presentation.controller;

import com.nongthinh.notification_service.application.port.in.emailhistory.GetAllEmailHistory;
import com.nongthinh.notification_service.application.port.in.emailhistory.GetEmailHistoryByPurposeId;
import com.nongthinh.notification_service.application.port.in.emailhistory.GetEmailHistoryByTemplateId;
import com.nongthinh.notification_service.application.port.in.emailhistory.GetEmailHistoryByUserId;
import com.nongthinh.notification_service.application.view.EmailHistoryView;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("email-history")
public class EmailHistoryController {

    private final GetEmailHistoryByUserId getEmailHistoryByUserId;
    private final GetEmailHistoryByTemplateId getEmailHistoryByTemplateId;
    private final GetEmailHistoryByPurposeId getEmailHistoryByPurposeId;
    private final GetAllEmailHistory getAllEmailHistory;

    @GetMapping
    @PreAuthorize("hasAuthority('notification:email:manage')")
    public List<EmailHistoryView> getAllEmailHistory() {
        return getAllEmailHistory.execute();
    }

    @GetMapping("users/{userId}")
    @PreAuthorize("hasAuthority('notification:email:manage')")
    public List<EmailHistoryView> getEmailHistoryByUserId(@PathVariable UUID userId) {
        return getEmailHistoryByUserId.execute(userId);
    }

    @GetMapping("templates/{templateId}")
    @PreAuthorize("hasAuthority('notification:email:manage')")
    public List<EmailHistoryView> getEmailHistoryByTemplateId(@PathVariable Long templateId) {
        return getEmailHistoryByTemplateId.execute(templateId);
    }

    @GetMapping("purposes/{purposeId}")
    @PreAuthorize("hasAuthority('notification:email:manage')")
    public List<EmailHistoryView> getEmailHistoryByPurposeId(@PathVariable Long purposeId) {
        return getEmailHistoryByPurposeId.execute(purposeId);
    }
}
