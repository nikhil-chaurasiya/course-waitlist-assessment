# Design Specification

## 1. Scope

This service implements the registration workflow for a single course offering:

- Create a course offering with a fixed capacity.
- Enroll an eligible learner when a seat is available.
- Add an eligible learner to the waitlist when the course is full.
- Drop an existing enrollment and promote the earliest eligible waitlisted learner when possible.
- Prevent duplicate registration records and enforce prerequisite eligibility.

The implementation deliberately excludes authentication, a course catalog, a frontend, payment processing, and external prerequisite integrations.

## 2. Service Architecture

`RegistrationController` exposes the HTTP API and delegates registration decisions to `RegistrationService`.

`RegistrationService` coordinates enrollment, dropping, and waitlist promotion. Spring Data repositories persist course offerings, enrollments, and waitlist entries.

The relational database enforces uniqueness constraints, while service-level validation and transaction boundaries protect registration workflows.

The implementation follows a layered architecture, uses the Repository pattern for persistence access, and uses dependency injection to connect application components.

## 3. Data Model

### `CourseOffering`

Represents a course offering and includes its identifier, name, and capacity.

Capacity must be positive. The number of enrolled learners must never exceed capacity.

### `Enrollment`

Represents an active enrollment and contains a course-offering reference, learner ID, and creation timestamp.

A unique constraint on `(course_id, learner_id)` prevents duplicate enrollment records for the same learner and offering.

### `WaitlistEntry`

Represents a learner waiting for a seat and contains a course-offering reference, learner ID, prerequisite status captured at request time, and creation timestamp.

A unique constraint on `(course_id, learner_id)` prevents duplicate waitlist records for the same learner and offering.

The service also ensures that a learner is not simultaneously enrolled and waitlisted for the same offering.

## 4. API Surface

- `POST /api/courses` — create a course offering.
- `GET /api/courses/{courseId}` — inspect a course offering and the registration information exposed by the API.
- `POST /api/courses/{courseId}/enrollments` — enroll an eligible learner or add them to the waitlist when the course is full.
- `DELETE /api/courses/{courseId}/enrollments/{learnerId}` — drop an enrollment and attempt waitlist promotion.

The enrollment request uses `learnerId` and `prerequisiteSatisfied`. The caller supplies these values in this prototype. A production service should derive identity and prerequisite eligibility from trusted systems.

## 5. Business Rules and Invariants

1. The enrollment count must never exceed course capacity.
2. A learner may have at most one active enrollment per offering.
3. A learner may have at most one waitlist entry per offering.
4. A learner must not be both enrolled and waitlisted for the same offering.
5. A learner who fails the prerequisite check must not be enrolled or newly added to the waitlist.
6. Dropping a learner who is not currently enrolled must not change registration state.
7. A successful drop and any resulting promotion must be handled atomically.
8. A promotion must not exceed the available course capacity.

## 6. Waitlist and Prerequisite Policy

Eligible learners are considered in FIFO order. Waitlist entries are ordered by creation timestamp, with the database-generated entry ID used as a tie-breaker.

When a seat becomes available, the service scans the waitlist in that order:

1. Check the earliest queued entry.
2. If the learner is eligible, remove the waitlist entry, create the enrollment, and stop after one promotion.
3. If the learner is ineligible, skip that entry without deleting it and continue to the next entry.
4. If no eligible entry is found, leave the seat available.

The API rejects a learner who fails the prerequisite check when they initially attempt to register.

The prerequisite-satisfaction flag is captured when a learner joins the waitlist. The current implementation does not provide a mechanism to refresh or update eligibility later. During promotion, the service uses the stored flag: ineligible entries are skipped and retained, while the earliest eligible entry is promoted.

## 7. Transactions and Concurrency

Enrollment and drop workflows acquire a pessimistic write lock on the course row inside a transaction before reading registration state and applying changes.

This is intended to serialize registration operations for the same course offering and protect capacity checks and drop/promotion updates from conflicting writes.

Database uniqueness constraints provide additional safeguards against duplicate records.

A focused concurrent-enrollment test submits six eligible learner requests against a course with capacity one. It verifies that exactly one learner is enrolled and the remaining five learners are waitlisted.

This test provides evidence for the tested H2 configuration and scenario. Concurrency behavior should still be validated against the intended production database before production use.

## 8. Trade-offs and Limitations

- H2 in-memory storage keeps local setup simple, but data is not durable across application restarts.
- Prerequisite eligibility is represented by a request field rather than a trusted eligibility service.
- The implementation is limited to the registration workflow required for this assessment.
- The current application does not refresh prerequisite eligibility for existing waitlist entries.
- The automated tests focus on service-level behavior; separate HTTP-layer integration tests have not been verified.
- Production readiness would require further validation of concurrency behavior, error handling, observability, persistence, and integration with trusted identity and prerequisite systems.
