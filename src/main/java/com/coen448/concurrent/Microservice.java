package com.coen448.concurrent;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Represents a microservice that performs asynchronous operations.
 * Execution is concurrent and completion order is nondeterministic.
 */
public class Microservice {
    private final String serviceId;
    private final Random random = new Random();
    private boolean shouldFail = false;
    private int delayMs = 100;

    /**
     * Constructs a Microservice with the given service identifier.
     *
     * @param serviceId unique identifier for this service
     */
    public Microservice(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * Performs an asynchronous retrieval operation.
     * The operation may complete successfully or exceptionally based on configuration.
     *
     * @param message the message to process
     * @return CompletableFuture that completes with the processed result
     */
    public CompletableFuture<String> retrieveAsync(String message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate variable processing time for non-deterministic completion order
                int processingTime = delayMs + random.nextInt(50);
                TimeUnit.MILLISECONDS.sleep(processingTime);
                
                if (shouldFail) {
                    throw new RuntimeException("Service " + serviceId + " failed processing: " + message);
                }
                
                return serviceId + ": " + message.toUpperCase();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Service " + serviceId + " interrupted", e);
            }
        });
    }

    /**
     * Configures this service to fail on the next retrieveAsync call.
     *
     * @param shouldFail true to make the service fail
     */
    public void setShouldFail(boolean shouldFail) {
        this.shouldFail = shouldFail;
    }

    /**
     * Sets the base delay for processing operations.
     *
     * @param delayMs delay in milliseconds
     */
    public void setDelayMs(int delayMs) {
        this.delayMs = delayMs;
    }

    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String toString() {
        return "Microservice{" +
                "serviceId='" + serviceId + '\'' +
                '}';
    }
}
