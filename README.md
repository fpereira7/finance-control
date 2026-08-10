# finance-control

API de controle financeiro construída com Spring Boot.

## Stack

- Java 21
- Spring Boot 4
- Spring Web
- Spring Data JPA
- PostgreSQL
- Flyway
- Bean Validation
- Maven
- JUnit 5
- Mockito

## Arquitetura

```
Controller → Service → Repository → Database
```

Pacotes:

| Pacote | Responsabilidade |
|--------|------------------|
| `controller` | Endpoints HTTP (DTO) |
| `service` | Regras de negócio |
| `repository` | Acesso a dados |
| `entity` | Persistência |
| `dto` | Request/response da API |
| `mapper` | Entity ↔ DTO |
| `exception` | Exceções e handlers |
| `configuration` | Configurações Spring |

Entity não é usada como request/response da API.

## Pré-requisitos

- JDK 21+
- Docker Desktop

## Banco de dados (Docker)

```bash
docker compose up -d
```

PostgreSQL em `localhost:5432`, banco `finance_control` (usuário/senha: `postgres`/`postgres`).

## Executar a API

```bash
./mvnw spring-boot:run
```

## Testes

```bash
./mvnw test
```
