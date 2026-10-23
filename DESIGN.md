# Design Specification

## Service boundary
`RegistrationController` exposes REST endpoints and delegates all workflow decisions to `RegistrationService`. The service coordinates persistence through Spring Data repositories. The database stores course offerings, enrollments, and waitlist entries.

## Data model
- `CourseOffering`: ID, name, positive capacity.
- `Enrollment`: offering reference, learner ID, creation time; unique `(course_id, learner_id)`.
- `WaitlistEntry`: offering reference, learner ID, prerequisite status captured at request time, creation time; unique `(course_id, learner_id)`.

The caller supplies learner ID and prerequisite status to keep the prototype bounded; a real system would derive eligibility from trusted learner/course records.

## API
- `POST /api/courses` — create an offering.
- `GET /api/courses/{courseId}` — inspect capacity and counts.
- `POST /api/courses/{courseId}/enrollments` — enroll or waitlist an eligible learner.
- `DELETE /api/courses/{courseId}/enrollments/{learnerId}` — drop an enrollment and promote one eligible learner if possible.

## Invariants
1. Enrollment count never exceeds offering capacity.
2. A learner cannot be both enrolled and waitlisted for the same offering, nor have duplicate rows in either table.
3. Prerequisite failure never creates an enrollment or a new waitlist entry.
4. Dropping a learner who is not enrolled changes no registration state.
5. Successful drop and waitlist promotion occur in one transaction.

## Promotion policy
Waitlist entries are read in ascending creation timestamp and then ascending ID. Promotion scans in FIFO order, promotes the first eligible entry, removes that entry from the waitlist, and stops after one promotion. Ineligible entries are skipped but retained in their original queue position. The current API rejects ineligible learners at initial enrollment; the retention rule covers eligibility becoming false after queue entry or persisted data from an external eligibility source.

## Persistence and concurrency
H2 is used for a simple local relational store. Enrollment and drop acquire a pessimistic write lock on the course row before reading counts or mutating state. The transaction keeps the drop and promotion atomic. Unique database constraints protect against duplicate enrollment/waitlist records. A production database should be tested under concurrent load and configured with appropriate lock timeouts and transaction monitoring.

## Tradeoffs
The prototype deliberately has no user identity system, learner catalog, frontend, or policy engine. Eligibility is a request field to keep the task scoped to the required service slice. The database is in-memory, so data does not survive process restart.
