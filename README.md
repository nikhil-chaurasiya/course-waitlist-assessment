# Course Waitlist — AI-Assisted Engineering Assessment

A small Java 21 and Spring Boot REST API implementing enrollment, dropping, prerequisite validation, and FIFO waitlist promotion for a single course offering.



## Technologies Used

- **Java 21** — primary programming language
- **Spring Boot 3.4.5** — application framework
- **Spring Web** — REST API endpoints
- **Spring Data JPA** — database access and repository operations
- **H2 Database** — in-memory relational database
- **Maven** — dependency management and build
 

## Run the application

From the project root, run:

mvn spring-boot:run


The application uses an in-memory H2 database. Data is not intended to persist after the application stops.

### H2 Console

Open:

http://localhost:8080/h2-console

Use these connection settings:

| Setting | Value |
|---|---|
| JDBC URL | jdbc:h2:mem:coursewaitlist;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE |
| User Name | sa |
| Password | 12345 |




## Run tests

From the project root:

mvn test



## API examples

The examples below assume the application is running locally. The course ID in subsequent requests must match the ID returned when you create a course.


### 1. Create a course offering


curl -X POST http://localhost:8080/api/courses \
  -H 'Content-Type: application/json' \
  -d '{"name":"Distributed Systems","capacity":2}'
  

### 2. Enroll a learner or join the waitlist


curl -X POST http://localhost:8080/api/courses/1/enrollments \
  -H 'Content-Type: application/json' \
  -d '{"learnerId":"alice","prerequisiteSatisfied":true}'


If a course has an available seat, an eligible learner can be enrolled. If the course is full, an eligible learner can be added to the waitlist.



### 3. Inspect the course

curl http://localhost:8080/api/courses/1

Use the response to inspect the course state and registration counts exposed by the API.


### 4. Drop an enrollment


curl -X DELETE http://localhost:8080/api/courses/1/enrollments/alice

A successful drop should free a seat and trigger promotion of the earliest eligible learner in the waitlist, if one exists.



## Scope and assumptions

- The service manages one course offering per request. Capacity is set when the offering is created.
- The caller supplies the learner ID and prerequisite-satisfaction flag. Authentication, identity management, and a learner/course catalog are outside this assessment's scope.
- A learner must not be enrolled or waitlisted more than once for the same course offering.
- A learner who does not satisfy the prerequisite is rejected rather than enrolled or newly waitlisted.
- Waitlist promotion follows FIFO order, using creation timestamp and database-generated ID as a tie-breaker.
- During promotion, an ineligible queued entry is skipped and retained in its existing position; the service continues looking for the earliest eligible learner.
- Enrollment and drop workflows use a transaction and a pessimistic lock on the course row. Database uniqueness constraints provide additional protection against duplicate records.
- H2 is used as a local in-memory database. This is a prototype, not a production persistence configuration.



## Limitations and follow-up risks

- The prerequisite flag is supplied by the API caller. A production system should determine eligibility using a trusted source rather than trusting a client-provided boolean.
- Concurrency behavior should be tested against the intended production database before production use. The presence of a lock does not by itself prove correctness under every database or deployment configuration.
- The application does not include authentication, a frontend, pagination, an external prerequisite service, or deployment configuration because these are outside the assessment's core scope.
- Automated test claims should be based on the actual output of `mvn test`; manual API checks are useful but do not replace automated tests.
