# Failure Semantics in Concurrent Systems

## Overview

This document explains three distinct exception-handling policies for concurrent microservice execution. When multiple services execute concurrently, the system must explicitly define failure semantics—what happens when one or more services fail.

---

## 1. Fail-Fast Policy (Atomic)

### Description
The **Fail-Fast** policy implements atomic semantics: if any concurrent microservice invocation fails, the entire operation fails and no result is produced. This is also known as "all-or-nothing" execution.

### Behavior
- All services are invoked concurrently (fan-out pattern)
- The system waits for all services to complete using `CompletableFuture.allOf()`
- If **any** service completes exceptionally, the aggregate future completes exceptionally
- **No partial results** are ever returned to the caller
- The exception from the failing service propagates to the caller

### When to Use
This policy is appropriate for **correctness-critical systems** where partial results are invalid or meaningless:

- **Financial transactions**: All operations must succeed (debit and credit) or none should be applied
- **Distributed databases**: Multi-shard writes where consistency requires all shards to succeed
- **Data validation pipelines**: If any validation step fails, the entire dataset is considered invalid
- **Configuration deployment**: All configuration changes must be applied atomically across services
- **Healthcare systems**: Partial patient record updates could lead to dangerous inconsistencies

### Example Scenario
```
Scenario: Order Processing System
Services: [InventoryService, PaymentService, ShippingService]

Case 1: All services succeed
→ Order is confirmed and processed
→ Returns: "Inventory reserved, Payment processed, Shipping scheduled"

Case 2: PaymentService fails
→ Entire order fails
→ Returns: Exception("Payment processing failed")
→ No inventory is reserved, no shipping is scheduled
```

### Risks and Trade-offs
- **Lower availability**: Single point of failure—one failing service brings down the entire operation
- **Wasted computation**: Successful service results are discarded when another fails
- **Cascading failures**: One slow or failing service blocks the entire operation
- **User experience**: Users get no partial information, just a generic failure

### Implementation Pattern
```java
CompletableFuture.allOf(futures)
    .thenApply(v -> collectAllResults());
// Exception propagates automatically if any future fails
```

---

## 2. Fail-Partial Policy (Best-Effort)

### Description
The **Fail-Partial** policy implements best-effort semantics: successful microservice invocations return results, while failed invocations do not abort the entire operation. The system returns whatever results are available.

### Behavior
- All services are invoked concurrently (fan-out pattern)
- Each service's failure is handled independently
- Successful results are collected and returned
- Failed invocations are marked with error indicators (e.g., `[FAILED: serviceId]`)
- The overall operation **always completes normally** (never throws exception)
- Partial results are meaningful and actionable

### When to Use
This policy is appropriate for **systems where partial results are useful**:

- **Dashboards and monitoring**: Show available metrics even if some collectors fail
- **Aggregation services**: Display product information even if reviews service is down
- **Search results**: Return available results from accessible data sources
- **News feeds**: Show posts from available sources even if some feeds are unavailable
- **Analytics platforms**: Generate reports with available data, noting missing sources
- **Weather widgets**: Display temperature even if humidity sensor is offline

### Example Scenario
```
Scenario: Product Details Page
Services: [ProductInfo, Reviews, Recommendations, Inventory]

Case 1: All services succeed
→ Returns: ["Product X details", "4.5 stars (200 reviews)", "Similar items: Y, Z", "In stock: 15 units"]

Case 2: Reviews service fails
→ Returns: ["Product X details", "[FAILED: Reviews]", "Similar items: Y, Z", "In stock: 15 units"]
→ Page renders with most information intact
→ Reviews section shows "Temporarily unavailable"
```

### Risks and Trade-offs
- **User confusion**: Users may not realize data is incomplete
- **Inconsistent experience**: Same request may return different amounts of data
- **Silent degradation**: Repeated failures might go unnoticed without proper monitoring
- **Downstream assumptions**: Callers must handle variable-length results
- **Business logic complexity**: Code must handle presence/absence of optional data

