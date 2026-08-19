# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY src src
RUN chmod +x mvnw && ./mvnw -DskipTests package

FROM eclipse-temurin:21-jre-jammy AS runner
WORKDIR /app
RUN useradd -r -u 1001 appuser
COPY --from=builder /app/target/finance-control-0.0.1-SNAPSHOT.jar app.jar
RUN chown appuser:appuser app.jar
USER appuser
ENV PORT=10000
EXPOSE 10000
ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT} -jar app.jar"]
