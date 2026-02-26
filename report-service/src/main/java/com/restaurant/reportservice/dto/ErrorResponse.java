package com.restaurant.reportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standardized error response DTO for the Report Service REST API.
 * Provides a consistent error structure matching the order-service contract.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * Timestamp when the error occurred.
     */
    private LocalDateTime timestamp;

    /**
     * HTTP status code (e.g., 400, 422, 500).
     */
    private Integer status;

    /**
     * Error type or category (e.g., "Bad Request", "Unprocessable Entity").
     */
    private String error;

    /**
     * Detailed error message describing what went wrong.
     */
    private String message;
}
