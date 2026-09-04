# syntax=docker/dockerfile:1

FROM node:22-alpine AS frontend
WORKDIR /ui
COPY frontend/package.json frontend/package-lock.json frontend/.npmrc ./
RUN npm ci --legacy-peer-deps
COPY frontend/ ./
RUN npm run build

FROM maven:3.9.9-eclipse-temurin-17 AS backend
WORKDIR /app
COPY backend/pom.xml .
COPY backend/.mvn .mvn
RUN mvn -q -B -DskipTests dependency:go-offline || true
COPY backend/src src
COPY --from=frontend /ui/dist/frontend/browser src/main/resources/static
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=backend /app/target/*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
