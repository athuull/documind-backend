# DocuMind — AI-Powered Insurance Agency Management System (AMS)

DocuMind is an enterprise-grade Insurance Agency Management System and intelligent document extraction layer. Built on **Spring Boot 4**, **Spring AI**, **PostgreSQL**, and **Vector Store RAG**, DocuMind automates the insurance document lifecycle: from PDF policy upload and automated data extraction to conversational policy question-answering, client 360° portfolio management, and proactive renewal pipelines.

---

## 🌟 Key Features

- **Automated AI Policy Extraction**: Upload scanned or digital PDF insurance policies; DocuMind parses the document using Apache PDFBox, feeds cleaned content to Spring AI LLM prompts, and extracts structured data classified strictly into `HEALTH` or `VEHICLE` policy types.
- **RAG Policy Assistant**: Ingests uploaded policy documents into a vector store with chunking and token embeddings. Insurance agents and clients can query policies in plain English (e.g., *"What is my deductible for accident damage?"*) using grounded context and policy citations.
- **Client 360° Dashboard**: Consolidated overview per client detailing active policies, total annual premium volume under management, insured vehicles, upcoming renewal deadlines, and document archives.
- **Policy Lifecycle & Status Management**: Manage policy progression across `ACTIVE`, `MANUAL_REVIEW`, `EXPIRED`, and `CANCELLED`. If AI extraction encounters missing data (e.g. absent vehicle registration), the policy is automatically flagged for agent review before activation.
- **Proactive Renewal Pipeline**: Automated daily cron scheduler (`0 1 0 * * *`) that transitions expired policies and flags upcoming expirations within customizable timeframes (e.g., 30-day renewal alerts).
- **Client & Vehicle Portfolios**: Full CRUD and search capabilities across clients, vehicles (with year, make, model, registration), and policy associations with optimistic locking concurrency checks.
- **Enterprise Security & RBAC**: Stateless authentication powered by Spring Security, JWT tokens (1-hour expiry), and BCrypt password encryption with role-based access control (`ADMIN`, `USER`).

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| **Framework** | Spring Boot 4.0.2 (Java 21) |
| **Security** | Spring Security, JJWT 0.11.5, BCrypt |
| **AI & LLM** | Spring AI 2.0.0-M2 (OpenAI / OpenRouter API client) |
| **Vector Store** | Spring AI SimpleVectorStore / Chroma integration |
| **Document Processing** | Apache PDFBox 3.0.5 |
| **Database & ORM** | PostgreSQL 17, Spring Data JPA, Hibernate ORM 7 |
| **DTO Mapping** | MapStruct 1.6.3, Project Lombok |
| **Resilience** | Spring Retry 2.0.12 |
| **API Documentation** | SpringDoc OpenAPI 2.8.4 (Swagger UI) |

---

## 📋 Prerequisites

- **Java Development Kit (JDK)**: Version 21 or higher
- **PostgreSQL**: Version 14 or higher running locally or in Docker
- **OpenRouter / OpenAI API Key**: For Spring AI document extraction and RAG chat

---

## ⚙️ Configuration & Environment Variables

DocuMind supports configuration via environment variables with fallback defaults in `src/main/resources/application.properties`:

| Environment Variable | Description | Default Value |
|---|---|---|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC connection URL | `jdbc:postgresql://localhost:5432/insurance_db` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `athul` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | *(empty)* |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Hibernate schema management | `update` |
| `OPENROUTER_API_KEY` | OpenRouter or OpenAI API key | *(configured key)* |
| `SPRING_AI_OPENAI_BASE_URL` | API Base URL | `https://openrouter.ai/api` |
| `SPRING_AI_OPENAI_MODEL` | LLM model name | `stepfun/step-3.5-flash:free` |
| `JWT_SECRET_KEY` | HMAC-SHA256 secret key for signing JWTs | *(256-bit hex secret)* |
| `JWT_EXPIRATION_TIME` | JWT token validity in milliseconds | `3600000` (1 hour) |

---

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/athul/documind-backend.git
cd documind-backend
```

### 2. Set Up Database
Create a PostgreSQL database named `insurance_db`:
```bash
psql -U postgres -c "CREATE DATABASE insurance_db;"
```

### 3. Run the Application
```bash
./mvnw spring-boot:run
```

The server starts on port `8080`. Default roles (`ADMIN` and `USER`) are automatically seeded by `DataInitializer` upon startup.

### 4. Explore Interactive API Docs
Open your browser and navigate to:
```
http://localhost:8080/swagger-ui/index.html
```

---

## 📡 REST API Reference

### 1. Authentication (`/api/auth`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/auth/signup` | Register a new user account | No |
| `POST` | `/api/auth/login` | Authenticate and obtain JWT Bearer token | No |

