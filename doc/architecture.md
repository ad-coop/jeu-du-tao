# Architecture

## 1. Guiding Principles

1. **Domain purity** — `domain` and `application` modules have zero framework dependencies (Java stdlib only). This
   makes the core logic portable, testable without a Spring context, and insulated from framework upgrades.
2. **Dependency rule** — dependencies always point inward: `infra-*` → `application` → `domain`. No inner layer ever
   imports an outer one.
3. **Stateless instances** — application instances hold no in-memory state that cannot be reconstructed from shared
   storage. This is the prerequisite for horizontal scaling.
4. **Module boundaries as enforcement** — Gradle module declarations make illegal dependencies uncompilable, not just
   undesirable. The build is the first line of architectural defence.
5. **DDD tactical patterns** — aggregates encapsulate invariants and are the only entry point for state changes;
   repositories are domain ports; domain events decouple side effects from business logic.

---

## 2. Module Structure

```
domain/              Pure Java. Aggregates, value objects, domain events, port interfaces.
application/         Pure Java. Depends on domain only. Use cases as plain classes.
infra-persistence/   Depends on domain. JDBC repositories, Liquibase migrations, DB config.
infra-web-backend/   Depends on application (+ domain types). REST controllers, DTOs,
                     exception handlers, WebSocket adapter.
app/                 Spring Boot entry point. Depends on all infra modules.
                     Wires beans via @Bean methods. Serves the SPA.
infra-web-frontend/        React SPA.
```

### Per-module detail

| Module               | Contains                                                                                                | Allowed dependencies                                 | Forbidden dependencies                              |
|----------------------|---------------------------------------------------------------------------------------------------------|------------------------------------------------------|-----------------------------------------------------|
| `domain`             | Aggregates, entities, value objects, domain events, repository/service interfaces (ports)               | Java stdlib                                          | Spring, Jakarta EE, any infra module, `application` |
| `application`        | Use case classes (inbound port implementations), outbound port interfaces (e.g. `EmailSender`, `Clock`) | `domain`, Java stdlib                                | Spring, Jakarta EE, any infra module                |
| `infra-persistence`  | JDBC repository implementations, Liquibase scripts, datasource config                                   | `domain`, Spring JDBC, Liquibase                     | `application`, other `infra-*` modules              |
| `app`                | `@SpringBootApplication`, `@Bean` wiring, `SpaWebConfig`, application properties                        | All modules                                          | Business logic of any kind                          |
| `infra-web-backend`  | REST controllers, WebSocket handlers, DTOs, JSON mappers, exception handlers                            | `application`, `domain` (value types), Spring WebMVC | `infra-persistence`                                 |
| `infra-web-frontend` | React SPA source                                                                                        | (pnpm ecosystem)                                     | —                                                   |

**Important:** `domain` and `application` do **not** include the Spring BOM. Use cases are
plain Java classes instantiated via `@Bean` methods in `app`, never annotated with `@Service`
or `@Component`.

---

## 3. Hexagonal Architecture: Ports and Adapters

The application follows the Ports & Adapters pattern (hexagonal architecture).

```
[infra-web-frontend] ──► [infra-web-backend] ──► [application] ──► [domain] ◄── [infra-persistence]
```

### Inbound ports (driven side)

Use case interfaces defined in `application` that express what the system *can do*:

```
application.game.CreateGame
application.game.JoinGame
application.game.KickPlayer
```

REST controllers and WebSocket handlers in `infra-web` call these interfaces. They never call domain objects directly.

### Outbound ports (driving side)

**Repository ports** — defined in `domain`, implemented in `infra-persistence`:

```
domain.game.port.GameRepository
```

**Infrastructure service ports** — defined in `application`, implemented in `infra-*`:

```
application.port.EmailSender
application.port.PasswordEncoder
application.port.Clock
```

### Adapter summary

| Adapter type        | Location                                  | Calls                                        |
|---------------------|-------------------------------------------|----------------------------------------------|
| Inbound — REST      | `infra-web-backend`                       | Application use case interfaces              |
| Inbound — WebSocket | `infra-web-backend` (or dedicated module) | Application use case interfaces              |
| Outbound — JDBC     | `infra-persistence`                       | Implements `domain.game.port.GameRepository` |
| Outbound — SMTP     | `infra-mail`                              | Implements `application.port.EmailSender`    |

---

## 4. DDD Tactical Patterns

### Aggregate root: `Game`

`Game` is the sole aggregate root. It owns all game-level invariants:

- A player can only join if the game is in the correct state.
- Only the guardian can kick a player.
- The game transitions between states (`WAITING`, `IN_PROGRESS`, `ENDED`) through domain methods on `Game`.

All operations on players go through `Game`. Nothing in `infra-*` or `application` modifies a `Player` directly.

### Entity: `Player`

`Player` lives inside the `Game` aggregate. It has identity within its aggregate but is never fetched or updated
independently.

### Value objects

| Value object   | Meaning                           |
|----------------|-----------------------------------|
| `Handle`       | A player's chosen display name    |
| `EmailAddress` | A validated email address         |
| `PasswordHash` | A bcrypt-hashed credential        |
| `PlayerRole`   | `GUARDIAN` or `PLAYER`            |
| `GameState`    | `WAITING`, `IN_PROGRESS`, `ENDED` |

