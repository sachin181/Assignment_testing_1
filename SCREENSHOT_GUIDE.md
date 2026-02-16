# Screenshot Guide – 7 Figures for Your Assignment

Use this guide in order. Copy-paste the text below into GitHub, then take each screenshot.

---

## Step 1: Create 4 Issues (for Figure 1 and Figure 2)

Go to: **https://github.com/sachin181/Assignment_testing_1/issues**  
Click **New issue**. Create these **4 issues** (one at a time). You can add labels like `enhancement`, `documentation`, `testing` if your repo has them.

---

### Issue #1 – Task A: Fail-Fast

**Title:**
```
Task A - Implement Fail-Fast (Atomic) Policy
```

**Description (copy everything below):**
```
## Objective
Implement the Fail-Fast exception handling policy for concurrent microservice execution.

## Requirements
- [ ] Create `processAsyncFailFast()` method in AsyncProcessor
- [ ] Use `CompletableFuture.allOf()` for coordination
- [ ] Ensure any exception propagates to caller
- [ ] No partial results returned
- [ ] Add comprehensive unit tests
- [ ] Add proper Javadoc documentation

## Acceptance Criteria
- All services succeed → returns concatenated results
- Any service fails → exception propagates
- No partial results ever returned
- All tests pass with timeout protection
```

---

### Issue #2 – Task B: Fail-Partial

**Title:**
```
Task B - Implement Fail-Partial (Best-Effort) Policy
```

**Description:**
```
## Objective
Implement the Fail-Partial exception handling policy that returns partial results.

## Requirements
- [ ] Create `processAsyncFailPartial()` method in AsyncProcessor
- [ ] Implement per-service error handling
- [ ] Return successful results with failure markers
- [ ] Ensure operation never throws exception
- [ ] Add comprehensive unit tests
- [ ] Document error marker format

## Acceptance Criteria
- All services succeed → returns all results
- Some services fail → returns mix of results and markers
- All services fail → returns all failure markers
- Operation always completes normally
- Clear error markers like [FAILED: serviceId]
```

---

### Issue #3 – Task C: Fail-Soft (use for Figure 2)

**Title:**
```
Task C - Implement Fail-Soft (Fallback) Policy with Risk Documentation
```

**Description:**
```
## Objective
Implement the Fail-Soft exception handling policy with comprehensive risk documentation.

## Requirements
- [ ] Create `processAsyncFailSoft()` method in AsyncProcessor
- [ ] Replace all failures with fallback values
- [ ] Ensure operation never fails
- [ ] Add CRITICAL WARNINGS in documentation
- [ ] Document risks of masking failures
- [ ] Document required safety measures
- [ ] Add comprehensive unit tests

## Acceptance Criteria
- All services succeed → returns all real results
- Some services fail → replaces failures with fallback
- All services fail → returns all fallback values
- Operation always completes normally
- Extensive risk documentation included
- Javadoc includes warnings about masking errors
```

*(Add label `documentation` if available – good for Figure 2.)*

---

### Issue #4 – Testing

**Title:**
```
Create JUnit 5 Test Suite for All Policies
```

**Description:**
```
## Objective
Create comprehensive unit tests for all three exception-handling policies.

## Requirements
- [ ] No Mockito - use real CompletableFuture instances
- [ ] All futures must be awaited with timeouts
- [ ] Test all three policies
- [ ] Test success paths
- [ ] Test single/multiple/all failures
- [ ] Test liveness (no deadlock)
- [ ] Observe but don't assert on non-determinism
- [ ] Test input validation
- [ ] Use proper exception assertions

## Acceptance Criteria
- 25+ test cases covering all requirements
- All tests annotated with @Timeout
- Proper use of assertThrows for exceptions
- Tests verify policy semantics
- All tests pass
- No Mockito dependencies
```

---

## Step 2: Take Screenshot – Figure 1

- Go to **Issues** → **Open** (or “All” so all 4 show).
- **Figure 1:** Screenshot of the **Issues list** showing **all 4 issues** (titles and state).

---

## Step 3: Take Screenshot – Figure 2

- Open **Issue #3** (Fail-Soft).
- **Figure 2:** Screenshot of **Issue #3’s full page** (title + full description + labels).

---

## Step 4: Take Screenshot – Figure 3 (Feature Branches)

- Go to **Code** → click the branch dropdown (“main”) → **View all branches** (or **Branches** in the repo menu).
- You should see: `main`, `feature/fail-fast-policy`, `feature/fail-partial-policy`, `feature/fail-soft-policy`.
- **Figure 3:** Screenshot of the **branches list** showing these **3 feature branches**.

---

## Step 5: Create the 3 Pull Requests

Create **3 PRs** (base: `main`, compare: each feature branch). Use the titles and bodies below.

---

### PR #1 – Fail-Fast

- **Base:** `main`  
- **Compare:** `feature/fail-fast-policy`  
- **Title:** `Implement Fail-Fast (Atomic) Exception Handling Policy`

