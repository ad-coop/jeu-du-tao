FROM eclipse-temurin:25.0.2_10-jdk AS build
WORKDIR /workspace

RUN apt-get update && apt-get install -y curl ca-certificates && \
    curl -fsSL https://deb.nodesource.com/setup_22.x | bash - && \
    apt-get install -y nodejs && \
    npm install -g pnpm && \
    rm -rf /var/lib/apt/lists/*

COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle.kts settings.gradle.kts gradle.properties ./

RUN ./gradlew dependencies --no-daemon -q

COPY app-frontend/package.json app-frontend/pnpm-lock.yaml app-frontend/
RUN cd app-frontend && pnpm install --frozen-lockfile

COPY app/ app/
COPY app-backend/ app-backend/
COPY app-frontend/ app-frontend/
RUN ./gradlew :app:bootJar --no-daemon -q

FROM gcr.io/distroless/java25-debian13
WORKDIR /app
COPY --from=build /workspace/app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
