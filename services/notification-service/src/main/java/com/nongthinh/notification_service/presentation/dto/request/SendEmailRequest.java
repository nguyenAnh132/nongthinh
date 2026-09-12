package com.nongthinh.notification_service.presentation.dto.request;

public record SendEmailRequest(
    String to,
    String subject,
    String content
) {

}
