# Aurora Auto

Claims assistance with agents for Aurora Seguros, a fictional car insurer. The domain is written
with DDD and TDD in Java 25; the language model sits at the edge, behind a port, and runs locally
with Ollama. Built with Spring Boot 4.2 and Spring AI 2.1.

## Requirements

- Docker with Compose. JDK 25 and Maven run inside the `dev` container; nothing else is installed on your machine.
- Git on the host.

## Getting started

```bash
docker compose up -d --build dev
docker compose exec dev ./mvnw -q -ntp verify
```

The container runs as a user with UID and GID 1000, so the files it creates in the project
belong to you. If your user has other IDs (`id -u`, `id -g`), build with them:

```bash
DEV_UID=$(id -u) DEV_GID=$(id -g) docker compose build dev
```

## Layout

| Path | Contents |
|---|---|
| `assistance` | The assistance application, where the agents will live |
| `docker/dev.Dockerfile` | The development container (JDK 25, Maven 3.9.16) |
| `compose.yaml` | Local services |
| `mvnw`, `.mvn/` | Maven Wrapper (Maven 3.9.16) and the build's default options |

## License

[MIT](LICENSE)
