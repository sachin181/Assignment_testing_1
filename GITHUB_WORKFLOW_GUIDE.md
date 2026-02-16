# GitHub Workflow - Visual Guide and Instructions

## Overview
This document provides step-by-step instructions for setting up the GitHub workflow for COEN448 Assignment 1.

---

## Step 1: Create GitHub Repository

### Actions:
1. Go to https://github.com/new
2. Repository name: `coen448-assignment1`
3. Description: "Testing Exception-Handling Policies in Concurrent Systems"
4. Visibility: Private (for academic work)
5. Initialize with README: Yes
6. Click "Create repository"

### Screenshot Checklist:
- [ ] Repository creation page
- [ ] Repository home page showing initial commit

---

## Step 2: Clone and Setup Local Repository

### Commands:
```bash
git clone https://github.com/YOUR_USERNAME/coen448-assignment1.git
cd coen448-assignment1

# Copy project files
cp -r /path/to/assignment/src .
cp -r /path/to/assignment/docs .
cp /path/to/assignment/pom.xml .
cp /path/to/assignment/README.md .

# Initial commit
git add .
git commit -m "Initial project structure with Maven and documentation"
git push origin main
```

### Screenshot Checklist:
- [ ] Initial commit in GitHub showing file structure
- [ ] Repository file browser showing all files

---

## Step 3: Create GitHub Issues

### Issue #1: Implement Fail-Fast Policy

**Title**: Task A - Implement Fail-Fast (Atomic) Policy

**Description**:
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

**Labels**: `enhancement`, `task-a`, `priority-high`

---

### Issue #2: Implement Fail-Partial Policy

**Title**: Task B - Implement Fail-Partial (Best-Effort) Policy

**Description**:
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

**Labels**: `enhancement`, `task-b`, `priority-high`

---

### Issue #3: Implement Fail-Soft Policy

**Title**: Task C - Implement Fail-Soft (Fallback) Policy with Risk Documentation

**Description**:
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

**Labels**: `enhancement`, `task-c`, `documentation`, `priority-high`

---

### Issue #4: Comprehensive Unit Testing

**Title**: Create JUnit 5 Test Suite for All Policies

**Description**:
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

**Labels**: `testing`, `priority-critical`

---

### Screenshot Checklist:
- [ ] Issues page showing all 4 issues
- [ ] Individual issue pages with full descriptions
- [ ] Issues with appropriate labels

---

## Step 4: Create Feature Branches

### Branch 1: feature/fail-fast-policy

**Commands**:
```bash
git checkout -b feature/fail-fast-policy

# Implement Microservice.java
# Implement fail-fast in AsyncProcessor.java
# Add tests

git add src/main/java/com/coen448/concurrent/Microservice.java
git commit -m "Add Microservice class with async operations"

git add src/main/java/com/coen448/concurrent/AsyncProcessor.java
git commit -m "Implement Fail-Fast policy in AsyncProcessor"

git add src/test/java/com/coen448/concurrent/AsyncProcessorTest.java
git commit -m "Add Fail-Fast unit tests with timeout protection"

git push origin feature/fail-fast-policy
```

---

### Branch 2: feature/fail-partial-policy

**Commands**:
```bash
git checkout main
git checkout -b feature/fail-partial-policy

# Implement fail-partial in AsyncProcessor.java
git add src/main/java/com/coen448/concurrent/AsyncProcessor.java
git commit -m "Implement Fail-Partial policy with error markers"

# Add tests
git add src/test/java/com/coen448/concurrent/AsyncProcessorTest.java
git commit -m "Add comprehensive Fail-Partial tests"

git push origin feature/fail-partial-policy
```

---

### Branch 3: feature/fail-soft-policy

**Commands**:
```bash
git checkout main
git checkout -b feature/fail-soft-policy

# Implement fail-soft in AsyncProcessor.java
git add src/main/java/com/coen448/concurrent/AsyncProcessor.java
git commit -m "Implement Fail-Soft policy with fallback"

# Add documentation
git add docs/failure-semantics.md
git commit -m "Add extensive risk documentation for Fail-Soft"

# Add tests
git add src/test/java/com/coen448/concurrent/AsyncProcessorTest.java
git commit -m "Add Fail-Soft unit tests"

git push origin feature/fail-soft-policy
```

---

### Screenshot Checklist:
- [ ] GitHub branches page showing all feature branches
- [ ] Network graph showing branch structure
- [ ] Commit history for each branch

---

## Step 5: Create Pull Requests

### Pull Request #1: Fail-Fast Implementation

**Title**: Implement Fail-Fast (Atomic) Exception Handling Policy

**Description**:
```
## Summary
This PR implements the Fail-Fast (Atomic) exception handling policy as specified in Task A.

## Changes
- ✅ Added `Microservice` class with asynchronous operations
- ✅ Implemented `processAsyncFailFast()` method
- ✅ Uses `CompletableFuture.allOf()` for coordination
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

**Reviewers**: Assign peer reviewer
**Labels**: `enhancement`, `task-a`
**Base**: main
**Compare**: feature/fail-fast-policy

---

### Pull Request #2: Fail-Partial Implementation

**Title**: Implement Fail-Partial (Best-Effort) Policy

**Description**:
```
## Summary
This PR implements the Fail-Partial (Best-Effort) exception handling policy as specified in Task B.

## Changes
- ✅ Implemented `processAsyncFailPartial()` method
- ✅ Per-service error handling with `exceptionally()`
- ✅ Clear error markers: `[FAILED: serviceId]`
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

