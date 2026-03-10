# Jeu du Tao — Quick Reference

## Build Commands

```bash
./gradlew build              # Compile + test + frontend build
./gradlew :app:bootRun       # Run the application (port 8080)
./gradlew :app:bootJar       # Build fat JAR

# Frontend (in infra-web-frontend/)
pnpm install                 # Install dependencies
pnpm run dev                 # Vite dev server (proxies /api, /actuator to :8080)
pnpm run build               # Production build → dist/
pnpm test                    # Run Vitest tests
pnpm run lint                # ESLint
```

## Module Structure

```
app           — Spring Boot entry point; serves frontend SPA at /
infra-web-backend   — REST controllers under /api/** (all REST endpoints go here)
infra-web-frontend  — React SPA (non-Java; uses pnpm + Vite)
```

### API conventions

- All REST endpoints live in `infra-web-backend` under the `/api/**` prefix.
- `/actuator/**` is reserved for Spring Boot actuator.
- Any unmatched path is served `index.html` by the SPA fallback (`SpaWebConfig`).

## Spring Boot 4 Critical Standards

- `spring-boot-starter-webmvc` (not `spring-boot-starter-web`)
- `spring-boot-starter-webmvc-test` (not `spring-boot-starter-test`)
- BOM imported via `implementation(platform(SpringBootPlugin.BOM_COORDINATES))` — no `io.spring.dependency-management` plugin
- Jackson 3 group: `tools.jackson` (not `com.fasterxml.jackson`)
- `@AutoConfigureMockMvc` is NOT available; use `MockMvcBuilders.webAppContextSetup(context)` instead.
