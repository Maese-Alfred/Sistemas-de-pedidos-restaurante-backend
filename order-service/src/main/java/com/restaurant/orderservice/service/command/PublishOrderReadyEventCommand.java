package com.restaurant.orderservice.service.command;

import com.restaurant.orderservice.application.port.out.OrderReadyEventPublisherPort;
import com.restaurant.orderservice.domain.event.OrderReadyDomainEvent;

/**
 * Concrete command that publishes an order.ready event.
 */
public class PublishOrderReadyEventCommand implements OrderCommand {

    private final OrderReadyEventPublisherPort orderReadyEventPublisherPort;
    private final OrderReadyDomainEvent event;

    public PublishOrderReadyEventCommand(OrderReadyEventPublisherPort orderReadyEventPublisherPort,
                                          OrderReadyDomainEvent event) {
        this.orderReadyEventPublisherPort = orderReadyEventPublisherPort;
        this.event = event;
    }

    @Override
    public void execute() {
        orderReadyEventPublisherPort.publish(event);
    }
}
