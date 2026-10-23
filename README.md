# Course Waitlist — AI-Assisted Engineering Assessment

A deliberately small Java 21 / Spring Boot REST service implementing one course offering's enrollment, drop, and FIFO waitlist promotion workflow.

## Requirements
- Java 21
- Maven 3.9+

## Run
```bash
mvn spring-boot:run
```
The app uses an in-memory H2 database; data is reset when the application process stops. H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:coursewaitlist`, username `sa`, blank password).

## Test
```bash
mvn test
```

## API examples
Create an offering:
```bash
curl -X POST http://localhost:8080/api/courses \
  -H 'Content-Type: application/json' \
  -d '{"name":"Distributed Systems","capacity":2}'
```
Enroll (or join waitlist if full):
```bash
curl -X POST http://localhost:8080/api/courses/1/enrollments \
  -H 'Content-Type: application/json' \
  -d '{"learnerId":"alice","prerequisiteSatisfied":true}'
```
Inspect capacity and counts:
```bash
curl http://localhost:8080/api/courses/1
```
Drop and trigger promotion:
```bash
curl -X DELETE http://localhost:8080/api/courses/1/enrollments/alice
```

## Scope and assumptions
- One offering per request, with a capacity set when the offering is created.
- The caller supplies the learner identifier and whether the prerequisite is satisfied. Identity management and a learner/course catalog are outside this assessment's scope.
- A learner may be enrolled or waitlisted at most once per offering. Database unique constraints reinforce this invariant.
- An ineligible learner is rejected before enrollment or waitlisting. If a queued learner is later considered ineligible, promotion skips that entry but retains it in its existing position and continues to the next eligible entry.
- FIFO is determined by waitlist creation timestamp, with the database-generated entry ID as a stable tie-breaker.
- Every enrollment/drop workflow locks the course row inside a transaction so operations for the same offering serialize. Enrollment and waitlist uniqueness constraints are additional safeguards.
- H2 is an in-memory database for the assessment; persistence across restarts is not provided.

## Limitations / follow-up risks
- The request's prerequisite flag is a small assessment boundary, not a production authorization or prerequisite-verification system.
- Concurrency behavior should be exercised against the target database as well as H2 before production use. This project includes a pessimistic course-row lock, but does not claim a cross-database concurrency test.
- No authentication, pagination, course catalog, frontend, deployment, or external prerequisite service is included because these are outside the requested scope.