**Description:**
```
## Summary
This PR implements the Fail-Fast (Atomic) exception handling policy as specified in Task A.

## Changes
- ✅ Added Microservice class with asynchronous operations
- ✅ Implemented processAsyncFailFast() method
- ✅ Uses CompletableFuture.allOf() for coordination
- ✅ Proper exception propagation
- ✅ Added comprehensive unit tests
- ✅ All tests pass with timeout protection

## Testing
- All services succeed: Returns concatenated results ✅
- One service fails: Exception propagates ✅
- Multiple services fail: Exception propagates ✅
- Liveness test: No deadlock ✅

## Related Issue
Closes #1

## Review Checklist
- [ ] Code follows project style guidelines
- [ ] All tests pass
- [ ] Documentation is clear
- [ ] No race conditions or deadlocks
- [ ] Exception handling is correct
```

---

### PR #2 – Fail-Partial

- **Base:** `main`  
- **Compare:** `feature/fail-partial-policy`  
- **Title:** `Implement Fail-Partial (Best-Effort) Policy`

**Description:**
```
## Summary
This PR implements the Fail-Partial (Best-Effort) exception handling policy as specified in Task B.

## Changes
- ✅ Implemented processAsyncFailPartial() method
- ✅ Per-service error handling with exceptionally()
- ✅ Clear error markers: [FAILED: serviceId]
- ✅ Operation never throws exception
- ✅ Added comprehensive unit tests
- ✅ Tests verify partial result handling

## Testing
- All services succeed: Returns all results ✅
- One service fails: Returns partial results with marker ✅
- Multiple services fail: Returns mix of results/markers ✅
- All services fail: Returns all failure markers ✅
- Liveness test: No deadlock ✅

## Related Issue
Closes #2

## Review Checklist
- [ ] Error markers are consistent and clear
- [ ] No exceptions escape to caller
- [ ] All tests pass
- [ ] Documentation explains partial results
```

---

### PR #3 – Fail-Soft (for Figure 6)

- **Base:** `main`  
- **Compare:** `feature/fail-soft-policy`  
- **Title:** `Implement Fail-Soft Policy with Comprehensive Risk Documentation`

**Description:**
```
## Summary
This PR implements the Fail-Soft (Fallback) exception handling policy with extensive risk documentation as specified in Task C.

## Changes
- ✅ Implemented processAsyncFailSoft() method
- ✅ Fallback value replaces all failures
- ✅ Operation never fails
- ✅ CRITICAL WARNINGS in Javadoc
- ✅ Extensive risk documentation in failure-semantics.md
- ✅ Added comprehensive unit tests
- ✅ Documented required safety measures

## Risk Documentation Includes
- Silent failures warning
- Data quality concerns
- Debugging difficulties
- Required monitoring and alerting
- When NOT to use this policy

## Testing
- All services succeed: Returns all real results ✅
- One service fails: Replaces with fallback ✅
- Multiple services fail: Replaces each with fallback ✅
- All services fail: Returns all fallbacks ✅
- Liveness test: No deadlock ✅

## Related Issue
Closes #3

## Review Checklist
- [ ] Risk documentation is thorough
- [ ] Warnings are prominent in Javadoc
- [ ] All tests pass
- [ ] Fallback behavior is correct
- [ ] No exceptions can escape
```

---

## Step 6: Take Screenshot – Figure 4 (Network Graph)

- Go to **Insights** → **Network** (or **Graph** in some layouts).
- **Figure 4:** Screenshot of the **network graph** showing `main` and the **3 feature branches** (and later, after merges, the merge structure).

*(You can take this before or after merging the PRs; both show “parallel development.”)*

---

## Step 7: Add Code Review (for Figure 7)

- Open **PR #3** (Fail-Soft).
- Go to **Files changed**.
- Click the **+** next to a line (e.g. in `README.md` or `AsyncProcessor.java`) to add a **comment**.
- Add a **single comment** (or a few). Example:

**Example line comment (on a line in the changed file):**
```
Excellent documentation of risks in the Javadoc. The warnings are clear and prominent.
```

**Example general review comment (when submitting review):**
```
Approved! The risk documentation is exactly what users need to make informed decisions about using Fail-Soft. Required safety measures are clearly stated.
```

- Choose **Approve** and submit the review.
- **Figure 7:** Screenshot of **PR #3** showing **Conversation** or **Files changed** with your **review comment(s) and approval**.

---

## Step 8: Merge All 3 PRs

- On each PR, click **Merge pull request** → **Confirm merge**.
- **Figure 5:** Screenshot of **Pull requests** (filter: “Closed” or “Merged”) showing **all 3 PRs merged**.
- **Figure 6:** Screenshot of **PR #3’s main page** (title, description, “Merged” badge, and link to Fail-Soft implementation/docs).

---

## Quick Screenshot Checklist

| Figure | What to capture |
|--------|------------------|
| **1** | Issues list – all 4 issues |
| **2** | Issue #3 – full page (Fail-Soft requirements) |
| **3** | Branches – 3 feature branches |
| **4** | Network graph – branch/merge structure |
| **5** | Pull requests – all 3 PRs merged |
| **6** | PR #3 – Fail-Soft implementation + documentation |
| **7** | PR #3 (or any PR) – code review comments + approval |

---

**Repo:** https://github.com/sachin181/Assignment_testing_1
