# Jeu du Tao — Project Instructions

## Tech Stack

| Layer | Choice |
|---|---|
| Java | 25 |
| Build | Gradle 9.4.0 (Kotlin DSL) |
| Framework | Spring Boot 4.0.3 (Spring Framework 7, Jakarta EE 11) |
| JSON | Jackson 3 (group: `tools.jackson`) |
| Tests | JUnit 5 via `spring-boot-starter-webmvc-test` |
| Formatting | Spotless + Google Java Format |

## Build Commands

```bash
./gradlew build              # Compile + test
./gradlew spotlessCheck      # Check formatting
./gradlew spotlessApply      # Auto-fix formatting
./gradlew :app:bootRun       # Run the application (port 8080)
./gradlew :app:bootJar       # Build fat JAR
```

## Spring Boot 4 Conventions

- `spring-boot-starter-webmvc` (not `spring-boot-starter-web`)
- `spring-boot-starter-webmvc-test` (not `spring-boot-starter-test`)
- BOM imported via `implementation(platform(SpringBootPlugin.BOM_COORDINATES))` — no `io.spring.dependency-management` plugin
- Jackson 3 group: `tools.jackson` (not `com.fasterxml.jackson`)

## Modules

Additional modules are added to `settings.gradle.kts` as:
```kotlin
include("module-name")
```

Each module applies the root `subprojects` block configuration (Java toolchain, JUnit, Spotless).

## Testing Conventions

See global CLAUDE.md. Tests use `@SpringBootTest` for integration tests.

## Actuator

Exposed endpoints: `health`, `info` only.
Health details visible only to authorized users (`when_authorized`).
