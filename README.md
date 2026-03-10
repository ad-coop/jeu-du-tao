# Jeu du Tao

A collaborative game that harnesses collective intelligence to help participants achieve their goals.

## Running the application

```bash
./gradlew :app:bootRun   # starts on http://localhost:8080
```

## Database

The application uses an H2 file-based database stored in `app/data/jeudutao.mv.db` (excluded from git).

On startup, H2 also starts a TCP server on port **9092**, allowing external connections while the app is running.

### Connecting with an external tool (IntelliJ, DBeaver, etc.)

| Setting  | Value                                          |
|----------|------------------------------------------------|
| URL      | `jdbc:h2:tcp://localhost:9092/./data/jeudutao` |
| User     | `sa`                                           |
| Password | `sa`                                           |

> The app must be running for the TCP server to be available.

## More

- [`doc/quick-reference.md`](doc/quick-reference.md) — build commands, module structure, critical standards
- [`doc/vision.md`](doc/vision.md) — long-term vision
