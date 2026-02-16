package com.coen448.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Processes multiple microservices concurrently with configurable failure handling policies.
 * Implements three distinct exception-handling strategies:
 * 1. Fail-Fast (Atomic): Any failure aborts the entire operation
 * 2. Fail-Partial (Best-Effort): Returns successful results, ignores failures
 * 3. Fail-Soft (Fallback): Replaces failures with fallback values
 */
public class AsyncProcessor {

    /**
     * Task A: Fail-Fast Policy (Atomic)
     * 
     * If any concurrent microservice invocation fails, the entire operation fails
     * and no result is produced. This is appropriate for correctness-critical systems
     * where partial results are invalid.
     * 
     * Concurrency Pattern: Fan-out / Fan-in
     * - All services invoked concurrently
     * - Uses CompletableFuture.allOf to wait for all completions
     * - Any exception propagates to the caller
     * - No partial results returned
     *
     * @param services list of microservices to invoke
     * @param messages corresponding messages for each service
     * @return CompletableFuture that completes with concatenated results or fails
     * @throws IllegalArgumentException if lists are null, empty, or different sizes
     */
    public CompletableFuture<String> processAsyncFailFast(
            List<Microservice> services,
            List<String> messages) {
        
        validateInputs(services, messages);

        // Create array of futures - one per service/message pair
        CompletableFuture<String>[] futures = IntStream.range(0, services.size())
                .mapToObj(i -> services.get(i).retrieveAsync(messages.get(i)))
                .toArray(CompletableFuture[]::new);

        // allOf completes when all futures complete (successfully or exceptionally)
        // If any future completes exceptionally, allOf completes exceptionally
        return CompletableFuture.allOf(futures)
                .thenApply(v -> {
                    // Only executed if all futures completed successfully
                    // Collect results in order
                    StringBuilder result = new StringBuilder();
                    for (CompletableFuture<String> future : futures) {
                        // join() is safe here because allOf already completed
                        result.append(future.join()).append("\n");
                    }
                    return result.toString().trim();
                });
        // If any future fails, the exception propagates automatically
    }

    /**
     * Task B: Fail-Partial Policy (Best-Effort)
     * 
     * Successful microservice invocations return results, while failed invocations
     * do not abort the entire operation. This is appropriate for dashboards, analytics,
     * or aggregation where partial results are useful.
     * 
     * Concurrency Pattern: Fan-out / Fan-in with per-service error handling
     * - All services invoked concurrently
     * - Each service failure is handled independently
     * - Successful results are collected
     * - Failed invocations are marked with error indicator
     * - Overall computation always completes normally
     *
     * @param services list of microservices to invoke
     * @param messages corresponding messages for each service
     * @return CompletableFuture<List<String>> containing successful results and error markers
     * @throws IllegalArgumentException if lists are null, empty, or different sizes
     */
    public CompletableFuture<List<String>> processAsyncFailPartial(
            List<Microservice> services,
            List<String> messages) {
        
        validateInputs(services, messages);

        // Create futures with exception handling for each service
        List<CompletableFuture<String>> futures = IntStream.range(0, services.size())
                .mapToObj(i -> services.get(i)
                        .retrieveAsync(messages.get(i))
                        .exceptionally(ex -> {
                            // Handle failure for this specific service
                            // Return error marker instead of propagating exception
                            return "[FAILED: " + services.get(i).getServiceId() + "]";
                        }))
                .collect(Collectors.toList());

        // Convert List<CompletableFuture<String>> to CompletableFuture<List<String>>
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
        // No exception can escape because all exceptions are handled by exceptionally()
    }

    /**
     * Task C: Fail-Soft Policy (Fallback)
     * 
     * All failures are replaced with a predefined fallback value; the computation
     * never fails. This is appropriate for high-availability systems where degraded
     * output is acceptable.
     * 
     * WARNING: This policy masks failures and may hide serious errors.
     * Use only when:
     * - Availability is more critical than accuracy
     * - Fallback values provide meaningful degraded service
     * - Failures are logged/monitored through other means
     * - Users understand they may receive fallback data
     * 
     * RISKS:
     * - Silent failures: Critical errors may go unnoticed
     * - Data quality: Users cannot distinguish real from fallback data
     * - Debugging difficulty: Failure patterns are hidden
     * - Cascading issues: Downstream systems may not handle fallback appropriately
     *
     * Concurrency Pattern: Fan-out / Fan-in with guaranteed fallback
     * - All services invoked concurrently
     * - Every failure replaced with fallback value
     * - Always completes normally
     *
     * @param services list of microservices to invoke
     * @param messages corresponding messages for each service
     * @param fallbackValue value to use when service fails
     * @return CompletableFuture that always completes with concatenated results/fallbacks
     * @throws IllegalArgumentException if lists are null, empty, or different sizes
     */
    public CompletableFuture<String> processAsyncFailSoft(
            List<Microservice> services,
            List<String> messages,
            String fallbackValue) {
        
        validateInputs(services, messages);
        if (fallbackValue == null) {
            throw new IllegalArgumentException("Fallback value cannot be null");
        }

        // Create futures with fallback handling for each service
        CompletableFuture<String>[] futures = IntStream.range(0, services.size())
                .mapToObj(i -> services.get(i)
                        .retrieveAsync(messages.get(i))
                        .exceptionally(ex -> {
                            // Replace any failure with fallback value
                            // Exception is completely swallowed - never propagates
                            return fallbackValue;
                        }))
                .toArray(CompletableFuture[]::new);

        // Collect all results (which may include fallback values)
        return CompletableFuture.allOf(futures)
                .thenApply(v -> {
                    // All futures are guaranteed to complete successfully
                    StringBuilder result = new StringBuilder();
                    for (CompletableFuture<String> future : futures) {
                        result.append(future.join()).append("\n");
                    }
                    return result.toString().trim();
                });
        // This future can never complete exceptionally
    }

    /**
     * Validates that input lists are non-null, non-empty, and equal in size.
     *
     * @param services list of microservices
     * @param messages list of messages
     * @throws IllegalArgumentException if validation fails
     */
    private void validateInputs(List<Microservice> services, List<String> messages) {
        if (services == null || messages == null) {
            throw new IllegalArgumentException("Services and messages cannot be null");
        }
        if (services.isEmpty() || messages.isEmpty()) {
            throw new IllegalArgumentException("Services and messages cannot be empty");
        }
        if (services.size() != messages.size()) {
            throw new IllegalArgumentException(
                    "Services and messages must have the same size: " +
                    "services=" + services.size() + ", messages=" + messages.size());
        }
    }
}
