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

## Project Status

The repository contains the core student/recruiter workflow, coding assessment flow, code execution, submission storage and review indicators. Before production deployment, run the complete backend build, frontend build, database integration tests and end-to-end browser tests with production configuration.
