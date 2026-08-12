# finance-control

API de controle financeiro construída com Spring Boot.

## Stack

- Java 21
- Spring Boot 4
- Spring Web
- Spring Data JPA
- Spring Security (JWT)
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
| `security` | JWT e usuário autenticado |

Entity não é usada como request/response da API.

## Pré-requisitos

- JDK 21+
- Docker Desktop

## Banco de dados (Docker)

```bash
docker compose up -d
```

PostgreSQL em `localhost:5432`, banco `finance_control` (usuário/senha: `postgres`/`postgres`).

## Autenticação

Endpoints públicos:

- `POST /api/auth/register` — body: `{ "email", "password", "name?" }`
- `POST /api/auth/login` — body: `{ "email", "password" }`

Resposta: `{ accessToken, tokenType, expiresIn, user }`.

Demais rotas `/api/**` exigem header:

```http
Authorization: Bearer <accessToken>
```

### Variáveis JWT

| Propriedade | Env | Padrão local |
|-------------|-----|--------------|
| `app.jwt.secret` | `APP_JWT_SECRET` | valor de desenvolvimento (trocar fora do local) |
| `app.jwt.expiration-ms` | `APP_JWT_EXPIRATION_MS` | `3600000` (1h) |

### Migration de dados existentes (V6)

A migration `V6__add_users_and_ownership.sql`:

1. Cria a tabela `users`
2. Insere o usuário `migration@local` com senha `ChangeMe123!`
3. Atribui esse usuário a salários, despesas e imports já existentes
4. Torna `user_id` obrigatório e ajusta unicidade de import por usuário

Em ambientes com dados reais, altere a senha ou migre esses registros para uma conta definitiva.

## Executar a API

```bash
./mvnw spring-boot:run
```

## Frontend (Angular)

```bash
cd frontend
npm start
```

Abre em `http://localhost:4200` (repositório separado). O front ainda precisa integrar o login JWT.

## Testes

```bash
./mvnw test
```
