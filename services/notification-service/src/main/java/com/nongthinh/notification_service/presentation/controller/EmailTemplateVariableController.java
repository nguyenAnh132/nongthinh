package com.nongthinh.notification_service.presentation.controller;

import com.nongthinh.notification_service.application.port.in.variable.ListEmailTemplateVariablesByPurposeIdUseCase;
import com.nongthinh.notification_service.application.view.EmailTemplateVariableView;
import com.nongthinh.notification_service.common.response.ApiResponse;
import com.nongthinh.notification_service.presentation.mapper.EmailTemplateMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("email-template-purposes")
@RequiredArgsConstructor
public class EmailTemplateVariableController {

    private final ListEmailTemplateVariablesByPurposeIdUseCase listEmailTemplateVariablesByPurposeIdUseCase;
    private final EmailTemplateMapper emailTemplateMapper;

    @GetMapping("/{id}/variables")
    @PreAuthorize("hasAuthority('notification:email:manage')")
    public ResponseEntity<ApiResponse<List<EmailTemplateVariableView>>> getVariablesByPurposeId(
            @PathVariable Long id
    ) {
        List<EmailTemplateVariableView> data = listEmailTemplateVariablesByPurposeIdUseCase.execute(id);

        return ResponseEntity.ok(ApiResponse.<List<EmailTemplateVariableView>>builder()
                .message("Email template variables retrieved successfully")
                .result(Optional.of(data))
                .build());
    }
}
