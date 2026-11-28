# Aurora Auto

[![CI](https://github.com/aluiziodeveloper/aurora-auto/actions/workflows/ci.yml/badge.svg)](https://github.com/aluiziodeveloper/aurora-auto/actions/workflows/ci.yml)

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

## Checks

`verify` compiles with Error Prone and NullAway, runs the tests and checks the formatting.
CI runs the same checks on every pull request, in the same image as the `dev` container.
Code in a `@NullMarked` package must declare `@Nullable` wherever null is allowed.
The `architecture-tests` module checks, with [ArchUnit](https://www.archunit.org/), that every
`domain` package is plain Java (no Spring, no JPA, no Spring AI) and that a context's domain
uses no other context's domain, except the shared kernel.

```bash
docker compose exec dev ./mvnw -q -ntp spotless:apply   # formats the code
docker compose exec dev ./mvnw -q -ntp verify           # compiles, tests and checks everything
```

Integration tests (`*IT.java`) start their own containers with
[Testcontainers](https://testcontainers.com/), so they stay out of the fast `verify` and run
with the `integration` profile. CI runs them in the `Integration` job.

```bash
docker compose exec dev ./mvnw -q -ntp -Pintegration verify   # unit and integration tests
```

The `dev` container reaches the host's Docker through `/var/run/docker.sock`, with the
socket's group added to its user. That group is `999` by default; if yours is another
(`stat -c %g /var/run/docker.sock`), recreate `dev` with it:

```bash
DOCKER_GID=$(stat -c %g /var/run/docker.sock) docker compose up -d dev
```

To keep it, write `DOCKER_GID=<group>` in a `.env` file next to `compose.yaml` (Git ignores it).

Whoever reaches that socket controls the host's Docker. Each test container publishes a
random port on the host while it runs, and Testcontainers removes it when the tests end.

## Local services

PostgreSQL 18 with pgvector and Ollama run next to the `dev` container, with no published
ports: from `dev`, they are `postgres:5432` (database, user and password `aurora`) and
`http://ollama:11434`. The chat model is `qwen3:8b`.

```bash
docker compose up -d postgres ollama
docker compose exec ollama ollama pull qwen3:8b
```

Ollama gets `OLLAMA_CPUS` CPUs (4 by default). It sizes its thread pool from the host CPUs,
not from that limit, so every request sends `num_thread` with the same number; `dev` has it
in `AURORA_OLLAMA_NUM_THREAD`.

## Workflow

`main` is protected by the ruleset in `.github/ruleset-main.json`: no direct pushes, changes
only through pull requests, squash merge only, and the checks `Format`, `Lint`, `Test`,
`Build`, `Integration` and `PR title` must be green. The pull request title follows
[Conventional Commits](https://www.conventionalcommits.org/) and becomes the commit on `main`.

Every change goes from a branch to `main` with one script, run on the host (it needs `git`
and `gh`):

```bash
git switch -c feat/some-change
# ... edit, test ...
scripts/pr.sh "feat: add some change"            # commit, push, PR, checks, squash merge
scripts/pr.sh "feat: add some change" aula-02-01 # same, and tags the merge commit
```

Check a title before opening the pull request with `scripts/check-pr-title.sh "<title>"`.
Dependabot (`.github/dependabot.yml`) opens weekly updates for the Maven dependencies and the
GitHub Actions, with titles that pass the same check. Maven itself is left out: the Wrapper
and the `dev` image change together, in one pull request.

## Decisions and incidents

The decisions are recorded in [`docs/adr`](docs/adr/README.md), the incidents in
[`docs/incidents`](docs/incidents), and the project's words in [`docs/glossary.md`](docs/glossary.md).

## Layout

| Path | Contents |
|---|---|
| `shared` | Shared kernel: value objects every context speaks |
| `customers` | Customers context: the insured people and their personal data |
| `policy` | Policy context: policies, vehicles, coverages and deductibles |
| `architecture-tests` | ArchUnit rules on the compiled classes of every context |
| `assistance` | The assistance application, where the agents will live |
| `labs/model-choice` | Lab: which local model calls the Aurora tools right, on CPU ([results](labs/model-choice/README.md)) |
| `docker/dev.Dockerfile` | The development container (JDK 25, Maven 3.9.16) |
| `docker/postgres/init.sql` | Enables pgvector when the database is created |
| `compose.yaml` | Local services |
| `.github/workflows/ci.yml` | CI: format, lint, test, build and integration jobs |
| `.github/workflows/pr-title.yml` | CI: the pull request title follows Conventional Commits |
| `.github/ruleset-main.json` | Protection rules for `main` |
| `.github/dependabot.yml` | Weekly dependency and action updates |
| `scripts/` | `pr.sh` (branch to `main`) and `check-pr-title.sh` |
| `docs/` | Decision records, incident postmortems and the glossary |
| `mvnw`, `.mvn/` | Maven Wrapper (Maven 3.9.16), the build's default options and the compiler JVM flags for Error Prone |

## License

[MIT](LICENSE)
