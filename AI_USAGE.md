# AI-Assisted Engineering Use Record

## 1. Purpose

AI was used as an engineering assistant during requirement analysis, implementation, debugging, testing preparation, and documentation review. It contributed to implementation suggestions and code examples, as well as explanations of business rules and API behavior.

AI-generated output was treated as a starting point rather than as automatically correct. The implementation was reviewed against the assessment requirements, an incorrect request-field example was corrected after observing application behavior, and the existing automated test suite was executed locally.

The assessment requirements remained the source of truth for scope and acceptance criteria.

## 2. Activity 1 — Requirement Analysis

**Prompt / request:**

Review the Course Waitlist AI-Assisted Engineering Assessment and identify the required implementation scope, acceptance criteria, prerequisite and waitlist policy decisions, tests, and deliverables.

**AI contribution:**

- Helped break down enrollment, dropping, FIFO waitlist promotion, prerequisite validation, duplicate prevention, and invalid-drop handling into implementation requirements.
- Suggested documenting how ineligible learners are handled during promotion.
- Suggested focused automated tests and a proportionate design specification.
- Identified the need to record AI usage and verification evidence.

**Decision:** Accepted the requirement-decomposition suggestions as planning guidance. The assessment remained authoritative when determining scope and acceptance criteria.

**Verification:** Compared the proposed scope against the assessment requirements and used the identified behaviors to guide implementation and test coverage.

## 3. Activity 2 — Implementation Assistance and Debugging

**Prompt / request:**

Help design and implement a minimal Java 21, Spring Boot, H2, and JUnit-based application supporting course creation, enrollment, dropping, prerequisite checks, duplicate prevention, FIFO waitlist promotion, and transactional behavior.

**AI contribution:**

AI provided implementation guidance and code examples for the REST API workflow, service-layer business rules, persistence structure, transaction handling, and testing scenarios. It also helped explain request payloads and troubleshoot an enrollment request that did not behave as expected.

**Decision:** Used AI-generated implementation guidance as a starting point, then reviewed the behavior through manual API checks and automated tests. Suggestions were adjusted when they did not match the actual application contract or verification evidence.

**Manual verification performed:**

- Created a course offering through the REST API.
- Tested learner enrollment through Postman.
- Tested a duplicate enrollment request.
- Added additional learners to exercise the full-course and waitlist workflow.

These manual checks are limited to the scenarios actually exercised. The automated tests provide additional coverage for the business rules.

**Correction made — request field name:**

An initially suggested request example used `hasPrerequisite`, but the implementation expects `prerequisiteSatisfied`. The mismatched property did not set the expected prerequisite flag, so the request behaved as though the prerequisite was not satisfied.

The request was corrected to:

```json
{
  "learnerId": "Alice",
  "prerequisiteSatisfied": true
}
```

The corrected request worked as expected, and the README example was updated to use the implementation's actual field name.

**Decision:** Edited the incorrect example rather than retaining the original suggestion. This was a concrete example of checking AI-assisted output against observed application behavior.

## 4. Activity 3 — Documentation Review

**Prompt / request:**

Review the README, design specification, and AI usage record for inconsistencies and readiness against the assessment requirements.

**AI contribution:**

- Identified a mismatch between the README's H2 console password instructions and the application configuration.
- Recommended documenting the configured JDBC URL and password consistently.
- Suggested making service responsibilities, capacity invariants, FIFO ordering, and the ineligible-waitlist policy explicit.
- Recommended distinguishing implemented behavior from assumptions, limitations, and unverified scenarios.
- Recommended recording actual test results rather than leaving verification placeholders.

**Decision:** Accepted and adapted the documentation suggestions where they matched the implementation. Documentation claims were kept within the available code and test evidence.

**Verification:** Reviewed the documentation against the application configuration, source structure, and assessment requirements.

## 5. Automated Verification

**Command executed:**

```bash
mvn clean test
```

**Observed result on 23 October 2026:**

```text
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

**Environment note:** The initial test attempt failed because the terminal and Maven were using Java 17 while the project targets Java 21. After configuring the environment to use Java 21, the test run completed successfully.

**Interpretation:** The nine tests in the successful test run passed, including the existing service tests and the concurrent-enrollment test.

**Concurrency test:** The test submits six eligible learner requests concurrently against a course with capacity one. It verifies that exactly one learner is enrolled and five learners are waitlisted. This is evidence for the tested H2 configuration and scenario; it does not prove correctness for every production database or concurrency scenario.

**Additional verification evidence:**

- Reviewed the Maven test output and confirmed zero failures and errors.
- Manually exercised course creation, learner enrollment, and a duplicate enrollment attempt through the REST API.
- Corrected the enrollment request field name after observing the prerequisite-validation behavior.
- Reviewed the documented API fields and H2 configuration for consistency with the application.

The automated test suite provides service-level coverage. Separate HTTP-layer integration tests have not been verified.

## 6. Final Review Checklist

- [x] Reviewed the documented API fields against the request field used during manual API testing.
- [x] Checked the H2 console settings against `application.properties`.
- [x] Ran the automated test suite successfully using Java 21.
- [x] Confirmed that the successful test run reported nine tests, zero failures, zero errors, and zero skipped tests.
- [x] Reviewed the core capacity, duplicate-registration, prerequisite, FIFO-promotion, and invalid/repeated-drop test coverage.
- [x] Added and executed a concurrent-enrollment test for a course with capacity one.
- [ ] Complete the final source and documentation diff review before submission.

## 7. Limitations and Follow-up Risks

- The prerequisite-satisfaction value is supplied by the API caller. It is not a production-grade eligibility verification mechanism.
- H2 is configured as an in-memory database and does not provide durable persistence across application restarts.
- The service uses transactional workflows and pessimistic course-row locking. The concurrent-enrollment test verifies the tested H2 scenario, but concurrency behavior should still be validated against the intended production database.
- The current implementation does not refresh prerequisite eligibility for existing waitlist entries.
- The existing automated tests focus on service-level behavior. Separate HTTP-layer integration tests have not been verified.
- Manual Postman checks supplement, but do not replace, automated tests.
- Mockito emitted a dynamic-agent attachment warning during the successful test run. The warning did not cause the tests to fail; explicit test-agent configuration may be considered for future JDK compatibility.

