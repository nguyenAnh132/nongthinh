package com.nongthinh.notification_service.presentation.mapper;

import com.nongthinh.notification_service.application.command.template.CreateEmailTemplateCommand;
import com.nongthinh.notification_service.application.command.template.PreviewEmailTemplateCommand;
import com.nongthinh.notification_service.application.command.template.UpdateEmailTemplateCommand;
import com.nongthinh.notification_service.presentation.dto.request.CreateEmailTemplateRequest;
import com.nongthinh.notification_service.presentation.dto.request.PreviewEmailTemplateRequest;
import com.nongthinh.notification_service.presentation.dto.request.UpdateEmailTemplateRequest;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplateMapper {

    public CreateEmailTemplateCommand toCreateCommand(CreateEmailTemplateRequest request) {
        return new CreateEmailTemplateCommand(
                request.name(),
                request.description(),
                request.subject(),
                request.htmlContent(),
                request.textContent());
    }

    public UpdateEmailTemplateCommand toUpdateCommand(UpdateEmailTemplateRequest request) {
        return new UpdateEmailTemplateCommand(
                request.name(),
                request.description(),
                request.subject(),
                request.htmlContent(),
                request.textContent());
    }

    public PreviewEmailTemplateCommand toPreviewCommand(PreviewEmailTemplateRequest request) {
        return new PreviewEmailTemplateCommand(
                request.subject(),
                request.htmlContent(),
                request.textContent());
    }
}
