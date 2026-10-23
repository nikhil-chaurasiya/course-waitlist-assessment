# AI-Assisted Engineering Use Record

## 1. Purpose

AI assistance was used to help interpret the assessment, plan a minimal implementation, review API usage, and improve project documentation. The assessment requirements remained the source of truth. Generated suggestions were treated as proposals and checked against the implementation and observed behavior.

## 2. Activity 1 — Requirement decomposition

**Prompt / request:**  
Review the Course Waitlist AI-Assisted Engineering Assessment and explain the minimum implementation scope, acceptance criteria, policy decisions, tests, and required deliverables.

**AI contribution:**
- Identified enrollment, dropping, FIFO waitlist promotion, prerequisite validation, duplicate prevention, and invalid-drop behavior as core workflow concerns.
- Recommended documenting the prerequisite and ineligible-waitlist policy explicitly.
- Recommended focused automated tests and a concise design specification.
- Identified the need to document AI usage and actual verification evidence.

**Decision:**  
Used the suggestions as a planning aid while keeping the assessment instructions authoritative.

**Verification:**  
Compared the proposed scope against the assessment requirements and kept the implementation focused on the required registration workflow.

## 3. Activity 2 — Implementation assistance

**Prompt / request:**  
Create and explain a minimal Java 21, Spring Boot, H2, and JUnit implementation for course creation, enrollment, dropping, prerequisite checks, duplicate prevention, FIFO waitlist promotion, and transaction handling.

**AI contribution:**  
Helped structure the REST API workflow, explain request payloads, and identify practical manual and automated verification scenarios.

**Decision:**  
Used the proposed implementation as a starting point and checked the API behavior manually rather than assuming generated code was correct.

**Observed manual checks:**
- Created a course offering using the REST API.
- Tested learner enrollment using Postman.
- Tested a duplicate enrollment attempt.
- Continued with additional learners to exercise the full-course/waitlist scenario.

**Important correction:**  
An initially suggested request example used `hasPrerequisite`, but the implementation expects `prerequisiteSatisfied`. The mismatched property caused the request to behave as though the prerequisite had not been satisfied. The request body was corrected to use `prerequisiteSatisfied`, after which the enrollment request worked as expected.

Correct request example:


{
  "learnerId": "Alice",
  "prerequisiteSatisfied": true
}


This correction was carried into the README API example.

## 4. Activity 3 — Documentation review

**Prompt / request:**  
Review the repository README, design specification, and AI usage record for inconsistencies and assessment readiness.

**AI contribution:**
- Identified a mismatch between the README's H2 password instructions and `application.properties`.
- Recommended documenting the configured JDBC URL and password consistently.
- Recommended making the design document explicit about invariants, FIFO ordering, and the policy for ineligible waitlist entries.
- Recommended replacing this template-style AI record with a truthful account of assistance, corrections, and verification evidence.

**Decision:**  
Accepted the documentation improvements, subject to checking the final files against the committed implementation.

## 5. Automated verification

**Command run:**

mvn test

**Observed result:**  
[Replace this section with the actual Maven output. Include the number of tests run, failures/errors, and the final BUILD SUCCESS or BUILD FAILURE result. Do not claim success until the command has completed successfully.]

**Additional tests or checks performed:**  
[Record only checks actually performed. If the drop/promotion, prerequisite failure, repeated drop, or FIFO scenarios have not been verified, say so and test them before submission.]

## 6. Final review

Before submission, verify that:

- [ ] The documented API fields and endpoints match the implementation.
- [ ] The H2 console settings match `application.properties`.
- [ ] Automated tests run successfully and the observed output is recorded above.
- [ ] Capacity limits, duplicate registration, prerequisite failure, FIFO promotion, and invalid/repeated drops are covered by tests.
- [ ] Any concurrency test claimed here exists and has actually been run.
- [ ] The final source and documentation diff has been reviewed.

## 7. Limitations

- The prerequisite-satisfaction value is supplied by the caller; it is not a production-grade eligibility verification mechanism.
- H2 is configured as an in-memory database.
- Concurrency behavior should be verified against the intended production database before production use.
- Manual Postman checks do not replace automated tests.
