# Code Authenticity Verification System

A full-stack coding assessment platform built with **Java, Spring Boot, MySQL, React and Vite**. The system lets recruiters create coding assessments and assign them to students, while students can solve, execute and submit code. Recruiters can review submissions using execution results and basic behavior/source-code indicators.

## Main Features

### Student
- Register and log in securely
- View assigned assessments
- Open coding questions
- Write and execute Java code
- Run code against test cases
- Submit solutions
- Record assessment behavior events
- View execution/submission results

### Recruiter
- Register and log in
- Create and manage coding questions
- Add public and hidden test cases
- Create assessments
- Assign assessments to students
- Review submitted code
- Review execution and behavior indicators
- Identify identical normalized source-code matches within recruiter assessments

## Technology Stack

**Frontend**
- React
- Vite
- JavaScript
- Axios
- React Router

**Backend**
- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA / Hibernate
- Spring Security
- Jakarta Validation

**Database**
- MySQL

**Development / Testing**
- IntelliJ IDEA
- Maven
- Postman
- Git / GitHub

## Architecture

```text
React + Vite Frontend
        |
        | HTTP / JSON
        v
Spring Boot REST API
        |
        +--> Authentication / Session
        +--> Candidate / User Management
        +--> Questions
        +--> Assessments
        +--> Code Execution
        +--> Submissions / Review
        |
        v
      MySQL
```

## Important Security Notes

- Passwords are stored using BCrypt hashing.
- Application secrets and database credentials must be supplied through local environment/configuration and must not be committed to GitHub.
- The current development configuration uses a Vite frontend origin for local CORS.
- The current authenticity checks are **review indicators**, not proof of AI use or misconduct.

## Local Setup

### Backend

1. Install JDK 21 and Maven.
2. Create a MySQL database.
3. Configure the database connection in the local `application.properties` file or environment configuration.
4. Start the Spring Boot application.

### Frontend

1. Install Node.js and npm.
2. Open the `vericode-frontend` directory.
3. Install dependencies:

```bash
npm install
```

4. Start the development server:

```bash
npm run dev
```

The frontend is configured for the Vite development environment.

## API Areas

- `/Auth` — registration, login, session and logout
- `/Candidates` — candidate management
- `/Questions` — question management
- `/Assessments` — assessment creation and retrieval
- `/CodeExecution` — code execution and evaluation
- `/Submissions` — solution submission and recruiter review
- `/Assessments/{id}/attempt` — retrieve the server-timed attempt after opening an assessment
- `/Assessments/{id}/start` — start once the student confirms the recording notice

## Consent, timing, and database changes

Before a student starts, the app explains that it records code edit activity, paste events, code runs, and when the assessment window loses focus or visibility. The student must confirm before the server creates an attempt. The server stores the consent and start times, calculates the deadline from the assessment duration, and returns the remaining time when the student reopens the assessment. A submission is accepted through the deadline plus a 10-second grace period.

Flyway applies `V1__create_assessment_attempts.sql`. On the existing `code_authentication` database, Flyway baselines the current schema at version 0 and adds the new `assessment_attempts` table and indexes. It does not drop, truncate, or recreate existing tables. Flyway's clean operation is disabled.

Question and test-case read results use a bounded five-minute in-memory cache. Recruiter edits and deletions clear the affected cache entries. Hibernate batch fetching reduces repeated relationship queries when loading assessment lists.

## Hosting configuration

Keep database credentials in environment variables or a private deployment secret store. The backend accepts `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`. Set `CORS_ALLOWED_ORIGINS` to the deployed frontend origin or comma-separated origins. Set `VITE_API_BASE_URL` to the public backend URL before building the frontend; Vite embeds this value in the production bundle. The backend uses HikariCP for connection pooling and enables gzip compression for larger responses.

For a Windows local test run, open PowerShell in `vericode-backend` and run:

```powershell
.\mvnw.cmd test
```

Then, in `vericode-frontend`, install the npm packages and run `npm run build` to create the production frontend bundle.

## Project Status

The repository contains the core student/recruiter workflow, coding assessment flow, code execution, submission storage and review indicators. Before production deployment, run the complete backend build, frontend build, database integration tests and end-to-end browser tests with production configuration.
