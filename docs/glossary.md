# Glossary

Two glossaries in one file. **Domain** is Aurora's ubiquitous language: the words the code,
the tests and the docs use for car insurance claims. The technical glossary, **Build and
Java**, **Workflow** and **Infrastructure and models**, holds the terms the project relies on.
Each entry says in which lesson it first appears.

## Domain

The ubiquitous language starts in module 03. The code uses these words in English; the
lessons use the Portuguese word in parentheses.

| Term | Meaning | First seen |
|---|---|---|
| **CPF** (CPF) | `Cpf`: the Brazilian individual taxpayer number, eleven digits, the last two are check digits. Outside the customers context it only appears masked, `***.456.789-**` (RN04) | 03-05 |
| **license plate** (placa) | `LicensePlate`: the vehicle's plate, kept in the Mercosul format (BRA2E19); an old plate (ABC1234) is converted (ABC1C34). Before the model it becomes a keyed pseudonym, `plate-` and 12 hex digits (RN04) | 03-06 |
| **money** (dinheiro, valor) | `Money`: an amount in reais, always with two decimal places and rounded half-even; never negative, so a subtraction below zero is refused (INC-03). Lives in the shared kernel | 03-07 |
| **policy number** (número da apólice) | `PolicyNumber`: AUR, the year of issue and a six-digit sequence, such as `AUR-2026-000123` | 03-08 |
| **coverage period** (vigência) | `CoveragePeriod`: the days the policy is in force, first and last included. A claim is accepted for the date of the event, not of the notice (RN01) | 03-08 |
| **deductible** (franquia) | `Deductible`: the part of a partial loss the insured pays. It comes off the authorized estimate, and the result is never negative: an estimate at or below it pays nothing, and the claim is registered without indemnity. It does not apply to a total loss (RN05) | 03-09 |
| **adjustment factor** (fator de ajuste) | `AdjustmentFactor`: the percentage of the Aurora table value the insured contracted, from 0.80 to 1.20 in whole points (RN09) | 03-10 |
| **reference value** (valor de referência) | `ReferenceValue`: the Aurora table value times the adjustment factor; the base of the total loss rule (RN08) and of a total loss indemnity (RN09) | 03-10 |
| **policy** (apólice) | `Policy`: the root of the aggregate with the vehicle, the coverage period, the adjustment factor and the coverages; an entity, identified by its policy number | 03-11 |
| **coverage** (cobertura) | `Coverage`: what the policy protects against (`CoverageType`: collision, theft, fire, glass, third party), each with its own deductible; `Coverages` is the first-class collection, one coverage per type | 03-11 |
| **endorsement** (endosso) | `Policy.endorse`: a change to the coverages during the coverage period; the policy stays the same | 03-11 |

## Domain design

The modeling terms of module 03: how the domain above is built.

| Term | Meaning | Lesson |
|---|---|---|
| **bounded context** (contexto delimitado) | A part of the business with its own model and words; in the code, a Maven module (`customers`, `policy`) | 03-02 |
| **shared kernel** (núcleo compartilhado) | The small model every context may use: `shared`, with `Money` | 03-02 |
| **value object** | A type defined by its values, with no identity: immutable, valid from the constructor on (`Cpf`, `Money`) | 03-03 |
| **entity** (entidade) | A type with an identity that stays while its data changes: `Policy`, by its number | 03-03 |
| **aggregate** (agregado) | A group that changes as one unit, through its root, which checks the rules of the whole group | 03-03 |
| **first-class collection** (coleção de primeira classe) | A class that wraps a collection and holds its rules: `Coverages` | 03-03 |
| **ArchUnit** | The library that tests the dependencies of compiled classes; `architecture-tests` keeps the domain plain Java | 03-04 |
| **pseudonym** (pseudônimo) | A stand-in that identifies a value without showing it; the plate's is a keyed HMAC | 03-06 |
| **property-based test** (teste de propriedade) | A test of a rule that must hold for any generated input; jqwik runs 1,000 tries per property | 03-09 |
| **shrinking** (encolhimento) | jqwik reducing a failing input, step by step, to the smallest one that still fails | 03-09 |

