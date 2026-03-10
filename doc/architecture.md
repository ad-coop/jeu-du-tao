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
domain/              Pure Java. Aggregates, value objects, domain events, command port interfaces.
application/         Pure Java. Depends on domain only. Command + query use cases as plain classes.
                     Owns query port interfaces and read models (GameInfoView, PlayerView).
infra-persistence/   Depends on domain + application. JDBC repositories (command + query),
                     Liquibase migrations, DB config.
infra-web-backend/   Depends on application (+ domain types). REST controllers, DTOs,
                     exception handlers, WebSocket adapter.
app/                 Spring Boot entry point. Depends on all infra modules.
                     Wires beans via @Bean methods. Serves the SPA.
infra-web-frontend/  React SPA.
```

### Per-module detail

| Module               | Contains                                                                                                                                                                                       | Allowed dependencies                                 | Forbidden dependencies                              |
|----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------|-----------------------------------------------------|
| `domain`             | Aggregates, entities, value objects, domain events, command port interfaces (`GameCommandRepository`, `PlayerCommandRepository`)                                                                | Java stdlib                                          | Spring, Jakarta EE, any infra module, `application` |
| `application`        | `GameCommandService`, `GameQueryService` use cases; query port interfaces (`GameQueryRepository`, `PlayerQueryRepository`); read models (`GameInfoView`, `PlayerView`); outbound port interfaces (`PasswordEncoder`, `MagicLinkSender`) | `domain`, Java stdlib | Spring, Jakarta EE, any infra module |
| `infra-persistence`  | JDBC command + query repository implementations, Liquibase scripts, datasource config                                                                                                          | `domain`, `application`, Spring JDBC, Liquibase      | other `infra-*` modules                             |
| `app`                | `@SpringBootApplication`, `@Bean` wiring, `SpaWebConfig`, application properties                                                                                                              | All modules                                          | Business logic of any kind                          |
| `infra-web-backend`  | REST controllers, WebSocket handlers, DTOs, JSON mappers, exception handlers                                                                                                                   | `application`, `domain` (value types), Spring WebMVC | `infra-persistence`                                 |
| `infra-web-frontend` | React SPA source                                                                                                                                                                               | (pnpm ecosystem)                                     | —                                                   |

**Important:** `domain` and `application` do **not** include the Spring BOM. Use cases are
plain Java classes instantiated via `@Bean` methods in `app`, never annotated with `@Service`
or `@Component`.

**Note on `infra-persistence`:** this module depends on both `domain` (for command repositories that
handle aggregates) and `application` (for query repositories that return read models). This is the
only intended exception to the rule that `infra-*` modules depend only on `domain`.

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

**Command repository ports** — defined in `domain`, implemented in `infra-persistence`.
They deal with aggregates and belong to the domain:

```
domain.game.port.GameCommandRepository
domain.game.port.PlayerCommandRepository
```

**Query repository ports** — defined in `application`, implemented in `infra-persistence`.
They return read models (`GameInfoView`, `PlayerView`) and therefore cannot live in `domain`
(which cannot depend on `application`):

```
application.game.query.GameQueryRepository
application.game.query.PlayerQueryRepository
```

**Infrastructure service ports** — defined in `application`, implemented in `infra-*`:

```
application.port.PasswordEncoder
application.port.MagicLinkSender
```

### Adapter summary

| Adapter type        | Location                                  | Calls                                                                               |
|---------------------|-------------------------------------------|-------------------------------------------------------------------------------------|
| Inbound — REST      | `infra-web-backend`                       | `GameCommandService`, `GameQueryService`                                            |
| Inbound — WebSocket | `infra-web-backend` (or dedicated module) | `GameCommandService`, `GameQueryService`                                            |
| Outbound — JDBC (command) | `infra-persistence`               | Implements `domain.game.port.GameCommandRepository`, `PlayerCommandRepository`      |
| Outbound — JDBC (query)   | `infra-persistence`               | Implements `application.game.query.GameQueryRepository`, `PlayerQueryRepository`    |
| Outbound — SMTP     | `infra-mail`                              | Implements `application.port.MagicLinkSender`                                       |

---

## 4. CQRS: Command/Query Responsibility Segregation

The application layer is split along CQRS lines:

| Side    | Service              | Purpose                                  | Returns                                    |
|---------|----------------------|------------------------------------------|--------------------------------------------|
| Command | `GameCommandService` | State mutations: create, join, kick, restore | Typed result records with primitives only  |
| Query   | `GameQueryService`   | Read-only projections: game info, player list | `GameInfoView`, `List<PlayerView>` read models |

### Why query ports live in `application`, not `domain`

Command repository ports (`GameCommandRepository`, `PlayerCommandRepository`) deal with domain
aggregates (`Game`, `Player`) and belong in `domain`.

Query repository ports (`GameQueryRepository`, `PlayerQueryRepository`) return application-level
read models (`GameInfoView`, `PlayerView`). These read models are projections — they are not domain
entities. Since `domain` cannot depend on `application`, the query ports must live in `application`.

This is the reason `infra-persistence` depends on `application`: it must implement interfaces that
live there.

### Synchronisation

Both sides read from and write to the same database tables. There is no event sourcing, no separate
read store, and no eventual consistency. The command and query sides are synchronised within the
same transaction.

### Result types

`GameCommandService` result records expose only primitives and strings. No domain objects (`Game`,
`Player`) leak out of the application layer. This decouples the web layer from the domain model.

---

## 5. DDD Tactical Patterns

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

The dispatch mechanism (return values vs. event publisher) is a decision left open (see section 10).

### Repositories as ports

The repository layer is split along CQRS lines:

**Command repositories** — defined in `domain`, handle aggregate load/save:

```java
// domain.game.port
public interface GameCommandRepository {
    void save(Game game);
    Optional<Game> findByHandle(String handle);
    boolean existsByHandle(String handle);
}

