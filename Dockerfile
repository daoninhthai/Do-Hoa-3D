# ============================================================
# Stage 1: Build the backend with Maven
# ============================================================
FROM eclipse-temurin:17-jdk-jammy AS backend-build

WORKDIR /app

# Copy Maven wrapper and pom.xml first for dependency caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B --no-transfer-progress

# Copy source and build
COPY src/ src/
RUN ./mvnw package -DskipTests -B --no-transfer-progress

# ============================================================
# Stage 2: Build the Angular frontend
# ============================================================
FROM node:18-alpine AS frontend-build

WORKDIR /app/frontend

COPY frontend/package.json frontend/package-lock.json* ./
RUN npm ci --prefer-offline

COPY frontend/ ./
RUN npm run build -- --configuration=production

# ============================================================
# Stage 3: Production runtime image
# ============================================================
FROM eclipse-temurin:17-jre-jammy AS runtime

RUN groupadd --system appuser && useradd --system --gid appuser appuser

WORKDIR /app

# Copy the built JAR
COPY --from=backend-build /app/target/*.jar app.jar

# Copy the frontend build output into the JAR's static resources location
COPY --from=frontend-build /app/frontend/dist/oauth2-auth-frontend/ /app/static/

# Healthcheck against Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:9000/actuator/health || exit 1

EXPOSE 9000

USER appuser

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