### Best Practices
1. **Clear markers**: Use consistent error indicators that are easy to detect
2. **Logging**: Always log failures even though they don't abort the operation
3. **Monitoring**: Track failure rates to detect systemic issues
4. **UI design**: Clearly communicate to users when data is incomplete
5. **Graceful degradation**: Design UI to work with any subset of data

### Implementation Pattern
```java
futures.stream()
    .map(f -> f.exceptionally(ex -> "[FAILED: " + serviceId + "]"))
    .collect(toList());
```

---

## 3. Fail-Soft Policy (Fallback)

### Description
The **Fail-Soft** policy implements guaranteed completion: all failures are replaced with predefined fallback values, and the computation never fails. The system always returns a complete result, even if all services fail.

### Behavior
- All services are invoked concurrently (fan-out pattern)
- Failed invocations are replaced with the specified fallback value
- The overall operation **always completes normally** (never throws exception)
- No distinction is made between real results and fallback values in the output
- Failures are completely masked from the caller

### When to Use
This policy is appropriate for **high-availability systems** where degraded output is acceptable:

- **Caching layers**: Return cached/default value if backend is unreachable
- **Feature flags**: Use default configuration if config service is down
- **A/B testing**: Use control variant if experiment service fails
- **Content delivery**: Show placeholder content if personalization service fails
- **Public APIs**: Return generic response rather than HTTP 500 errors
- **Embedded systems**: Use safe default values if sensors malfunction

### Example Scenario
```
Scenario: Personalized Homepage
Services: [UserPreferences, RecentActivity, Recommendations]
Fallback: "Default content"

Case 1: All services succeed
→ Returns: ["Dark mode, English", "Viewed products X, Y", "Try products A, B, C"]

Case 2: UserPreferences and Recommendations fail
→ Returns: ["Default content", "Viewed products X, Y", "Default content"]
→ Page loads with generic theme and default recommendations
→ User experience degraded but functional
```

### **CRITICAL WARNINGS**

#### Risks of Masking Failures
This policy hides serious errors that may require immediate attention:

1. **Silent failures**: Critical system problems go undetected
2. **Data quality issues**: Users cannot distinguish real from fallback data
3. **Debugging difficulty**: Failure patterns are hidden from developers
4. **Cascading problems**: Downstream systems may not handle fallback values correctly
5. **False sense of security**: System appears healthy when it's actually broken
6. **Compliance risks**: Fallback data may not meet regulatory requirements
7. **Business impact**: Wrong decisions made based on fallback instead of real data

#### Required Safety Measures
If using Fail-Soft policy, you **MUST**:

1. **Extensive logging**: Log every failure with full context
2. **Metrics and monitoring**: Track fallback usage rates in real-time
3. **Alerting**: Set up alerts when fallback rate exceeds threshold
4. **Documentation**: Clearly document that fallbacks are in use
5. **User notification**: Consider informing users when showing fallback data
6. **Audit trail**: Maintain records of when fallbacks were used
7. **Regular review**: Analyze fallback patterns to identify systemic issues

### When NOT to Use
Avoid Fail-Soft policy when:

- Accuracy is critical (financial calculations, medical data, legal documents)
- Users need to know if data is unavailable
- Fallback values could lead to wrong decisions
- System health visibility is important
- Compliance requires reporting of failures
- Debugging and troubleshooting is a priority

### Implementation Pattern
```java
futures.stream()
    .map(f -> f.exceptionally(ex -> fallbackValue))
    .collect(toList());
// All exceptions are swallowed and replaced
```

---

## Comparison Matrix

| Aspect | Fail-Fast | Fail-Partial | Fail-Soft |
|--------|-----------|--------------|-----------|
| **Atomicity** | All-or-nothing | Per-service | Guaranteed completion |
| **Availability** | Low (single point of failure) | Medium | High (never fails) |
| **Data quality** | Perfect or none | Variable | Degraded acceptable |
| **Error visibility** | Explicit | Marked | Hidden |
| **User experience** | Fails completely | Partial content | Always works |
| **Debugging** | Easy (fails loudly) | Medium | Hard (fails silently) |
| **Typical use case** | Transactions | Dashboards | Caching/defaults |

