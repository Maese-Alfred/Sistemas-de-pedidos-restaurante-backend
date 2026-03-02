package com.restaurant.orderservice.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event emitted when an order transitions to READY status.
 * Keeps domain intent independent from transport-specific contracts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReadyDomainEvent {

    public static final int CURRENT_VERSION = 1;
    public static final String EVENT_TYPE = "order.ready";

    private UUID eventId;
    private String eventType;
    private Integer eventVersion;
    private LocalDateTime occurredAt;
    private UUID orderId;
    private String status;
    private LocalDateTime updatedAt;
}
