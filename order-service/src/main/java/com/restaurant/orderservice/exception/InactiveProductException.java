package com.restaurant.orderservice.exception;

/**
 * Exception thrown when a product exists but is inactive (not available for ordering).
 * Returns 422 Unprocessable Entity — the product was found but cannot be processed
 * because it is inactive.
 *
 * This distinguishes from ProductNotFoundException (404) which means the product
 * does not exist at all.
 *
 * Validates Requirements: 2.6, 11.2
 */
public class InactiveProductException extends RuntimeException {

    /**
     * Constructs a new InactiveProductException with a descriptive message.
     *
     * @param productId the ID of the inactive product
     */
    public InactiveProductException(Long productId) {
        super("Product with id " + productId + " is inactive and cannot be ordered");
    }
}