#### Login Request Example:
```json
POST /api/auth/login
Content-Type: application/json

{
  "email": "agent@documind.ai",
  "password": "SecurePassword123!"
}
```
#### Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 3600000
}
```

> **Note**: For all protected endpoints below, include the HTTP header:
> `Authorization: Bearer <token>`

---

### 2. User Management (`/users`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/users/me` | Get profile of currently authenticated user |
| `POST` | `/users/register` | Create user with specific role (Admin only) |
| `GET` | `/users/all` | List all system users |
| `GET` | `/users/{id}` | Get user by ID |
| `PUT` | `/users/{id}` | Update user with optimistic locking |
| `DELETE` | `/users/{id}` | Remove user |

---

### 3. Client Management (`/clients`)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/clients/user/{userId}` | Create a client assigned to an agent |
| `PUT` | `/clients/{id}` | Update client profile & reassignment |
| `GET` | `/clients/{id}` | Get client details by ID |
| `GET` | `/clients` | List all clients |
| `GET` | `/clients/my-clients` | List clients created by or assigned to authenticated user |
| `GET` | `/clients/{id}/dashboard` | **Client 360° Dashboard** (vehicles, policies, premium, alerts) |
| `GET` | `/clients/search?query=...` | Search clients by name, email, or phone |
| `DELETE` | `/clients/{id}` | Delete a client |

#### Client 360° Dashboard Response Example (`GET /clients/1/dashboard`):
```json
{
  "clientId": 1,
  "clientName": "Acme Logistics Corp",
  "email": "fleet@acmelogistics.com",
  "phone": "+1-555-0199",
  "assignedToAgent": "Agent Smith",
  "totalVehicles": 2,
  "totalPolicies": 3,
  "activePoliciesCount": 2,
  "totalAnnualPremium": 85000.00,
  "totalDocuments": 3,
  "vehicles": [
    {
      "vehicleId": 10,
      "make": "Volvo",
      "model": "FH16",
      "year": 2023,
      "regNumber": "KA01AB1234",
      "activePolicyNumber": "POL-V-9988",
      "currentStatus": "ACTIVE"
    }
  ],
  "policies": [
    {
      "policyId": 45,
      "policyNumber": "POL-V-9988",
      "policyType": "VEHICLE",
      "status": "ACTIVE",
      "provider": "Tata AIG",
      "premium": 45000.00,
      "startDate": "2025-10-01",
      "endDate": "2026-09-30",
      "isExpiringSoon": true
    }
  ]
}
```

---

### 4. Policy Operations (`/api/policies`)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/policies` | Create a unified policy (`VEHICLE` or `HEALTH`) |
| `PUT` | `/api/policies/{id}` | Update policy details |
| `PATCH` | `/api/policies/{id}/status?status=...` | Transition policy status (`ACTIVE`, `MANUAL_REVIEW`, `CANCELLED`) |
| `GET` | `/api/policies/{id}` | Get policy by ID |
| `GET` | `/api/policies` | List all policies across all types |
| `GET` | `/api/policies/by-client/{clientId}` | Get all policies for a given client |
| `GET` | `/api/policies/by-number/{policyNumber}` | Lookup policy by unique policy number |
| `GET` | `/api/policies/by-type/{type}` | Filter policies by type (`HEALTH` or `VEHICLE`) |
| `GET` | `/api/policies/by-status/{status}` | Filter policies by status (`ACTIVE`, `MANUAL_REVIEW`, `EXPIRED`, `CANCELLED`) |
| `GET` | `/api/policies/expiring?days=30` | **Renewals Pipeline**: Get policies expiring within N days |
| `GET` | `/api/policies/search?query=...` | Search policies by number, provider, or insured name |
| `GET` | `/api/policies/stats` | **Agency Portfolio KPI Metrics** |
| `DELETE` | `/api/policies/{id}` | Delete policy record |

#### Create Policy Request Example (`POST /api/policies`):
```json
{
  "policyNumber": "STAR-HLT-2026-9901",
  "clientId": 1,
  "insuredName": "John Doe",
  "provider": "Star Health Insurance",
  "premium": 18500.00,
  "startDate": "2026-01-01",
  "endDate": "2026-12-31",
  "policyType": "HEALTH",
  "coverageType": "Family Floater",
  "coverageAmount": 1000000.00,
  "beneficiaryName": "Jane Doe",
  "insuredPersonAge": 38
}
```

