# Jeu du Tao — Project Instructions

The _Jeu du Tao_ helps you achieve your goal by harnessing collective intelligence.

This goal—which we'll call a "quest" during the game—can be material, relational, creative, or personal.
During the game, participants use collective intelligence to help each other progress on their quest.

It's a win-win game! Players play together, not against each other. In a _Jeu du Tao_, your only opponent is yourself.

The game encourages you to cooperate and exchange ideas with kindness, active listening, freedom of expression, and
clarity. It's suitable for individuals and businesses alike, for individual or collective quests.

## Domain Vocabulary

| Term     | Definition                                                                                       |
|----------|--------------------------------------------------------------------------------------------------|
| Quest    | The goal a player wants to achieve during the game (material, relational, creative, or personal) |
| Guardian | The person who leads the game: initiates it and invites other players to join                    |

## Game play

Users are distributed, each on their own computer.
A Guardian initiates the game, then invites other players to join.

## Specifications

- Specifications are in `doc/spec/NNNN-name/*.md`. They mix technical and non-technical content (but their structure
  should clearly separate concerns).
- Status : `draft` → `ready for implementation` → `qa` → `done`.
    - Draft: the PRD is being written, but not yet ready for implementation.
    - Ready for implementation: the PRD is complete and can be implemented.
    - QA: the implementation is complete and is being tested against the PRD.
    - Done: the implementation has passed QA and is complete.

## Architecture

* The application is a distributed application.
* Several instances of the same application can run on different machines.
* For horizontal scaling reasons, their number is variable.
* Each of them has to be able to respond to requests for any on-going game.

## Module Structure

```
domain              — pure Java: Game, Player, port interfaces (GameRepository, PlayerRepository), exceptions
application         — pure Java: GameService use case, PasswordEncoder/MagicLinkSender port interfaces
infra-persistence   — Spring JDBC + Liquibase: JdbcGameRepository, JdbcPlayerRepository, LiquibaseConfig
infra-web-backend   — Spring WebMVC + WebSocket: controllers, DTOs, RateLimiter, WebSocket handlers
infra-web-frontend  — React 19 + TypeScript + Vite (pnpm)
app                 — Spring Boot entry point: @Bean wiring (UseCaseConfig), SpaWebConfig, H2TcpServerConfig
```

`domain` and `application` have **no Spring dependency** — enforced by Gradle.
Use cases are wired via `@Bean` in `app/config/UseCaseConfig.java`.

## Tech Stack

| Layer            | Choice                                                |
|------------------|-------------------------------------------------------|
| Java             | 25                                                    |
| Build            | Gradle 9.4.0 (Kotlin DSL)                             |
| Framework        | Spring Boot 4.0.3 (Spring Framework 7, Jakarta EE 11) |
| JSON             | Jackson 3 (group: `tools.jackson`)                    |
| Tests            | JUnit 5 via `spring-boot-starter-webmvc-test`         |
| Frontend         | React 19 + TypeScript (strict) + Vite                 |
| Frontend pkg     | pnpm                                                  |
| Frontend routing | React Router v7                                       |
| Frontend tests   | Vitest + @testing-library/react                       |
| Frontend lint    | ESLint 9 flat config + typescript-eslint strict       |

## Java

- Prefer var over explicit variable type when the right member is explicit enough.
- Prefer immutable data structures (records, immutable collections) to mutable data structures (getter/setter).
- Prefix variables of type Optional with `maybe` (eg: `maybeDescription`).

## Testing Conventions

- In Java tests, avoid `@ExtendWith(MockitoExtension.class)` / `@Mock` / `@InjectMocks`. Use explicit `mock(Class)`
  calls and direct constructor instantiation instead. Reset mocks in a `@BeforeEach` method using `Mockito.reset(...)`.
- Use a Given / When / Then structure, but don't add the `// Given`, `// When` and `// Then` comments: only separate
  blocks.
- Name tests with the following pattern: `<method>_(when<condition>|of<input>)_<expected>`. When the middle part is
  irrelevant, skip it.
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

## i18n

- (important) The frontend (`t()` call sites) is the **single source of truth** for i18n key names.
- Before writing any key in `messages_fr.properties`, grep the frontend for the actual keys used: `grep -r 't("' infra-web-frontend/src`.
- Never invent key names server-side — derive them from the frontend code.
- Key naming convention: `<feature>.<section>.<element>` (e.g., `game.waiting.handle.copy`). See `doc/i18n.md`.
- (important) When multiple agents work in parallel on the same feature, i18n must be owned by a single agent (preferably the frontend agent, which also writes the properties file).

## Extended documentation

On-demand open with Read tool (don't import with @) :

- `doc/quick-reference.md` — Commands, structure, critical standards
- `doc/vision.md` — The long-term vision of the project
- `doc/i18n.md` — i18n architecture, key naming conventions, workflow
- `doc/architecture.md` - application architecture, technical decisions, technical standards, technical design

When a decision impacts one of those documents, update it.
