# External Audit Coordinator

## Overview
The **External Audit Coordinator** is a comprehensive, production-ready enterprise application designed to streamline and automate the entire lifecycle of audit programs, tasks, and findings. Built on a modern microservices architecture, it seamlessly integrates a Spring Boot backend, a Python/Flask AI microservice for intelligent audit insights, and a responsive React frontend. 

The system features role-based access control (RBAC), JWT authentication, asynchronous email notifications, Swagger OpenAPI documentation, and automated database seeding, providing a robust foundation for managing complex compliance workflows.

---

## Architecture Diagram

```text
                               +-------------------+
                               |                   |
                               |    React (Vite)   |
                               |    (Frontend)     |
                               |                   |
                               +---------+---------+
                                         |
                                         | REST / JSON
                                         v
                               +---------+---------+
                               |                   |
                               |  Spring Boot 3.2  |
                               |    (Backend)      |
                               |                   |
                               +----+----+----+----+
                                    |    |    |
          +-------------------------+    |    +-------------------------+
          |                              |                              |
          v                              v                              v
+---------+---------+          +---------+---------+          +---------+---------+
|                   |          |                   |          |                   |
|   PostgreSQL 15   |          |      Redis 7      |          |  Flask AI Service |
|   (Relational DB) |          |   (Caching/Jobs)  |          |    (Python/Groq)  |
|                   |          |                   |          |                   |
+-------------------+          +-------------------+          +---------+---------+
                                                                        |
                                                                        v
                                                              +---------+---------+
                                                              |                   |
                                                              |    Chroma DB      |
                                                              |  (Vector Store)   |
                                                              |                   |
                                                              +-------------------+
```

---

## Prerequisites
Before you begin, ensure you have the following installed on your host machine:
- **Docker Desktop** (Make sure the Docker daemon is running)
- **Git** (For version control)
- **Node.js 18+** (Optional, for local frontend development outside Docker)
- **Java 17+** & **Maven** (Optional, for local backend development outside Docker)

---

## Setup Steps

1. **Clone the Repository**
   ```bash
   git clone https://github.com/shanmukhaprajwal1122/external-audit-coordinator.git
   cd external-audit-coordinator
   ```

2. **Configure Environment Variables**
   Copy the example environment file and fill in the required values (especially `JWT_SECRET` and `GROQ_API_KEY`).
   ```bash
   cp .env.example .env
   ```

3. **Start the Multi-Container Environment**
   Use Docker Compose to build and start all microservices, databases, and caches.
   ```bash
   docker-compose up --build -d
   ```

4. **Verify Deployment**
   - **Frontend UI**: `http://localhost:80` (or `http://localhost:3000` for local dev)
   - **Backend API**: `http://localhost:8080/api`
   - **Swagger OpenAPI Docs**: `http://localhost:8080/swagger-ui/index.html`
   - **AI Microservice**: `http://localhost:5000`

   *(Note: On initial startup, the backend `DataLoader` automatically seeds 30 realistic demo records and 3 default users (Admin, Manager, Auditor).)*

---

## Environment Variables Reference (`.env`)

| Variable | Description | Default Example | Required |
| :--- | :--- | :--- | :--- |
| **`SERVER_PORT`** | Port for the Spring Boot backend | `8080` | No |
| **`DB_URL`** | PostgreSQL JDBC connection URL | `jdbc:postgresql://postgres:5432/audit_db` | Yes |
| **`DB_USERNAME`** | Database user | `audit_user` | Yes |
| **`DB_PASSWORD`** | Database password | `audit_pass` | Yes |
| **`REDIS_HOST`** | Redis server host | `redis` | Yes |
| **`REDIS_PORT`** | Redis server port | `6379` | Yes |
| **`MAIL_HOST`** | SMTP server host | `smtp.gmail.com` | Yes |
| **`MAIL_PORT`** | SMTP server port | `587` | Yes |
| **`MAIL_USERNAME`** | Email account for sending alerts | `your-email@gmail.com` | Yes |
| **`MAIL_PASSWORD`** | App-specific password for email | `your-app-password` | Yes |
| **`JWT_SECRET`** | 256-bit secure key for JWT signatures | `change-me-to-long-secret` | **Yes** |
| **`JWT_EXPIRATION`** | JWT token lifespan in milliseconds | `86400000` (24h) | Yes |
| **`FLASK_PORT`** | Port for Python AI Microservice | `5000` | No |
| **`GROQ_API_KEY`** | Groq API Key for AI inference | `your_groq_api_key_here` | **Yes** |
| **`RATE_LIMIT`** | AI requests allowed per minute per IP | `30` | No |
| **`VITE_API_BASE_URL`** | React Frontend target for backend API | `http://localhost:8080/api` | Yes |
| **`VITE_AI_BASE_URL`** | React Frontend target for AI API | `http://localhost:5000` | Yes |
| **`AI_SERVICE_URL`** | Backend's target URL for AI Service | `http://ai-service:5000` | Yes |
| **`APP_BASE_URL`** | Host URL used inside email links | `http://localhost:3000` | Yes |
