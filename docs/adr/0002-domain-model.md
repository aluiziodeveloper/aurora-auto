# ADR 0002: The domain model

- **Status:** Accepted
- **Date:** 2026-11-28
- **Module:** 03, The Aurora Auto domain

## Context

Aurora's rules decide money: what the insurer pays, when a policy covers an event, how much a
car is worth. In INC-03 a repair of R$ 1,800.00 with a deductible of R$ 2,200.00 became an
indemnity of R$ -400.00, both in a plain `BigDecimal` subtraction and in the text of a
language model. The rule lived nowhere, so every place that did the math had its own version.
From module 06 on, agents call tools that reach these rules; the rules must hold whatever
calls them.

## Decision

1. **One Maven module per bounded context,** with the domain in plain Java. `shared` (the
   shared kernel), `customers` and `policy` each have a `domain` package marked
   `@NullMarked`. `architecture-tests` checks with ArchUnit 1.5.1 that a `domain` class only
   depends on `java..`, `javax.crypto..`, `org.jspecify..` and other `domain` packages, and
   that two contexts only share `br.com.aurora.shared.domain`.
2. **Value objects are records with a compact constructor.** An invalid value does not exist:
   `Cpf` checks its check digits, `LicensePlate` the Mercosul format (converting the old one),
   `PolicyNumber` the `AUR-2026-000123` format. A value object never shows personal data in
   `toString`: `Cpf` shows only its mask, `LicensePlate` nothing (RN04).
3. **The plate leaves its context as a keyed pseudonym:** HMAC-SHA256 with a key of at least
   32 bytes, 12 hex digits. A plain hash would be reversible by brute force (see
   Measurements). The key comes from configuration in module 06.
4. **Money is `Money`, in the shared kernel:** a `BigDecimal` with two decimal places, rounded
   half-even (`RoundingMode.HALF_EVEN`), never negative, created from text. Every operation
   returns a new `Money` through the constructor, so the scale, the rounding and the sign are
   checked in one place. Half-even comes from Aurora's general conditions: a half cent goes to
   the even neighbour, so many roundings do not drift up.
5. **The coverage period includes its first and last day,** with `LocalDate`, and
   `covers(eventDate)` answers for the date of the event, not of the notice (RN01). The
   application converts an instant to a date in `America/Sao_Paulo` before asking.
6. **The deductible rule is a type:** `Deductible.applyTo(estimate)` returns the estimate minus
   the deductible, or `Money.ZERO` when the estimate is at or below it (RN05). Registering the
   claim without indemnity, and skipping the deductible in a total loss, belong to the `Claim`
   aggregate (module 04).
7. **The reference value is the Aurora table value times the contracted adjustment factor**
   (RN09). `AdjustmentFactor` accepts 0.80 to 1.20 with at most two decimals and rejects a
   third decimal instead of rounding it: a factor outside the contract does not exist.
8. **`Policy` is an entity and the root of its aggregate,** equal by its policy number only.
   Its coverages are `Coverages`, a first-class collection: at least one coverage, one per
   `CoverageType`, an unmodifiable copy of the list it receives. The root checks the rule that
   spans the group (an endorsement only inside the coverage period); a refused change leaves
   the policy as it was.
9. **Rules are tested with examples and properties.** JUnit Jupiter holds the examples,
   including each business decision (the half-even case, the borders of the period); jqwik
   1.10.2 holds the properties (an indemnity is never negative nor above the estimate; a
   reference value never decreases with the table value and stays in the 80% to 120% band).
   jqwik keeps its failure database in `target/` and reports only failing properties
   (`policy/src/test/resources/junit-platform.properties`).

## Measurements

Taken in the module 03 lessons, in the `dev` container (JDK 25.0.4.1), on an i7-13700H.

| What | Result | Lesson, date |
|---|---|---|
| INC-03 reproduced | `1800.00 - 2200.00 = -400.00` in `jshell`; `qwen3:1.7b` wrote `1800 - 2200 = -400` in its answer (prototype P1) | 03-01, 2026-11-28 |
| Plain SHA-256 of a plate, one thread | 6.8 million hashes per second: all 456,976,000 Mercosul plates in about 67 s | 03-06, 2026-11-28 |
| `BigDecimal` traps | `10` and `10.00` not `equals`, `compareTo` 0; `new BigDecimal(0.1)` carries the `double` error; `1/3` without rounding throws `ArithmeticException` | 03-07, 2026-11-28 |
| Naive deductible under jqwik | Failed at the 4th try (233.86 against 148,059.66), shrunk in 2 steps to estimate 0.00 and deductible 0.01 | 03-09, 2026-11-28 |
| `Money` switched to half-up for one run | The example 10,000.10 × 1.05 failed (10,500.11, expected 10,500.10); the 3 properties passed | 03-10, 2026-11-28 |

## Alternatives rejected

- **`double` for money:** `0.1 + 0.2` is `0.30000000000000004`.
- **Cents in a `long`:** exact, but every multiplication by a factor needs a hand-written
  rounding, and every value from outside a conversion.
- **JSR 354 (Moneta):** complete and multi-currency; Aurora has one currency, and the domain
  stays without a dependency.
- **Half-up:** every half cent goes up, and a sum of roundings drifts.
- **A plain hash for the plate:** reversible in about a minute on one thread.
- **A `record` for `Policy`:** its `equals` compares every component, so the same policy after
  an endorsement would be another policy (lesson 03-11).
- **A `List<Coverage>` in the policy:** every place that receives it would repeat the rules,
  and whoever holds the list could change the policy (lesson 03-11).
- **Spring Modulith now:** it checks the dependencies of a Spring application; module 05 adds
  it when the application layer exists. Until then, ArchUnit checks the domain.

## Consequences

- The domain compiles and tests without Spring: `verify -pl policy -am` builds three modules.
- A new value object needs a test for its `toString` when it holds personal data.
- Any code that computes an indemnity, including a tool an agent calls, goes through
  `Deductible`; a direct subtraction of `Money` below zero throws instead of paying.
- Changing the rounding mode changes amounts already shown to customers: it needs a new ADR.
- A new dependency in a `domain` package fails `architecture-tests` until the rule names it,
  with the reason in a comment.
