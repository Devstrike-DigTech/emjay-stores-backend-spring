# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM gradle:8.8-jdk17 AS build

WORKDIR /app

# Copy dependency files first for better layer caching
COPY build.gradle.kts settings.gradle.kts gradle.properties* ./
COPY gradle ./gradle

# Pre-download dependencies (cached unless build files change)
RUN gradle dependencies --no-daemon || true

# Copy source and build
COPY src ./src
RUN gradle bootJar --no-daemon -x test

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Non-root user for security
RUN addgroup -S emjay && adduser -S emjay -G emjay

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Give ownership to non-root user
RUN chown emjay:emjay app.jar
USER emjay

# Railway injects PORT env var; fall back to 8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
