package com.coen448.concurrent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for AsyncProcessor failure handling policies.
 * 
 * Test Requirements:
 * - No Mockito usage
 * - All futures awaited with timeouts
 * - Policy semantics verified
 * - Liveness guarantees tested (no deadlock/infinite wait)
 * - Non-determinism observed but not asserted
 */
@DisplayName("AsyncProcessor Exception-Handling Policy Tests")
public class AsyncProcessorTest {

    private AsyncProcessor processor;
    private static final int TIMEOUT_SECONDS = 5;

    @BeforeEach
    void setUp() {
        processor = new AsyncProcessor();
    }

    // ========== FAIL-FAST POLICY TESTS ==========

    @Test
    @DisplayName("Fail-Fast: All services succeed - should return concatenated results")
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testFailFast_AllSucceed() throws Exception {
        // Arrange
        List<Microservice> services = Arrays.asList(
                new Microservice("service1"),
                new Microservice("service2"),
                new Microservice("service3")
        );
        List<String> messages = Arrays.asList("msg1", "msg2", "msg3");

        // Act
        CompletableFuture<String> future = processor.processAsyncFailFast(services, messages);
        String result = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("SERVICE1: MSG1"));
        assertTrue(result.contains("SERVICE2: MSG2"));
        assertTrue(result.contains("SERVICE3: MSG3"));
        assertEquals(3, result.split("\n").length);
    }

    @Test
    @DisplayName("Fail-Fast: One service fails - exception should propagate")
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testFailFast_OneServiceFails_ExceptionPropagates() {
        // Arrange
        Microservice service1 = new Microservice("service1");
        Microservice service2 = new Microservice("service2");
        Microservice service3 = new Microservice("service3");
        
        service2.setShouldFail(true); // Make second service fail
        
        List<Microservice> services = Arrays.asList(service1, service2, service3);
        List<String> messages = Arrays.asList("msg1", "msg2", "msg3");

        // Act
        CompletableFuture<String> future = processor.processAsyncFailFast(services, messages);

        // Assert - exception should propagate
        ExecutionException exception = assertThrows(ExecutionException.class, () -> {
            future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        });
        
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertTrue(exception.getCause().getMessage().contains("service2"));
    }

    @Test
    @DisplayName("Fail-Fast: Multiple services fail - exception should propagate")
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testFailFast_MultipleServicesFail_ExceptionPropagates() {
        // Arrange
        Microservice service1 = new Microservice("service1");
        Microservice service2 = new Microservice("service2");
        Microservice service3 = new Microservice("service3");
        
        service1.setShouldFail(true);
        service3.setShouldFail(true);
        
        List<Microservice> services = Arrays.asList(service1, service2, service3);
        List<String> messages = Arrays.asList("msg1", "msg2", "msg3");

        // Act
        CompletableFuture<String> future = processor.processAsyncFailFast(services, messages);

        // Assert - at least one exception should propagate
        assertThrows(ExecutionException.class, () -> {
            future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        });
    }

    @Test
    @DisplayName("Fail-Fast: Single service success")
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testFailFast_SingleService() throws Exception {
        // Arrange
        List<Microservice> services = Arrays.asList(new Microservice("solo"));
        List<String> messages = Arrays.asList("single");

        // Act
        CompletableFuture<String> future = processor.processAsyncFailFast(services, messages);
        String result = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // Assert
        assertEquals("SOLO: SINGLE", result);
    }

    // ========== FAIL-PARTIAL POLICY TESTS ==========

    @Test
    @DisplayName("Fail-Partial: All services succeed - should return all results")
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testFailPartial_AllSucceed() throws Exception {
        // Arrange
        List<Microservice> services = Arrays.asList(
                new Microservice("service1"),
                new Microservice("service2"),
                new Microservice("service3")
        );
        List<String> messages = Arrays.asList("msg1", "msg2", "msg3");

        // Act
        CompletableFuture<List<String>> future = processor.processAsyncFailPartial(services, messages);
        List<String> results = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // Assert
        assertNotNull(results);
        assertEquals(3, results.size());
        assertTrue(results.get(0).contains("SERVICE1: MSG1"));
        assertTrue(results.get(1).contains("SERVICE2: MSG2"));
        assertTrue(results.get(2).contains("SERVICE3: MSG3"));
    }

    @Test
    @DisplayName("Fail-Partial: One service fails - should return partial results with failure marker")
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testFailPartial_OneServiceFails_ReturnsPartialResults() throws Exception {
        // Arrange
        Microservice service1 = new Microservice("service1");
        Microservice service2 = new Microservice("service2");
        Microservice service3 = new Microservice("service3");
        
        service2.setShouldFail(true);
        
        List<Microservice> services = Arrays.asList(service1, service2, service3);
        List<String> messages = Arrays.asList("msg1", "msg2", "msg3");

        // Act
        CompletableFuture<List<String>> future = processor.processAsyncFailPartial(services, messages);
        List<String> results = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // Assert - no exception thrown, partial results returned
        assertNotNull(results);
        assertEquals(3, results.size());
        assertTrue(results.get(0).contains("SERVICE1: MSG1"));
        assertTrue(results.get(1).contains("[FAILED: service2]"));
        assertTrue(results.get(2).contains("SERVICE3: MSG3"));
    }

    @Test
    @DisplayName("Fail-Partial: Multiple services fail - should return available results")
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testFailPartial_MultipleServicesFail() throws Exception {
        // Arrange
        Microservice service1 = new Microservice("service1");
        Microservice service2 = new Microservice("service2");
        Microservice service3 = new Microservice("service3");
        Microservice service4 = new Microservice("service4");
        
        service1.setShouldFail(true);
        service3.setShouldFail(true);
        
        List<Microservice> services = Arrays.asList(service1, service2, service3, service4);
        List<String> messages = Arrays.asList("msg1", "msg2", "msg3", "msg4");

        // Act
        CompletableFuture<List<String>> future = processor.processAsyncFailPartial(services, messages);
        List<String> results = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // Assert
        assertNotNull(results);
        assertEquals(4, results.size());
        assertTrue(results.get(0).contains("[FAILED: service1]"));
        assertTrue(results.get(1).contains("SERVICE2: MSG2"));
        assertTrue(results.get(2).contains("[FAILED: service3]"));
        assertTrue(results.get(3).contains("SERVICE4: MSG4"));
    }

    @Test
    @DisplayName("Fail-Partial: All services fail - should return all failure markers")
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testFailPartial_AllServicesFail() throws Exception {
        // Arrange
        Microservice service1 = new Microservice("service1");
        Microservice service2 = new Microservice("service2");
        
        service1.setShouldFail(true);
        service2.setShouldFail(true);
        
        List<Microservice> services = Arrays.asList(service1, service2);
        List<String> messages = Arrays.asList("msg1", "msg2");

        // Act
        CompletableFuture<List<String>> future = processor.processAsyncFailPartial(services, messages);
        List<String> results = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // Assert - completes normally even when all fail
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.get(0).contains("[FAILED: service1]"));
        assertTrue(results.get(1).contains("[FAILED: service2]"));
    }

    // ========== FAIL-SOFT POLICY TESTS ==========

    @Test
    @DisplayName("Fail-Soft: All services succeed - should return all results")
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testFailSoft_AllSucceed() throws Exception {
        // Arrange
        List<Microservice> services = Arrays.asList(
                new Microservice("service1"),
                new Microservice("service2"),
                new Microservice("service3")
        );
        List<String> messages = Arrays.asList("msg1", "msg2", "msg3");

        // Act
        CompletableFuture<String> future = processor.processAsyncFailSoft(services, messages, "FALLBACK");
        String result = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("SERVICE1: MSG1"));
        assertTrue(result.contains("SERVICE2: MSG2"));
        assertTrue(result.contains("SERVICE3: MSG3"));
        assertFalse(result.contains("FALLBACK"));
    }

    @Test
    @DisplayName("Fail-Soft: One service fails - should replace with fallback value")
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testFailSoft_OneServiceFails_UsesFallback() throws Exception {
        // Arrange
        Microservice service1 = new Microservice("service1");
        Microservice service2 = new Microservice("service2");
        Microservice service3 = new Microservice("service3");
        
        service2.setShouldFail(true);
        
        List<Microservice> services = Arrays.asList(service1, service2, service3);
        List<String> messages = Arrays.asList("msg1", "msg2", "msg3");
        String fallback = "FALLBACK_VALUE";

        // Act
        CompletableFuture<String> future = processor.processAsyncFailSoft(services, messages, fallback);
        String result = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // Assert - completes normally with fallback
        assertNotNull(result);
        assertTrue(result.contains("SERVICE1: MSG1"));
        assertTrue(result.contains("FALLBACK_VALUE"));
        assertTrue(result.contains("SERVICE3: MSG3"));
    }

    @Test
    @DisplayName("Fail-Soft: All services fail - should return all fallback values")
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testFailSoft_AllServicesFail_AllFallbacks() throws Exception {
        // Arrange
        Microservice service1 = new Microservice("service1");
        Microservice service2 = new Microservice("service2");
        
        service1.setShouldFail(true);
        service2.setShouldFail(true);
        
        List<Microservice> services = Arrays.asList(service1, service2);
        List<String> messages = Arrays.asList("msg1", "msg2");
        String fallback = "DEFAULT";

        // Act
        CompletableFuture<String> future = processor.processAsyncFailSoft(services, messages, fallback);
        String result = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // Assert - completes normally even when all fail
        assertNotNull(result);
        assertEquals("DEFAULT\nDEFAULT", result);
    }

    @Test
    @DisplayName("Fail-Soft: Multiple services fail - should use fallback for each")
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testFailSoft_MultipleServicesFail() throws Exception {
        // Arrange
        Microservice service1 = new Microservice("service1");
        Microservice service2 = new Microservice("service2");
        Microservice service3 = new Microservice("service3");
        Microservice service4 = new Microservice("service4");
        
        service2.setShouldFail(true);
        service4.setShouldFail(true);
        
        List<Microservice> services = Arrays.asList(service1, service2, service3, service4);
        List<String> messages = Arrays.asList("msg1", "msg2", "msg3", "msg4");
        String fallback = "N/A";

        // Act
        CompletableFuture<String> future = processor.processAsyncFailSoft(services, messages, fallback);
        String result = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // Assert
        assertNotNull(result);
        String[] lines = result.split("\n");
        assertEquals(4, lines.length);
        assertTrue(lines[0].contains("SERVICE1: MSG1"));
        assertEquals("N/A", lines[1]);
        assertTrue(lines[2].contains("SERVICE3: MSG3"));
        assertEquals("N/A", lines[3]);
    }

    // ========== LIVENESS TESTS (NO DEADLOCK) ==========

    @Test
    @DisplayName("Liveness: Fail-Fast completes within timeout even with failures")
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testLiveness_FailFast_NoDeadlock() {
        // Arrange
        Microservice service1 = new Microservice("service1");
        service1.setShouldFail(true);
        
        List<Microservice> services = Arrays.asList(service1);
        List<String> messages = Arrays.asList("msg1");

        // Act & Assert - should complete (with exception) within timeout
        CompletableFuture<String> future = processor.processAsyncFailFast(services, messages);
        
        assertThrows(ExecutionException.class, () -> {
            future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        });
        // If we reach here, no deadlock occurred
    }

    @Test
    @DisplayName("Liveness: Fail-Partial completes within timeout")
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testLiveness_FailPartial_NoDeadlock() throws Exception {
        // Arrange
        List<Microservice> services = Arrays.asList(
                new Microservice("service1"),
                new Microservice("service2")
        );
        services.get(0).setShouldFail(true);
        
        List<String> messages = Arrays.asList("msg1", "msg2");

        // Act
        CompletableFuture<List<String>> future = processor.processAsyncFailPartial(services, messages);
        List<String> results = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // Assert - should complete successfully
        assertNotNull(results);
    }

    @Test
    @DisplayName("Liveness: Fail-Soft completes within timeout")
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testLiveness_FailSoft_NoDeadlock() throws Exception {
        // Arrange
        Microservice service1 = new Microservice("service1");
        service1.setShouldFail(true);
        
        List<Microservice> services = Arrays.asList(service1);
        List<String> messages = Arrays.asList("msg1");

        // Act
        CompletableFuture<String> future = processor.processAsyncFailSoft(services, messages, "FALLBACK");
        String result = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // Assert - should complete successfully
        assertNotNull(result);
        assertEquals("FALLBACK", result);
    }

    // ========== NON-DETERMINISM OBSERVATION TESTS ==========

    @Test
    @DisplayName("Non-determinism: Completion order may vary (observed, not asserted)")
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testNonDeterminism_CompletionOrderVaries() throws Exception {
        // This test observes but does not assert on completion order
        // Completion order is non-deterministic due to concurrent execution
        
        // Arrange - create services with varied delays
        Microservice service1 = new Microservice("service1");
        Microservice service2 = new Microservice("service2");
        Microservice service3 = new Microservice("service3");
        
        service1.setDelayMs(50);
        service2.setDelayMs(10);
        service3.setDelayMs(30);
        
        List<Microservice> services = Arrays.asList(service1, service2, service3);
        List<String> messages = Arrays.asList("msg1", "msg2", "msg3");

        // Act
        CompletableFuture<String> future = processor.processAsyncFailFast(services, messages);
        String result = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // Assert - results are present but order in execution may have varied
        // We only verify all results are present, not their completion order
        assertNotNull(result);
        assertTrue(result.contains("SERVICE1: MSG1"));
        assertTrue(result.contains("SERVICE2: MSG2"));
        assertTrue(result.contains("SERVICE3: MSG3"));
        
        // Note: Actual execution order was non-deterministic but list order is preserved
        System.out.println("Non-deterministic execution completed. Result order (preserved): " + result);
    }

    @Test
    @DisplayName("Non-determinism: Multiple runs may show different timing patterns")
    @Timeout(value = TIMEOUT_SECONDS * 3, unit = TimeUnit.SECONDS)
    void testNonDeterminism_MultipleRuns() throws Exception {
        // Run the same test multiple times to observe timing variations
        // This demonstrates concurrency but doesn't assert specific order
        
        for (int run = 1; run <= 3; run++) {
            List<Microservice> services = Arrays.asList(
                    new Microservice("s1"),
                    new Microservice("s2"),
                    new Microservice("s3")
            );
            List<String> messages = Arrays.asList("m1", "m2", "m3");

            long startTime = System.currentTimeMillis();
            CompletableFuture<String> future = processor.processAsyncFailFast(services, messages);
            String result = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            long duration = System.currentTimeMillis() - startTime;

            System.out.println("Run " + run + " completed in " + duration + "ms");
            assertNotNull(result);
        }
    }

    // ========== INPUT VALIDATION TESTS ==========

    @Test
    @DisplayName("Validation: Null services list should throw exception")
    void testValidation_NullServices() {
        List<String> messages = Arrays.asList("msg1");
        
        assertThrows(IllegalArgumentException.class, () -> {
            processor.processAsyncFailFast(null, messages);
        });
    }

    @Test
    @DisplayName("Validation: Null messages list should throw exception")
    void testValidation_NullMessages() {
        List<Microservice> services = Arrays.asList(new Microservice("s1"));
        
        assertThrows(IllegalArgumentException.class, () -> {
            processor.processAsyncFailFast(services, null);
        });
    }

    @Test
    @DisplayName("Validation: Empty services list should throw exception")
    void testValidation_EmptyServices() {
        List<Microservice> services = new ArrayList<>();
        List<String> messages = Arrays.asList("msg1");
        
        assertThrows(IllegalArgumentException.class, () -> {
            processor.processAsyncFailFast(services, messages);
        });
    }

    @Test
    @DisplayName("Validation: Mismatched list sizes should throw exception")
    void testValidation_MismatchedSizes() {
        List<Microservice> services = Arrays.asList(
                new Microservice("s1"),
                new Microservice("s2")
        );
        List<String> messages = Arrays.asList("msg1");
        
        assertThrows(IllegalArgumentException.class, () -> {
            processor.processAsyncFailFast(services, messages);
        });
    }

    @Test
    @DisplayName("Validation: Fail-Soft null fallback should throw exception")
    void testValidation_NullFallback() {
        List<Microservice> services = Arrays.asList(new Microservice("s1"));
        List<String> messages = Arrays.asList("msg1");
        
        assertThrows(IllegalArgumentException.class, () -> {
            processor.processAsyncFailSoft(services, messages, null);
        });
    }
}
