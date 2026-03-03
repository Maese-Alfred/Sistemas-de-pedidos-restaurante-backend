package com.restaurant.orderservice.infrastructure.messaging;

import com.restaurant.orderservice.application.port.out.OrderReadyEventPublisherPort;
import com.restaurant.orderservice.domain.event.OrderReadyDomainEvent;
import com.restaurant.orderservice.exception.EventPublicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ adapter for the order ready event output port.
 */
@Component
@Slf4j
public class RabbitOrderReadyEventPublisher implements OrderReadyEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing-key.order-ready}")
    private String orderReadyRoutingKey;

    public RabbitOrderReadyEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(OrderReadyDomainEvent domainEvent) {
        OrderReadyEventMessage message = toMessage(domainEvent);
        try {
            rabbitTemplate.convertAndSend(exchangeName, orderReadyRoutingKey, message, amqpMessage -> {
                amqpMessage.getMessageProperties().setHeader("eventType", message.getEventType());
                amqpMessage.getMessageProperties().setHeader("eventVersion", message.getEventVersion());
                return amqpMessage;
            });

            log.info(
                    "Successfully published order.ready event: eventId={}, orderId={}, version={}",
                    message.getEventId(),
                    message.getPayload().getOrderId(),
                    message.getEventVersion()
            );
        } catch (Exception ex) {
            log.error(
                    "Failed to publish order.ready event: eventId={}, orderId={}, error={}",
                    message.getEventId(),
                    message.getPayload().getOrderId(),
                    ex.getMessage(),
                    ex
            );
            throw new EventPublicationException(
                    String.format("Unable to publish order.ready event for orderId=%s",
                            message.getPayload().getOrderId()),
                    ex
            );
        }
    }

    private OrderReadyEventMessage toMessage(OrderReadyDomainEvent domainEvent) {
        OrderReadyEventMessage.Payload payload = OrderReadyEventMessage.Payload.builder()
                .orderId(domainEvent.getOrderId())
                .status(domainEvent.getStatus())
                .updatedAt(domainEvent.getUpdatedAt())
                .build();

        return OrderReadyEventMessage.builder()
                .eventId(domainEvent.getEventId())
                .eventType(domainEvent.getEventType())
                .eventVersion(domainEvent.getEventVersion())
                .occurredAt(domainEvent.getOccurredAt())
                .payload(payload)
                .build();
    }
}
