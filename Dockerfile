FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle.kts settings.gradle.kts gradle.properties ./

RUN ./gradlew dependencies --no-daemon -q

COPY app/ app/
RUN ./gradlew :app:bootJar --no-daemon -q

FROM gcr.io/distroless/java25-debian13
WORKDIR /app
COPY --from=build /workspace/app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
