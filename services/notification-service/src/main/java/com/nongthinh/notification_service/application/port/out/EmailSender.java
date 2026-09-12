package com.nongthinh.notification_service.application.port.out;

public interface EmailSender {

    void sendEmail(String to, String subject, String content);

}
