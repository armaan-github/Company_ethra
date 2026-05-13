# Team Task Manager — Backend API

A full-stack Team Task Manager where users can create projects, assign tasks, and track progress with **role-based access control (Admin/Member)**.

## Tech Stack

- **Backend:** Spring Boot 3.3, Java 23
- **Database:** MySQL 8
- **Security:** Spring Security + JWT (JJWT)
- **ORM:** Spring Data JPA / Hibernate

## Features

- 🔐 JWT-based Authentication (Register / Login)
- 👥 Role-based access control (ADMIN / MEMBER)
- 📁 Project management with team members
- ✅ Task creation, assignment & status tracking (TODO → IN_PROGRESS → DONE)
- 📊 Dashboard with task statistics and overdue tracking
- ⚠️ Global exception handling with structured error responses
- ✔️ Request validation on all endpoints

## API Endpoints

| Module | Endpoints |
|--------|-----------|
| **Auth** | `POST /api/auth/register`, `POST /api/auth/login` |
| **Users** | `GET /api/users/me`, `GET /api/users` |
| **Projects** | `CRUD /api/projects`, member management |
| **Tasks** | `CRUD /api/projects/{id}/tasks`, status updates |
| **Dashboard** | `GET /api/dashboard/stats`, `GET /api/dashboard/my-tasks` |

## Setup

### Prerequisites
- Java 17+
- MySQL 8
- Maven 3.8+

### Run Locally

```bash
# 1. Create database
mysql -u root -p -e "CREATE DATABASE task_manager_db;"

# 2. Update credentials in application.properties

# 3. Build and run
mvn clean install
mvn spring-boot:run
```

The API will be available at `http://localhost:8080/api`

## Project Structure

```
src/main/java/com/ethra/taskmanager/
├── config/           # Security & CORS config
├── controller/       # REST controllers (5)
├── dto/              # Request & Response DTOs (12)
├── entity/           # JPA entities (4)
├── enums/            # Role, TaskStatus, Priority
├── exception/        # Custom exceptions + global handler
├── repository/       # Spring Data repositories (4)
├── security/         # JWT filter, token provider, UserDetails
└── service/          # Business logic (5)
```

## License

This project is for assessment purposes.
