# ADR 0001: Toolchain, development environment and workflow

- **Status:** Accepted
- **Date:** 2026-11-28
- **Module:** 01, Environment and workflow

## Context

Aurora Auto is a Spring Boot application whose agents call a local language model. Every
student, every CI job and every recording must build it with the same JDK, the same Maven and
the same checks, and every number shown in the course must come from a real run. The model
runs on CPU, where a wrong setting doubles the time of each answer (INC-01), and the agents
depend on a model that picks the right tool with the right arguments.

## Decision

1. **JDK 25 and Maven 3.9.16, pinned, only in the `dev` container.** `docker/dev.Dockerfile`
   starts `FROM maven:3.9.16-eclipse-temurin-25` and runs as the host user (UID/GID 1000 by
   default), so generated files belong to you. The host only needs Docker, Git and `gh`.
   `./mvnw` (`.mvn/wrapper/maven-wrapper.properties`) and the image change together, in one
   pull request, never through Dependabot: `.github/dependabot.yml` ignores
   `org.apache.maven:apache-maven`.
2. **One multi-module Maven build.** The root inherits `spring-boot-starter-parent` 4.2.0 and
   imports `spring-ai-bom` 2.1.0; `assistance` is the application and `labs/` holds the labs.
3. **Checks at compile time and in `verify`.** Error Prone 2.53.0 with NullAway 0.14.4
   (`OnlyNullMarked`) fails the compilation; packages declare `@NullMarked` (JSpecify).
   Spotless 3.12.0 with palantir-java-format 2.103.0 fails `verify` on unformatted code.
4. **Readable test output.** Mockito loads as an agent with `-Xshare:off`, tests log at `WARN`
   without the banner, and `.mvn/maven.config` keeps the Surefire and Failsafe summaries
   visible under `-q`.
5. **CI on every pull request,** in the same image as `dev`: `Format`, `Lint`, `Test`,
   `Build`, `Integration` and `PR title` (Conventional Commits, `scripts/check-pr-title.sh`).
   Only `Test` saves the Maven cache; the other jobs restore it.
6. **`main` is protected** by `.github/ruleset-main.json`: changes only through pull requests,
   the six checks green, squash merge only, linear history, no force pushes, no deletion, no
   bypass. `scripts/pr.sh` takes a branch to `main` (commit, push, pull request, checks,
   squash merge, tag). Dependabot proposes Maven and Actions updates weekly, with titles that
   pass the same check.
7. **Local services in Compose, with no published ports:** PostgreSQL 18 with pgvector 0.8.7
   (`pgvector/pgvector:0.8.7-pg18-trixie`, extension enabled by `docker/postgres/init.sql`)
   and `ollama/ollama:0.40.16`, with a CPU quota (`OLLAMA_CPUS`, 4 by default). Every request
   to Ollama sends the same number as `num_thread` (`AURORA_OLLAMA_NUM_THREAD`) (INC-01).
8. **Chat model: `qwen3:8b`,** with thinking off and temperature 0, chosen by
   `labs/model-choice`. Plan B for smaller machines: `qwen3:4b-instruct`.
9. **Integration tests with Testcontainers.** Classes named `*IT` run in Failsafe, in the
   `integration` profile of the module that has them, out of the fast `verify`. They start
   their own containers through the host's Docker: `dev` gets `/var/run/docker.sock` and its
   group (`DOCKER_GID`). The test database uses the Compose image and `init.sql`.

## Measurements

Taken in the module 01 lessons, on an i7-13700H (20 CPUs) under WSL 2, and on the public
GitHub runner.

| What | Result | Lesson, date |
|---|---|---|
| Maven cache in CI | Maven step of `Test` from 22 s to 9 s; `Lint` 20 to 3 s, `Build` 21 to 5 s, `Format` 8 to 2 s | 01-05, 2026-11-26 |
| `qwen3:8b`, same answer (51 tokens), model loaded | 8.6 s of generation with `num_thread` 4, 19.8 s without it | 01-07, 2026-11-27 |
| Tool calling, 30 cases, 4 threads | `qwen3:8b` 30/30 at 11.7 s per case; `qwen3:4b-instruct` 23/30; `qwen3:1.7b` 12/30 | 01-08, 2026-11-27 |
| `PgvectorDatabaseIT`, 2 tests | 2.7 s in `dev`, 8.6 s on the runner; `verify` 6 s, `-Pintegration verify` 9 s | 01-09, 2026-11-28 |

## Alternatives rejected

- **JDK and Maven installed on the host:** every machine would build with a different version.
- **Gradle:** Maven is what most of the course's audience uses at work.
- **google-java-format:** two-space indentation would reformat code already tagged.
- **Dependabot updating the Maven Wrapper alone:** `./mvnw` and the image's `mvn` would run
  different Maven versions (it proposed 3.10.2 for the Wrapper only, PR #4).
- **Letting Ollama pick its threads:** it sizes them from the host CPUs, not from the quota
  (INC-01).
- **An in-memory database for tests:** it has no pgvector, so it would not test the real one.
- **Binding Testcontainers' ports to the gateway IP:** it would work on Linux and break on
  Docker Desktop. The random ports stay open only while the tests run.
- **Merge commits:** squash keeps one commit per pull request, titled like the pull request.

## Consequences

- A new machine needs only Docker, Git and `gh`; the first build downloads everything once.
- Changing the JDK or Maven means changing the image and the Wrapper in the same pull request,
  and refreshing the outputs shown in the lessons.
- Null checks and formatting stop the build, so a pull request never fails review on them.
- The `dev` container controls the host's Docker through the socket. Only `dev` gets it.
- Removing `num_thread` from a request brings INC-01 back without any error message.