public interface PlayerCommandRepository {
    void save(Player player);
    Optional<Player> findById(String id);
    List<Player> findByGameHandle(String gameHandle);
    void deleteById(String id);
}
```

**Query repositories** — defined in `application`, return read model projections:

```java
// application.game.query
public interface GameQueryRepository {
    Optional<GameInfoView> findGameInfo(String handle);
}

public interface PlayerQueryRepository {
    List<PlayerView> findPlayersByGame(String gameHandle);
}
```

All JDBC implementations live in `infra-persistence` and are wired via `@Bean` in `app`.
The synchronisation strategy is synchronous (same DB tables, same transaction).

---

## 6. Package Conventions

Root package: `fr.adcoop.jeudutao`

```
domain/
  fr.adcoop.jeudutao.domain.game          ← Game, Player, GameState, PlayerRole, …
  fr.adcoop.jeudutao.domain.game.event    ← GameCreated, PlayerJoined, …
  fr.adcoop.jeudutao.domain.game.port     ← GameCommandRepository, PlayerCommandRepository
  fr.adcoop.jeudutao.domain.shared        ← shared value objects (EmailAddress, …)

application/
  fr.adcoop.jeudutao.application.game.command  ← GameCommandService
  fr.adcoop.jeudutao.application.game.query    ← GameQueryService, GameQueryRepository,
                                                  PlayerQueryRepository, GameInfoView, PlayerView
  fr.adcoop.jeudutao.application.port          ← PasswordEncoder, MagicLinkSender

infra-persistence/
  fr.adcoop.jeudutao.infra.persistence.game    ← JdbcGameCommandRepository,
                                                  JdbcPlayerCommandRepository,
                                                  JdbcGameQueryRepository,
                                                  JdbcPlayerQueryRepository
  fr.adcoop.jeudutao.infra.persistence.config  ← DataSource, Liquibase config

infra-web-backend/
  fr.adcoop.jeudutao.infra.web.game       ← GameController, DTOs, PlayerInfo
  fr.adcoop.jeudutao.infra.web.websocket  ← WebSocketDisconnectListener, WebSocketSessionManager
  fr.adcoop.jeudutao.infra.web.i18n       ← I18nController
  fr.adcoop.jeudutao.infra.web.config     ← SpaWebConfig, WebSocket config

app/
  fr.adcoop.jeudutao                      ← @SpringBootApplication
  fr.adcoop.jeudutao.config               ← @Bean wiring for use cases and adapters
```

---

## 7. Dependency Rules

| Module              | Depends on                                              | Must NOT depend on                                  |
|---------------------|---------------------------------------------------------|-----------------------------------------------------|
| `domain`            | Java stdlib                                             | Spring, Jakarta EE, any infra module, `application` |
| `application`       | `domain`, Java stdlib                                   | Spring, Jakarta EE, any infra module                |
| `infra-persistence` | `domain`, `application`¹, Spring JDBC, Liquibase        | other `infra-*` modules                             |
| `infra-web-backend` | `application`, `domain` (value types), Spring WebMVC    | `infra-persistence`                                 |
| `app`               | All modules                                             | Business logic of any kind                          |

¹ `infra-persistence` depends on `application` specifically to implement the query repository ports
(`GameQueryRepository`, `PlayerQueryRepository`) which return application-level read models.
This is the only sanctioned exception to the rule that `infra-*` modules do not depend on `application`.

**Enforcement:** Gradle module declarations are the primary enforcement mechanism. Because
`domain` and `application` do not declare Spring as a dependency, any accidental `@Service`
or `@Autowired` annotation will fail to compile. ArchUnit may be adopted later for finer-grained intra-module rules (see
section 10).

---

## 8. Infrastructure: Statelessness & Horizontal Scaling

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

## 9. Testing Strategy

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

## 10. Decisions Left Open

The following are not yet resolved and should not be assumed in implementation:

1. **WebSocket module** — currently in `infra-web-backend`; may move to a dedicated `infra-websocket` module.
2. **Domain event dispatch** — aggregate methods may return a list of events, or a domain event publisher may be
   injected. Both approaches have trade-offs.
