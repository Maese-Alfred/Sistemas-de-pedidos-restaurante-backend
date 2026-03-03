package com.restaurant.orderservice;

import com.restaurant.orderservice.application.port.out.OrderPlacedEventPublisherPort;
import com.restaurant.orderservice.application.port.out.OrderReadyEventPublisherPort;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceApplicationTests {

    @MockBean
    private OrderPlacedEventPublisherPort eventPublisherPort;

    @MockBean
    private OrderReadyEventPublisherPort orderReadyEventPublisherPort;

    @Test
    void contextLoads() {
        // This test verifies that the Spring application context loads successfully
    }
}
