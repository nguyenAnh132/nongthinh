package com.nongthinh.notification_service.presentation.controller;

import com.nongthinh.notification_service.application.port.in.purpose.GetEmailTemplatePurposeUseCase;
import com.nongthinh.notification_service.application.port.in.purpose.ListEmailTemplatePurposesUseCase;
import com.nongthinh.notification_service.application.port.in.template.CreateEmailTemplateUseCase;
import com.nongthinh.notification_service.application.port.in.template.ListEmailTemplatesByPurposeIdUseCase;
import com.nongthinh.notification_service.application.port.in.template.PreviewDraftEmailTemplateUseCase;
import com.nongthinh.notification_service.application.view.EmailTemplatePurposeView;
import com.nongthinh.notification_service.application.view.EmailTemplateView;
import com.nongthinh.notification_service.application.view.PreviewEmailTemplateView;
import com.nongthinh.notification_service.common.response.ApiResponse;
import com.nongthinh.notification_service.presentation.dto.request.CreateEmailTemplateRequest;
import com.nongthinh.notification_service.presentation.dto.request.PreviewEmailTemplateRequest;
import com.nongthinh.notification_service.presentation.mapper.EmailTemplateMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("email-template-purposes")
@RequiredArgsConstructor
public class EmailTemplatePurposeController {

    private final ListEmailTemplatePurposesUseCase listEmailTemplatePurposesUseCase;
    private final GetEmailTemplatePurposeUseCase getEmailTemplatePurposeUseCase;
    private final ListEmailTemplatesByPurposeIdUseCase listEmailTemplatesByPurposeIdUseCase;
    private final CreateEmailTemplateUseCase createEmailTemplateUseCase;
    private final PreviewDraftEmailTemplateUseCase previewDraftEmailTemplateUseCase;
    private final EmailTemplateMapper emailTemplateMapper;

    @GetMapping
    @PreAuthorize("hasAuthority('notification:email:manage')")
    public ResponseEntity<ApiResponse<List<EmailTemplatePurposeView>>> getAllPurposes() {
        List<EmailTemplatePurposeView> data = listEmailTemplatePurposesUseCase.execute();

        return ResponseEntity.ok(ApiResponse.<List<EmailTemplatePurposeView>>builder()
                .message("Email template purposes retrieved successfully")
                .result(Optional.of(data))
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('notification:email:manage')")
    public ResponseEntity<ApiResponse<EmailTemplatePurposeView>> getPurposeById(@PathVariable Long id) {
        EmailTemplatePurposeView data = getEmailTemplatePurposeUseCase.execute(id);

        return ResponseEntity.ok(ApiResponse.<EmailTemplatePurposeView>builder()
                .message("Email template purpose retrieved successfully")
                .result(Optional.of(data))
                .build());
    }

    @GetMapping("/{id}/email-templates")
    @PreAuthorize("hasAuthority('notification:email:manage')")
    public ResponseEntity<ApiResponse<List<EmailTemplateView>>> getTemplatesByPurposeId(
            @PathVariable Long id
    ) {
        List<EmailTemplateView> data = listEmailTemplatesByPurposeIdUseCase.execute(id);

        return ResponseEntity.ok(ApiResponse.<List<EmailTemplateView>>builder()
                .message("Email templates retrieved successfully")
                .result(Optional.of(data))
                .build());
    }

    @PostMapping("/{id}/email-templates")
    @PreAuthorize("hasAuthority('notification:email:manage')")
    public ResponseEntity<ApiResponse<EmailTemplateView>> createTemplate(
            @PathVariable Long id,
            @RequestBody @Valid CreateEmailTemplateRequest request
    ) {
        EmailTemplateView newTemplate = createEmailTemplateUseCase.execute(
                id,
                emailTemplateMapper.toCreateCommand(request));

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<EmailTemplateView>builder()
                .message("Email template created successfully")
                .result(Optional.of(newTemplate))
                .build());
    }

    @PostMapping("/{id}/email-templates/preview")
    @PreAuthorize("hasAuthority('notification:email:manage')")
    public ResponseEntity<ApiResponse<PreviewEmailTemplateView>> previewDraftTemplate(
            @PathVariable Long id,
            @RequestBody @Valid PreviewEmailTemplateRequest request
    ) {
        PreviewEmailTemplateView content = previewDraftEmailTemplateUseCase.execute(
                id,
                emailTemplateMapper.toPreviewCommand(request));

        return ResponseEntity.ok(ApiResponse.<PreviewEmailTemplateView>builder()
                .message("Email template preview generated successfully")
                .result(Optional.of(content))
                .build());
    }
}
