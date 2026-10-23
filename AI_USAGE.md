# AI-Assisted Engineering Use Record

This record is a template to complete honestly while reviewing and verifying the implementation. Do not claim a test or command passed unless you ran it and observed the result.

## Activity 1 — Requirement decomposition
- **Prompt used:** “Read the Course Waitlist AI-Assisted Assessment and identify the minimum implementation scope, acceptance criteria, required deliverables, and policy decisions.”
- **AI contribution:** Summarized enrollment, dropping, FIFO promotion, prerequisite policy, duplicate handling, focused tests, design documentation, and AI-use evidence.
- **Decision:** Accepted as a planning aid, with the assessment PDF remaining authoritative.
- **Verification:** Compared the scope and deliverables against the supplied assessment PDF.

## Activity 2 — Initial implementation assistance
- **Prompt used:** “Create a minimal Java 21 Spring Boot REST API using H2 and JUnit for one course offering, enrollment, dropping, FIFO waitlist promotion, duplicate prevention, prerequisite checks, and transactional handling.”
- **AI contribution:** Drafted a bounded implementation and focused tests.
- **Decision:** Treat generated code as a proposal, not verified truth. Review method-by-method and edit or reject any behavior that conflicts with the assessment.
- **Verification evidence:** Record the exact command and observed result after running `mvn test`; review each test assertion and inspect the final diff before submission.

## Required candidate update before submission
1. Add any follow-up prompts actually used and summarize relevant responses.
2. Record specific suggestions accepted, edited, or rejected and the files/decisions affected.
3. Paste the actual test command and observed output; do not mark verification complete before running it.
4. Mention any defects discovered and how they were corrected.
