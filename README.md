# 💳 Core Banking System

A robust, modular **Core Banking System** built with **Spring Boot**, **Kafka**, and **PostgreSQL**.  
Designed to support essential banking operations such as account management, deposits, withdrawals, and inter-account transfers with high scalability and event-driven architecture.

---

## 🏗️ Project Structure

core-banking-system/
├── account-service/ # Handles account creation and management
├── transaction-service/ # Manages deposits, withdrawals, transfers
├── notification-service/ # Sends notifications via email/SMS
├── common/ # Shared models, DTOs, configs
├── kafka/ # Kafka config and utilities
└── gateway/ # API Gateway for routing and authentication


---

## 🚀 Features

✅ Create and manage bank accounts  
✅ Deposit and withdraw money  
✅ Transfer funds between accounts  
✅ Kafka-based asynchronous communication  
✅ PostgreSQL persistence  
✅ RESTful APIs with Swagger UI  
✅ Modular & scalable architecture

---

## ⚙️ Tech Stack

| Layer            | Technology           |
|------------------|----------------------|
| Backend          | Spring Boot, Spring Web, Spring Kafka |
| Messaging        | Apache Kafka         |
| Database         | PostgreSQL           |
| API Gateway      | Spring Cloud Gateway |
| Configuration    | Spring Config, YAML  |
| Others           | Lombok, MapStruct, Swagger |

---

## 🔁 Event-Driven Architecture

Each operation emits an event to Kafka for reliable processing and decoupling:

- `AccountCreatedEvent`
- `MoneyDepositedEvent`
- `MoneyWithdrawnEvent`
- `TransferInitiatedEvent`
- etc.

Services listen and react to these events asynchronously.

---

## 🔧 How to Run (Dev)

1. **Clone the project**
   ```bash
   git clone https://github.com/your-org/core-banking-system.git
   cd core-banking-system
2. Start Kafka & PostgreSQL

    Use Docker Compose:
3. Build the project

    ./gradlew build
4. Run services
    ./gradlew :account-service:bootRun
    ./gradlew :transaction-service:bootRun
    ./gradlew :gateway:bootRun
## 📬 API Endpoints
Swagger available at:
  http://localhost:8080/swagger-ui.html
  
  Example APIs:
  
  POST /api/accounts → Create new account
  
  POST /api/deposit → Deposit money
  
  POST /api/withdraw → Withdraw money
  
  POST /api/transfer → Transfer money
