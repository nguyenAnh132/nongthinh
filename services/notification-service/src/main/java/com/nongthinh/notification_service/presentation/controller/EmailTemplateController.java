package com.nongthinh.notification_service.presentation.controller;

import com.nongthinh.notification_service.application.port.in.template.ActivateEmailTemplateUseCase;
import com.nongthinh.notification_service.application.port.in.template.GetEmailTemplateUseCase;
import com.nongthinh.notification_service.application.port.in.template.PreviewEmailTemplateByIdUseCase;
import com.nongthinh.notification_service.application.port.in.template.UpdateEmailTemplateUseCase;
import com.nongthinh.notification_service.application.view.EmailTemplateView;
import com.nongthinh.notification_service.application.view.PreviewEmailTemplateView;
import com.nongthinh.notification_service.common.response.ApiResponse;
import com.nongthinh.notification_service.presentation.dto.request.UpdateEmailTemplateRequest;
import com.nongthinh.notification_service.presentation.mapper.EmailTemplateMapper;
import jakarta.validation.Valid;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("email-templates")
@RequiredArgsConstructor
public class EmailTemplateController {

    private final GetEmailTemplateUseCase getEmailTemplateUseCase;
    private final UpdateEmailTemplateUseCase updateEmailTemplateUseCase;
    private final ActivateEmailTemplateUseCase activateEmailTemplateUseCase;
    private final PreviewEmailTemplateByIdUseCase previewEmailTemplateByIdUseCase;
    private final EmailTemplateMapper emailTemplateMapper;

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('notification:email:manage')")
    public ResponseEntity<ApiResponse<EmailTemplateView>> getTemplateById(@PathVariable Long id) {
        EmailTemplateView emailTemplateView = getEmailTemplateUseCase.execute(id);

        return ResponseEntity.ok(ApiResponse.<EmailTemplateView>builder()
                .message("Email template retrieved successfully")
                .result(Optional.of(emailTemplateView))
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('notification:email:manage')")
    public ResponseEntity<ApiResponse<EmailTemplateView>> updateTemplate(
            @PathVariable Long id,
            @RequestBody @Valid UpdateEmailTemplateRequest request
    ) {
        EmailTemplateView template = updateEmailTemplateUseCase.execute(
                id,
                emailTemplateMapper.toUpdateCommand(request));

        return ResponseEntity.ok(ApiResponse.<EmailTemplateView>builder()
                .message("Email template updated successfully")
                .result(Optional.of(template))
                .build());
    }

    @PatchMapping("/{id}/activation")
    @PreAuthorize("hasAuthority('notification:email:manage')")
    public ResponseEntity<ApiResponse<Void>> activateTemplate(@PathVariable Long id) {
        activateEmailTemplateUseCase.execute(id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Email template activated successfully")
                .build());
    }

    @PostMapping("/{id}/preview")
    @PreAuthorize("hasAuthority('notification:email:manage')")
    public ResponseEntity<ApiResponse<PreviewEmailTemplateView>> previewTemplate(@PathVariable Long id) {
        PreviewEmailTemplateView content = previewEmailTemplateByIdUseCase.execute(id);

        return ResponseEntity.ok(ApiResponse.<PreviewEmailTemplateView>builder()
                .message("Email template preview generated successfully")
                .result(Optional.of(content))
                .build());
    }
}
