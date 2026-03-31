# 👥 Participant-Service (NextEvent Project)

A microservice responsible for managing participant records, including profile information.  
It exposes a RESTful API consumed by the API Gateway.

---

## 👤 Student Information

- **Student Name:** Sherul Dhanushka Fernando
- **Student Number:** 2301691014
- **Slack Handle:** https://ijse-eca-hdse-69-70.slack.com/team/U0AEH8NS9DW
- **GCP Project ID:** project-0ae0d75b-3979-4ebf-be9

---

## 📝 About

The **Participant-Service** is responsible for managing all participant-related data in the NextEvent system.

It allows:

- Creating participant profiles
- Managing participant details
- Retrieving participant information
- Deleting participant records

All requests are routed through the **API Gateway**, and the service registers with the **Service-Registry (Eureka)**.

---

## 🛠 Tech Stack

| Technology | Details |
|---|---|
| Java | 25 |
| Spring Boot | 4.0.3 |
| Spring Cloud | 2025.1.0 |
| Spring Data JPA | ORM / persistence layer |
| PostgreSQL | Relational database (port `12500`) |
| MapStruct | DTO ↔ Entity mapping |
| Lombok | Boilerplate reduction |
| Spring Validation | Bean validation |
| Eureka Client | Service registration & discovery |
| Config Client | Fetches config from Config-Server |
| Spring Boot Actuator | Health & management endpoints |

---

## 🌐 Service Details

| Property | Value |
|---|---|
| Port | `8003` |
| Artifact ID | `Participant-Service` |
| Group ID | `lk.ijse.eca` |
| Database | PostgreSQL — `jdbc:postgresql://localhost:12500/eca` |

---

## 📡 API Endpoints

Base path: `/api/v1/participants`

| Method | Path | Description | Content-Type |
|---|---|---|---|
| `POST` | `/api/v1/participants` | Create a new participant | `application/json` |
| `GET` | `/api/v1/participants` | Get all participants | — |
| `GET` | `/api/v1/participants/{participantId}` | Get participant by ID | — |
| `PUT` | `/api/v1/participants/{participantId}` | Update participant | `application/json` |
| `DELETE` | `/api/v1/participants/{participantId}` | Delete participant | — |

---

> **NIC format:** `^\d{9}[vV]$` — 9 digits followed by `V` or `v` (e.g., `123456789V`). NIC is the primary key and cannot be changed after creation.

## Sample Request Body

> Requests must use `Content-Type: multipart/form-data`.

**POST / PUT** `/api/v1/participants`

| Field | Type | Required | Validation |
|---|---|---|---|
| `nic` | `string` | Yes (on create) | `^\d{9}[vV]$` |
| `name` | `string` | Yes | Letters and spaces only |
| `address` | `string` | Yes | — |
| `mobile` | `string` | Yes | — |
| `email` | `string` | No | Valid email format |
| `picture` | `file` | Yes (on create) | Image file, max 5 MB |

**Sample response:**

```json
{
  "nic": "123456789V",
  "name": "Kamal Perera",
  "address": "Peter's road, Maggona.",
  "mobile": "0771234567",
  "email": "kamal@example.com",
  "picture": "/api/v1/participants/123456789V/picture"
}
```

## Getting Started

Follow the lecture guidelines, refer to the lecture video for more information and how to get started correctly.

> **Prerequisites:** Config-Server, Service-Registry, and Api-Gateway must be running. A PostgreSQL instance must be accessible on port `12500` with a database named `eca`.

**Startup order:**
1. Config-Server (`9000`)
2. Service-Registry (`9001`)
3. Api-Gateway (`7000`)
4. **Student-Service** (`8000`)

```bash
./mvnw spring-boot:run
```

## Testing

A Postman collection is available for testing the API endpoints:

**Student Service:** [Open Collection](https://sherul.postman.co/workspace/classroom~67e69d15-9d52-4dc5-b136-621917174743/collection/40383343-f31980a0-d56c-4142-af12-46625f10feab?action=share&creator=40383343)

## Need Help?

If you encounter any issues, feel free to reach out and start a discussion via the Slack workspace.
