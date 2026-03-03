package com.restaurant.orderservice.application.port.out;

import com.restaurant.orderservice.domain.event.OrderReadyDomainEvent;

/**
 * Application output port for publishing order ready events.
 */
public interface OrderReadyEventPublisherPort {
    void publish(OrderReadyDomainEvent event);
}