Value objects are immutable, validated at construction, and carry no identity.

### Domain events

Domain events signal that something meaningful happened. They decouple the domain from side effects (WebSocket
broadcasts, email notifications):

- `GameCreated`
- `PlayerJoined`
- `PlayerLeft`
- `PlayerKicked`

The dispatch mechanism (return values vs. event publisher) is a decision left open (see section 9).

### Repositories as ports

`GameRepository` is an interface defined in `domain`. It expresses persistence *intent* in domain terms:

```java
// domain.game.port
public interface GameRepository {
    void save(Game game);

    Optional<Game> findById(GameId id);

    Optional<Game> findByJoinCode(JoinCode code);
}
```

The JDBC implementation lives in `infra-persistence` and is wired via `@Bean` in `app`.

---

## 5. Package Conventions

Root package: `fr.adcoop.jeudutao`

```
domain/
  fr.adcoop.jeudutao.domain.game          ← Game, Player, GameId, JoinCode, …
  fr.adcoop.jeudutao.domain.game.event    ← GameCreated, PlayerJoined, …
  fr.adcoop.jeudutao.domain.game.port     ← GameRepository interface
  fr.adcoop.jeudutao.domain.shared        ← shared value objects (EmailAddress, …)

application/
  fr.adcoop.jeudutao.application.game     ← CreateGame, JoinGame, KickPlayer use cases
  fr.adcoop.jeudutao.application.port     ← EmailSender, PasswordEncoder, Clock

infra-persistence/
  fr.adcoop.jeudutao.infra.persistence.game    ← JdbcGameRepository
  fr.adcoop.jeudutao.infra.persistence.config  ← DataSource, Liquibase config

infra-web-backend/
  fr.adcoop.jeudutao.infra.web.game       ← GameController, DTOs, WebSocket handlers
  fr.adcoop.jeudutao.infra.web.i18n       ← I18nController
  fr.adcoop.jeudutao.infra.web.config     ← SpaWebConfig, WebSocket config

app/
  fr.adcoop.jeudutao                      ← @SpringBootApplication
  fr.adcoop.jeudutao.config               ← @Bean wiring for use cases and adapters
```

---

## 6. Dependency Rules

| Module              | Depends on                                           | Must NOT depend on                                  |
|---------------------|------------------------------------------------------|-----------------------------------------------------|
| `domain`            | Java stdlib                                          | Spring, Jakarta EE, any infra module, `application` |
| `application`       | `domain`, Java stdlib                                | Spring, Jakarta EE, any infra module                |
| `infra-persistence` | `domain`, Spring JDBC, Liquibase                     | `application`, other `infra-*`                      |
| `infra-web-backend` | `application`, `domain` (value types), Spring WebMVC | `infra-persistence`                                 |
| `app`               | All modules                                          | Business logic of any kind                          |

**Enforcement:** Gradle module declarations are the primary enforcement mechanism. Because
`domain` and `application` do not declare Spring as a dependency, any accidental `@Service`
or `@Autowired` annotation will fail to compile. ArchUnit may be adopted later for finer-grained intra-module rules (see
section 9).

---

## 7. Infrastructure: Statelessness & Horizontal Scaling

**Constraint:** No in-memory state may be held by an application instance across requests.
This forbids `ConcurrentHashMap` session stores and an in-process STOMP broker.

**WebSocket:** The current in-process STOMP broker is a stepping stone. The target is an
external STOMP broker relay (e.g. RabbitMQ with STOMP plugin), so that any instance can
broadcast to any connected client regardless of which instance the client connected to.

**Rate limiting / session affinity:** Handled at the API-gateway or load-balancer level, not
inside the application.

**Database:**

- Target: PostgreSQL.
- Current H2 is an accepted stepping stone; `JdbcClient` + Liquibase make migration
  straightforward.
- `MERGE INTO` (H2) becomes `INSERT ... ON CONFLICT DO UPDATE` on PostgreSQL.

---

## 8. Testing Strategy

| Layer               | Approach                                                                                    | Spring context    |
|---------------------|---------------------------------------------------------------------------------------------|-------------------|
| Domain              | Pure Java unit tests. No mocks needed for value objects; use fakes for aggregates.          | None              |
| Application         | Instantiate use cases directly. Mock outbound port interfaces.                              | None              |
| Persistence adapter | `@SpringBootTest` scoped to persistence slice. Test against real (H2 or Testcontainers) DB. | Persistence slice |
| Web adapter         | `@SpringBootTest` + `MockMvc`. Mock use case interfaces.                                    | Web slice         |
| End-to-end          | Minimal. In `app`. Focused on wiring correctness, not business logic.                       | Full              |

**Rationale:** domain and application tests run without a Spring context, keeping them fast
and independent of framework changes. Adapter tests are isolated to their slice.

---

## 9. Decisions Left Open

The following are not yet resolved and should not be assumed in implementation:

1. **WebSocket module** — currently in `infra-web-backend`; may move to a dedicated `infra-websocket` module.
2. **Domain event dispatch** — aggregate methods may return a list of events, or a domain event publisher may be
   injected. Both approaches have trade-offs.
