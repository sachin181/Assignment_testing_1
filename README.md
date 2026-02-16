# COEN448 Assignment 1 - Testing Exception-Handling Policies in Concurrent Systems

## Project Overview

This project implements and tests three explicit exception-handling policies for concurrent microservice execution using Java's `CompletableFuture` API.

## Policies Implemented

1. **Fail-Fast (Atomic)**: All-or-nothing execution
2. **Fail-Partial (Best-Effort)**: Returns partial results on failure
3. **Fail-Soft (Fallback)**: Replaces failures with fallback values

## Project Structure

```
coen448-assignment1/
├── src/
│   ├── main/java/com/coen448/concurrent/
│   │   ├── Microservice.java         # Microservice implementation
│   │   └── AsyncProcessor.java       # Three failure policies
│   └── test/java/com/coen448/concurrent/
│       └── AsyncProcessorTest.java   # Comprehensive JUnit 5 tests
├── docs/
│   └── failure-semantics.md          # Detailed policy documentation
├── pom.xml                            # Maven build configuration
└── README.md                          # This file
```

## Building and Running

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher

### Compile the project
```bash
mvn clean compile
```

### Run tests
```bash
mvn test
```

### Package
```bash
mvn package
```

## Test Coverage

The test suite includes:
- ✅ All three policies with success scenarios
- ✅ Single and multiple failure cases
- ✅ Exception propagation validation
- ✅ Liveness testing (no deadlock)
- ✅ Non-determinism observation
- ✅ Input validation
- ✅ Timeout guarantees

**Total tests**: 25+ test cases covering all requirements

## Key Features

- **No Mockito**: All tests use real `CompletableFuture` instances
- **Timeout Protection**: All tests complete within 5 seconds
- **Proper Exception Handling**: Uses `assertThrows` for fail-fast tests
- **Comprehensive Documentation**: Detailed explanations in `docs/failure-semantics.md`

## Documentation

See `docs/failure-semantics.md` for:
- Detailed explanation of each policy
- When to use each approach
- Risks and trade-offs
- Example scenarios
- Best practices

## Author

COEN448/6761 Winter 2026 Assignment

## License

Educational use only - Copyright © 2026-2027 Yan Liu