---

## Concurrency and Non-Determinism

### Execution Order
All three policies invoke services concurrently using `CompletableFuture`. This means:

- **Execution order is non-deterministic**: Services may complete in any order
- **Completion time varies**: Depends on processing time, network latency, load
- **No ordering guarantees**: Even with identical inputs, order may differ between runs
- **List order is preserved**: Results are collected in the original request order

### Testing Non-Determinism
When testing concurrent code:
- **Don't assert execution order**: It's inherently unpredictable
- **Observe but don't verify**: Log completion patterns for understanding
- **Test liveness**: Ensure operations complete within timeouts (no deadlock)
- **Test semantics**: Verify policy behavior (exceptions, partial results, fallbacks)

### Example
```java
Services: [A (100ms), B (50ms), C (75ms)]

Run 1 actual completion order: B → C → A
Run 2 actual completion order: C → B → A
Run 3 actual completion order: B → A → C

But results always returned as: [A_result, B_result, C_result]
```

---

## Architectural Guidance

### Choosing a Policy

**Start with these questions:**

1. **Can the system function with partial results?**
   - Yes → Consider Fail-Partial
   - No → Consider Fail-Fast

2. **Is availability more critical than accuracy?**
   - Yes → Consider Fail-Soft (with extensive logging)
   - No → Stick with Fail-Fast

3. **Will users notice incomplete data?**
   - Yes → Avoid Fail-Soft
   - No, or they'll be informed → Fail-Partial or Fail-Soft acceptable

4. **Is this data used for critical decisions?**
   - Yes → Use Fail-Fast
   - No → Other policies acceptable

### Hybrid Approaches

Real systems often use different policies for different operations:

```java
// Critical path: Fail-Fast
checkoutOrder(cart)  // Must complete fully or fail

// Enhancement path: Fail-Partial
loadDashboard(userId)  // Show available widgets

// Background path: Fail-Soft
syncUserPreferences(prefs)  // Use defaults if sync fails
```

### Policy Evolution

As systems mature, policies may change:
- **MVP**: Fail-Fast everywhere (simple, correct)
- **Growth**: Add Fail-Partial for user-facing features
- **Scale**: Introduce Fail-Soft for high-traffic non-critical paths
- **Maturity**: Mix of all three based on requirements

---

## Testing Requirements

### Test Categories (JUnit 5)

Each policy requires comprehensive testing:

1. **Success path**: All services succeed
2. **Single failure**: One service fails
3. **Multiple failures**: Several services fail
4. **All failures**: Every service fails
5. **Liveness**: Completes within timeout (no deadlock)
6. **Non-determinism**: Observe but don't assert completion order
7. **Input validation**: Null/empty lists, size mismatches

### Assertions by Policy

**Fail-Fast:**
```java
assertThrows(ExecutionException.class, () -> future.get());
```

**Fail-Partial:**
```java
List<String> results = future.get();
assertTrue(results.contains("[FAILED: service2]"));
```

**Fail-Soft:**
```java
String result = future.get();
assertTrue(result.contains(fallbackValue));
```

### No Mockito Rule
All tests use real `CompletableFuture` instances with actual concurrency. This ensures:
- Real timing behavior is tested
- Actual exception propagation is verified
- True concurrent execution is validated
- Race conditions can be discovered

---

## Conclusion

Concurrency is not challenging because tasks run in parallel; it is challenging when **failure semantics are undefined**.

Each policy represents an explicit design decision with clear trade-offs:
- **Fail-Fast**: Correctness over availability
- **Fail-Partial**: Usefulness over completeness
- **Fail-Soft**: Availability over accuracy

The right choice depends on your specific requirements, but the wrong choice is **no explicit choice at all**.

---

**Author**: Assignment for COEN448/6761 Winter 2026  
**License**: Educational use only  
**Date**: February 2026
