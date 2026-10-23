# Course Waitlist API — AI-Assisted Engineering Assessment

A REST API built with Java 21 and Spring Boot for managing course enrollment, prerequisite validation, enrollment drops, and FIFO waitlist promotion for a single course offering.

## Technologies

- **Java 21** — programming language
- **Spring Boot 3.4.5** — application framework
- **Spring Web** — REST API development
- **Spring Data JPA** — persistence and data access
- **H2 Database** — in-memory relational database
- **Maven** — build and dependency management
- **JUnit 5 and Spring Boot Test** — automated testing

## Prerequisites

Ensure Java 21 and Maven are installed and available in your terminal.

Verify your environment:

```bash
java -version
mvn -version
```

## Getting Started

### Run the application

From the project root, execute:

```bash
mvn spring-boot:run
```

The application starts locally on port `8080`, unless configured otherwise. It uses an in-memory H2 database, so application data is not intended to persist after shutdown.

### Run automated tests

Execute the test suite from the project root:

```bash
mvn test
```

## Database Console

While the application is running, open the H2 Console:

[http://localhost:8080/h2-console](http://localhost:8080/h2-console)

Use the following connection settings:

| Setting | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:coursewaitlist;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE` |
| User Name | `sa` |
| Password | `12345` |

These settings correspond to the application's local configuration.

## API Usage

The examples below assume that the application is running on `http://localhost:8080`. Use the course ID returned by the create-course request in subsequent requests. The examples use course ID `1` for illustration.

### 1. Create a course offering

```bash
curl -X POST http://localhost:8080/api/courses \
  -H 'Content-Type: application/json' \
  -d '{"name":"Distributed Systems","capacity":2}'
```

Creates a course offering with a capacity of two learners.

### 2. Enroll a learner or join the waitlist

```bash
curl -X POST http://localhost:8080/api/courses/1/enrollments \
  -H 'Content-Type: application/json' \
  -d '{"learnerId":"alice","prerequisiteSatisfied":true}'
```

An eligible learner is enrolled when a seat is available. If the course is full, the learner is added to the waitlist.

The `prerequisiteSatisfied` field represents the prerequisite status supplied by the caller.

### 3. Retrieve course information

```bash
curl http://localhost:8080/api/courses/1
```

Retrieves the course information and registration state exposed by the API.

### 4. Drop an enrollment

```bash
curl -X DELETE \
  http://localhost:8080/api/courses/1/enrollments/alice
```

Dropping an existing enrollment frees a seat and triggers promotion of the earliest eligible learner in the waitlist, if one is available.

## Business Rules and Design Decisions

- **Capacity:** Enrollment is limited to the capacity configured when a course offering is created.
- **Duplicate prevention:** A learner cannot be enrolled or waitlisted more than once for the same course offering.
- **Prerequisites:** Learners who do not satisfy the prerequisite are rejected rather than enrolled or newly waitlisted.
- **FIFO promotion:** Waitlist promotion follows creation order, using the creation timestamp and database-generated ID as a tie-breaker.
- **Eligibility during promotion:** Ineligible queued entries are skipped and retained. The service continues searching for the earliest eligible learner.
- **Transactional consistency:** Enrollment and drop workflows use transactions and a pessimistic lock on the course row. Database uniqueness constraints provide additional protection against duplicate records.

## Scope and Assumptions

This implementation focuses on enrollment, drop handling, prerequisite validation, and waitlist promotion for a single course offering.

The following are outside the assessment's scope:

- Authentication and identity management
- A learner or course catalog integration
- A frontend or user interface
- Payments and external prerequisite services
- Pagination and deployment configuration

The API accepts the learner ID and prerequisite-satisfaction flag from the caller. It does not independently authenticate the learner or verify prerequisite status against an external system.

## Limitations and Production Considerations

- **Eligibility validation:** A production implementation should determine prerequisite eligibility using a trusted source rather than relying on a client-provided boolean.
- **Persistence:** H2 is configured as an in-memory database for local execution and assessment purposes.
- **Concurrency:** The application uses transactional locking, but concurrency behavior should be validated against the intended production database and deployment configuration before production use.
- **Integration testing:** Automated tests provide evidence for the scenarios they exercise; they do not establish correctness for every database, deployment, or failure condition.

This project is an assessment implementation, not a production-ready enrollment