**Reviewers**: Assign peer reviewer
**Labels**: `enhancement`, `task-b`
**Base**: main
**Compare**: feature/fail-partial-policy

---

### Pull Request #3: Fail-Soft with Documentation

**Title**: Implement Fail-Soft Policy with Comprehensive Risk Documentation

**Description**:
```
## Summary
This PR implements the Fail-Soft (Fallback) exception handling policy with extensive risk documentation as specified in Task C.

## Changes
- ✅ Implemented `processAsyncFailSoft()` method
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

**Reviewers**: Assign peer reviewer
**Labels**: `enhancement`, `task-c`, `documentation`
**Base**: main
**Compare**: feature/fail-soft-policy

---

### Screenshot Checklist:
- [ ] Pull requests page showing all 3 PRs
- [ ] Individual PR pages with full descriptions
- [ ] PR showing "Files changed" tab
- [ ] PR conversation with review comments

---

## Step 6: Code Review Process

### Reviewer Actions for Each PR:

1. **Review Code Changes**
   - Check "Files changed" tab
   - Review implementation logic
   - Verify exception handling
   - Check test coverage

2. **Add Review Comments**

Example comments to add:

**For PR #1 (Fail-Fast)**:
```
Line 45 in AsyncProcessor.java:
"Good use of CompletableFuture.allOf(). Exception propagation is correct."

Line 80 in AsyncProcessor.java:
"Consider adding a comment explaining why join() is safe here (because allOf already completed)."

General comment:
"LGTM! The Fail-Fast implementation correctly handles the all-or-nothing semantics. Tests are comprehensive and properly use assertThrows."
```

**For PR #2 (Fail-Partial)**:
```
Line 95 in AsyncProcessor.java:
"Nice use of exceptionally() for per-service error handling. Error markers are clear and consistent."

Line 120 in AsyncProcessorTest.java:
"Good test coverage of partial failure scenarios."

General comment:
"Approved! The error markers make it easy to identify which services failed. Tests verify all edge cases."
```

**For PR #3 (Fail-Soft)**:
```
Line 145 in AsyncProcessor.java:
"Excellent documentation of risks in the Javadoc. The warnings are clear and prominent."

Line 200 in failure-semantics.md:
"The risk documentation is very thorough. Good examples of when NOT to use this policy."

General comment:
"Approved with praise! The risk documentation is exactly what users need to make informed decisions about using Fail-Soft. Required safety measures are clearly stated."
```

3. **Approve PRs**
   - Submit review with "Approve"
   - Add final comment
   - Merge to main

---

### Screenshot Checklist:
- [ ] Code review comments in "Files changed"
- [ ] Conversation tab showing reviewer feedback
- [ ] Review approval
- [ ] Merge confirmation

---

## Step 7: Merge Pull Requests

For each PR:
1. Ensure all checks pass
2. Ensure review is approved
3. Click "Merge pull request"
4. Confirm merge
5. Delete feature branch (optional)
6. Close related issue

### Commands to Sync Local:
```bash
git checkout main
git pull origin main
```

---

## Final Repository State

After all merges, the main branch should contain:

```
coen448-assignment1/
├── src/
│   ├── main/java/com/coen448/concurrent/
│   │   ├── Microservice.java
│   │   └── AsyncProcessor.java (all 3 policies)
│   └── test/java/com/coen448/concurrent/
│       └── AsyncProcessorTest.java (25+ tests)
├── docs/
│   └── failure-semantics.md (comprehensive)
├── pom.xml
├── README.md
└── .gitignore
```

---

## Screenshot Collection for Report

### Required Screenshots:

1. **GitHub Issues** (minimum 3 images)
   - Issues list showing all 4 issues
   - Issue #1 detail page
   - Issue #3 detail page with documentation label

2. **Feature Branches** (minimum 2 images)
   - Branches page showing all feature branches
   - Network graph showing branch/merge structure

3. **Pull Requests** (minimum 2 images)
   - Pull requests page showing all 3 PRs (merged)
   - PR #3 detail with review comments

4. **Code Review** (minimum 1 image)
   - Code review comments on PR
   - Review approval

---

## Tips for Screenshots

1. Use full browser window (not just partial)
2. Ensure GitHub username is visible
3. Show commit SHAs and dates
4. Capture entire conversations
5. Show green "Merged" badges
6. Include timestamps on reviews

---

## Verification Checklist

Before submission, verify:

- [ ] At least 3 distinct GitHub issues created
- [ ] All issues have proper descriptions and labels
- [ ] At least 2 feature branches visible in repository
- [ ] At least 2 pull requests created and merged
- [ ] At least 1 code review with meaningful comments
- [ ] All PRs show "Merged" status
- [ ] Main branch has all code
- [ ] Screenshots clearly show all required elements
- [ ] Screenshots are high quality and readable

---

## Report Integration

Include screenshots in the PDF report in this order:

1. **Introduction**: Repository overview screenshot
2. **Issues Section**: Screenshots of issues
3. **Branches Section**: Branch and network graph screenshots
4. **Pull Requests Section**: PR list and detail screenshots
5. **Code Review Section**: Review comment screenshots

Add captions to each screenshot explaining what it shows.

---

**Document Created**: February 2026
**Assignment**: COEN448/6761 Assignment 1
**Purpose**: GitHub workflow guidance and screenshot instructions
