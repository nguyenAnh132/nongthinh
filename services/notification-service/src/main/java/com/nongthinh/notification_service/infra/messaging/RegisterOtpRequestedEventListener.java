package com.nongthinh.notification_service.infra.messaging;

import com.nongthinh.notification_service.application.command.SendRegisterOtpCommand;
import com.nongthinh.notification_service.application.event.RegisterOtpRequest;
import com.nongthinh.notification_service.application.port.in.sendemail.SendRegisterOtpUseCase;
import com.nongthinh.notification_service.application.port.out.EventDeserializer;
import com.nongthinh.notification_service.common.constant.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RegisterOtpRequestedEventListener {

    private final EventDeserializer eventDeserializer;
    private final SendRegisterOtpUseCase sendRegisterOtpUseCase;

    @KafkaListener(topics = KafkaTopics.REGISTER_OTP)
    public void consume(String message) {

        RegisterOtpRequest event = eventDeserializer.deserialize(message, RegisterOtpRequest.class);

        log.info(
                "Received RegisterOtpRequest: eventId={}, userId={}, email={}, purpose={}, expireMinutes={}",
                event.eventId(),
                event.userId(),
                event.email(),
                event.expireMinutes()
        );

        SendRegisterOtpCommand command = new SendRegisterOtpCommand(
                event.userId(),
                event.email(),
                event.otp(),
                event.userName(),
                event.expireMinutes() != null ? event.expireMinutes().toString() : null
        );

        sendRegisterOtpUseCase.execute(command);
    }
}
