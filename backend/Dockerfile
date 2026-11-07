# Multi-stage Dockerfile for Ordera Backend (Eclipse Temurin 21)
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build production jar skipping tests (tests executed in CI)
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

RUN addgroup -S orderagroup && adduser -S orderauser -G orderagroup

COPY --from=builder /workspace/target/ordera-api-*.jar app.jar
RUN chown -R orderauser:orderagroup /app

USER orderauser

ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE="dev"

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --retries=5 --start-period=30s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/v1/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

