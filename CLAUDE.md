# Jeu du Tao — Project Instructions

The _Jeu du Tao_ helps you achieve your goal by harnessing collective intelligence.

This goal—which we'll call a "quest" during the game—can be material, relational, creative, or personal.
During the game, participants use collective intelligence to help each other progress on their quest.

It's a win-win game! Players play together, not against each other. In a _Jeu du Tao_, your only opponent is yourself.

The game encourages you to cooperate and exchange ideas with kindness, active listening, freedom of expression, and
clarity. It's suitable for individuals and businesses alike, for individual or collective quests.

## Domain Vocabulary

| Term | Definition |
|---|---|
| Quest | The goal a player wants to achieve during the game (material, relational, creative, or personal) |
| Guardian | The person who leads the game: initiates it and invites other players to join |

## Game play

Users are distributed, each on their own computer.
A Guardian initiates the game, then invites other players to join.

## Architecture

* The application is a distributed application.
* Several instances of the same application can run on different machines.
* For horizontal scaling reasons, their number is variable.
* Each of them has to be able to respond to requests for any on-going game.

## Tech Stack

| Layer | Choice |
|---|---|
| Java | 25 |
| Build | Gradle 9.4.0 (Kotlin DSL) |
| Framework | Spring Boot 4.0.3 (Spring Framework 7, Jakarta EE 11) |
| JSON | Jackson 3 (group: `tools.jackson`) |
| Tests | JUnit 5 via `spring-boot-starter-webmvc-test` |
| Frontend | React 19 + TypeScript (strict) + Vite |
| Frontend pkg | pnpm |
| Frontend routing | React Router v7 |
| Frontend tests | Vitest + @testing-library/react |
| Frontend lint | ESLint 9 flat config + typescript-eslint strict |

## Build Commands

```bash
./gradlew build              # Compile + test + frontend build
./gradlew :app:bootRun       # Run the application (port 8080)
./gradlew :app:bootJar       # Build fat JAR

# Frontend (in app-frontend/)
pnpm install                 # Install dependencies
pnpm run dev                 # Vite dev server (proxies /api, /actuator to :8080)
pnpm run build               # Production build → dist/
pnpm test                    # Run Vitest tests
pnpm run lint                # ESLint
```

## Spring Boot 4 Conventions

- `spring-boot-starter-webmvc` (not `spring-boot-starter-web`)
- `spring-boot-starter-webmvc-test` (not `spring-boot-starter-test`)
- BOM imported via `implementation(platform(SpringBootPlugin.BOM_COORDINATES))` — no `io.spring.dependency-management` plugin
- Jackson 3 group: `tools.jackson` (not `com.fasterxml.jackson`)

## Modules

```
app           — Spring Boot entry point; serves frontend SPA at /
app-backend   — REST controllers under /api/** (all REST endpoints go here)
app-frontend  — React SPA (non-Java; uses pnpm + Vite)
```

Additional Java modules apply the root `subprojects` block (Java toolchain, JUnit) automatically.
`app-frontend` is excluded from the root `subprojects` block — it has no Java toolchain config.

### API conventions
- All REST endpoints live in `app-backend` under the `/api/**` prefix.
- `/actuator/**` is reserved for Spring Boot actuator.
- Any unmatched path is served `index.html` by the SPA fallback (`SpaWebConfig`).

## Spring Boot 4 Notes
- `@AutoConfigureMockMvc` is NOT available in `spring-boot-starter-webmvc-test`; use `MockMvcBuilders.webAppContextSetup(context)` instead.

## Java
- Prefer var over explicit variable type when the right member is explicit enough.
- Prefer immutable data structures (records, immutable collections) to mutable data structures (getter/setter).
- Prefix variables of type Optional with `maybe` (eg: `maybeDescription`).

## Testing Conventions

- In Java tests, avoid `@ExtendWith(MockitoExtension.class)` / `@Mock` / `@InjectMocks`. Use explicit `mock(Class)` calls and direct constructor instantiation instead. Reset mocks in a `@BeforeEach` method using `Mockito.reset(...)`.
- Use a Given / When / Then structure, but don't add the `// Given`, `// When` and `// Then` comments: only separate blocks.
- Name tests with the following pattern: `<method>_(when<condition>|of<input>)_<expected>`. When the middle part is irrelevant, skip it.
  - Example 1: `map_ofEmptyList_returnsEmptyList`
  - Example 2: `generateTranslations_whenI18nIsDisabled_doesNothing`
  - Example 3: `toString_buildsTheObjectsStringRepresentation`
- (important) Follow TDD red-green phases:
  1. **Red**: First write (or identify) a failing test that reproduces the bug
  2. **Green**: Then write the minimal fix to make that test pass
- Tests use `@SpringBootTest` for integration tests.

## Actuator

Exposed endpoints: `health`, `info` only.
Health details visible only to authorized users (`when_authorized`).