---

### 5. Type-Specific Policy Endpoints
- **Vehicle Policies**: `/vehicle-policies` (`GET`, `POST`, `PUT`, `DELETE`)
- **Health Policies**: `/health-policies` (`GET`, `POST`, `PUT`, `DELETE`)

---

### 6. Vehicle Management (`/api/vehicles`)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/vehicles` | Register vehicle for client (`make`, `model`, `year`, `regNumber`) |
| `PUT` | `/api/vehicles/{id}` | Update vehicle with optimistic locking |
| `GET` | `/api/vehicles` | List all registered vehicles |
| `GET` | `/api/vehicles/{id}` | Get vehicle by ID |
| `GET` | `/api/vehicles/registration/{regNo}` | Find vehicle by registration plate |
| `DELETE` | `/api/vehicles/{id}` | Delete vehicle record |

---

### 7. Document Processing & AI Pipeline (`/documents` & `/ai`)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/documents/client/{clientId}/upload` | Upload PDF policy document; triggers AI extraction & vector indexing |
| `GET` | `/documents/{id}/view` | View/download document binary stream (inline PDF) |
| `GET` | `/documents/client/{clientId}` | List all uploaded documents for a client |
| `GET` | `/documents/policy/{policyId}` | List all documents attached to a policy |
| `DELETE` | `/documents/{id}` | Delete document entity and file from storage |
| `POST` | `/ai/client/{clientId}/upload` | Alternate AI processing endpoint |

---

### 8. RAG Policy Assistant (`/api/chat`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/chat/ask` | Ask conversational question about a client's policies |
| `GET` | `/api/chat/debug/vectors` | Inspect vector store documents |
| `GET` | `/api/chat/debug/filter` | Test vector filter expressions |
| `GET` | `/api/chat/debug/retrieval` | Test similarity search top-K retrieval |

#### Ask Chatbot Example:
```
GET /api/chat/ask?clientId=1&question=What%20is%20the%20coverage%20amount%20for%20my%20health%20policy?
```
#### Response:
```json
{
  "answer": "According to policy STAR-HLT-2026-9901, your Family Floater health policy provides coverage up to ₹10,00,000 with Star Health Insurance.",
  "totalTokens": 342,
  "promptTokens": 305,
  "completionTokens": 37
}
```

---

### 9. Role Management (`/roles`)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/roles` | Create new role |
| `GET` | `/roles` | List all system roles |
| `GET` | `/roles/{id}` | Get role by ID |
| `PUT` | `/roles/{id}` | Update role (retryable on optimistic lock) |
| `DELETE` | `/roles/{id}` | Delete role |

---

## 🧪 Testing

Execute the comprehensive automated test suite:
```bash
./mvnw clean test
```

To run a single test class:
```bash
./mvnw test -Dtest=DocuMindServiceTests
```

---

## 📂 Project Structure

```
documind-backend/
├── pom.xml                                   # Maven build descriptor
├── uploads/                                  # Persistent uploaded PDF storage
├── src/
│   ├── main/
│   │   ├── resources/
│   │   │   └── application.properties        # App properties & env var bindings
│   │   └── java/com/athul/documind/
│   │       ├── DocuMindApplication.java      # Application main entry point
│   │       ├── Config/                       # Security, AI, vector store, DB configs
│   │       ├── Controller/                   # REST API Controllers
│   │       ├── DTO/                          # Request & Response Data Transfer Objects
│   │       ├── Entity/                       # JPA Database Entities
│   │       ├── Enum/                         # Enums (PolicyType, PolicyStatus, RoleType)
│   │       ├── Exception/                    # Custom exceptions & GlobalExceptionHandler
│   │       ├── Mapper/                       # MapStruct interface mappers
│   │       ├── Repository/                   # Spring Data JPA repositories
│   │       ├── Security/                     # Custom UserDetails implementation
│   │       ├── Service/                      # Business logic, AI, and scheduler services
│   │       └── vectorstore/                  # Vector document definitions & memory store
│   └── test/
│       └── java/com/athul/documind/
│           ├── DocuMindApplicationTests.java # Context loading test
│           └── DocuMindServiceTests.java     # Service integration tests
```

---

## 🛡️ License
Proprietary — Developed for Insurance Operations Intelligence.
