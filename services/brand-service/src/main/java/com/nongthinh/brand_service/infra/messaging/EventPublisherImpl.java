package com.nongthinh.brand_service.infra.messaging;

import org.springframework.stereotype.Component;
import com.nongthinh.brand_service.application.event.DomainEvent;
import com.nongthinh.brand_service.application.port.out.EventPublisher;
import com.nongthinh.brand_service.application.port.out.EventSerializer;
import org.springframework.kafka.core.KafkaTemplate;
import lombok.RequiredArgsConstructor;
import com.nongthinh.brand_service.infra.exception.InfrastructureException;
import com.nongthinh.brand_service.common.exception.ErrorCode;

@Component
@RequiredArgsConstructor
public class EventPublisherImpl implements EventPublisher {

    private final EventSerializer eventSerializer;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTopicResolver kafkaTopicResolver;

    @Override
    public void publish(DomainEvent event) {
        String topic = kafkaTopicResolver.resolve(event);
        String json = eventSerializer.serialize(event);
        String key = event.eventId().toString();
        try {
            kafkaTemplate.send(topic, key, json);
        } catch (Exception ex) {
            throw new InfrastructureException(ErrorCode.KAFKA_PUBLISH_FAILED, ex);
        }
    }
}