## Build and Java

| Term | Meaning | First seen |
|---|---|---|
| **Maven Wrapper** (`./mvnw`) | The script that downloads and runs the Maven version pinned in `.mvn/wrapper/maven-wrapper.properties` (3.9.16) | 01-02 |
| **multi-module build** | One Maven build at the root, with one module per part of the project (`assistance`, `labs/model-choice`) | 01-02 |
| **BOM** | A POM that only pins versions; importing `spring-ai-bom` lets the modules declare Spring AI dependencies without versions | 01-08 |
| **Error Prone** | A compiler plugin that turns common Java mistakes into compilation errors | 01-04 |
| **NullAway** | An Error Prone check that fails the compilation when a value that may be null is used as if it were not | 01-04 |
| **`@NullMarked`** | The JSpecify annotation that makes every type in a package non-null unless it says `@Nullable` | 01-04 |
| **Spotless** | The Maven plugin that checks (`spotless:check`) and applies (`spotless:apply`) the formatting, with palantir-java-format | 01-04 |
| **Surefire** | The Maven plugin that runs the unit tests (`*Test`) in the `test` phase | 01-01 |
| **Failsafe** | The Maven plugin that runs the integration tests (`*IT`) in `integration-test` and fails the build in `verify` | 01-09 |
| **profile** (perfil) | A block of the POM that only applies when asked for, with `-P`: `-Pintegration` | 01-09 |

## Workflow

| Term | Meaning | First seen |
|---|---|---|
| **tag** (`aula-MM-NN`) | The commit on `main` where each lesson ends | 01-02 |
| **Conventional Commits** | The title format `type(scope): summary`; the pull request title becomes the commit on `main` | 01-02 |
| **job** | One part of a CI run, with its own machine; Aurora's jobs run in the `dev` image | 01-05 |
| **cache** (Actions) | The saved `~/.m2`, restored by key; a cache saved in a pull request is not seen by `main` | 01-05 |
| **ruleset** | GitHub's protection rules for `main`, kept in `.github/ruleset-main.json` | 01-06 |
| **required check** (check obrigatório) | A job that must pass before a pull request can be merged | 01-06 |
| **squash merge** | Merging a pull request as a single commit on `main` | 01-05 |
| **Dependabot** | GitHub's bot that opens pull requests with dependency updates, weekly | 01-06 |

## Infrastructure and models

| Term | Meaning | First seen |
|---|---|---|
| **`dev` container** | The container with the JDK and Maven where every build and test runs, as your user | 01-02 |
| **Compose** | The file `compose.yaml` and the tool that starts Aurora's services together, in the project `aurora-auto` | 01-02 |
| **healthcheck** | The command Compose runs to tell when a service is ready (`healthy`) | 01-07 |
| **pgvector** | The PostgreSQL extension with the `vector` type and its distance operators (`<->`, `<=>`) | 01-07 |
| **cosine distance** (distância de cosseno) | How far apart two vectors point, ignoring their length: the `<=>` operator | 01-09 |
| **Ollama** | The server that runs the language models locally, at `http://ollama:11434` | 01-07 |
| **`num_thread`** | The Ollama option with the number of threads for the model; it must match the container's CPU quota (INC-01) | 01-07 |
| **tool calling** | The model answering with the tool to call and its arguments, instead of text | 01-08 |
| **lab** (bancada) | An experiment outside the domain that measures something before the course depends on it (`labs/`) | 01-08 |
| **Testcontainers** | The Java library that starts real containers for a test and removes them at the end | 01-09 |
| **Ryuk** | Testcontainers' container that removes the test containers when the test JVM ends | 01-09 |
| **Docker socket** | `/var/run/docker.sock`, the door to the host's Docker; whoever reaches it controls that Docker | 01-09 |
