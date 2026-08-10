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

## Pré-requisitos

- JDK 21+
- PostgreSQL rodando em `localhost:5432`
- Banco `finance_control` criado

## Configuração

Ajuste as credenciais em `src/main/resources/application.properties` se necessário.

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/finance_control
spring.datasource.username=postgres
spring.datasource.password=postgres
```

## Executar

```bash
./mvnw spring-boot:run
```

## Testes

```bash
./mvnw test
```
