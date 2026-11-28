# INC-03: The deductible that became a credit

- **Date:** 2026-11-28
- **Module:** 03, The Aurora Auto domain
- **Reproduction:** lesson 03-01, `jshell` in `dev` and the prototype P1; after the fix,
  `docs/incidents/inc-03/deductible.jsh`

## Summary

A partial loss with a repair estimate of R$ 1,800.00 and a deductible of R$ 2,200.00 produced
an indemnity of R$ -400.00. A payment system that receives -400 to pay can treat it as 400 to
receive: the deductible becomes a credit. RN05 says the result is never negative, and an
estimate below the deductible registers the claim without indemnity.

## Impact

The same wrong number came from two places:

| Where | What it produced |
|---|---|
| Java, `BigDecimal` subtraction (`jshell`, JDK 25.0.4.1) | `repair - deductible = -400.00`, `negative? true` |
| `qwen3:1.7b`, asked "Se o conserto ficar em 1.800 reais e a franquia for 2.200 reais, quanto eu recebo?" (prototype P1) | No tool call; the answer wrote `1800 - 2200 = -400` |

No error and no warning in either: a negative `BigDecimal` is a valid number, and the model's
answer was fluent. `qwen3:8b` called `calculateIndemnity`, but sent the amounts as plain JSON
numbers (`1800`, `2200`, and `12300.5` in another case), without currency or scale.

## Cause

- **The rule lived nowhere.** Each place that computed the indemnity had its own version of
  it; the model was one more such place, and the least reliable one.
- **Money was a plain number.** A `BigDecimal` accepts a negative amount and any scale; a
  `double` cannot even hold most cents.
- **The number does not carry the kind of loss.** In a total loss the deductible does not
  apply; a bare amount cannot know which loss it came from.

## Fix

- `Money` (shared kernel, lesson 03-07): two decimals, half-even, never negative. The naive
  subtraction now throws `IllegalArgumentException: Money cannot be negative`.
- `Deductible.applyTo` (lesson 03-09): the RN05 rule in one place, `Money.ZERO` at or below the
  deductible.
- Amounts from outside, a tool argument included, become `Money` through `Money.of(String)` or
  the constructor, which fixes the scale.

The reproduction, after the fix:

```
indemnity = Money[amount=0.00]
naive minus: Money cannot be negative
```

## Regression check

- `DeductibleTest`: four examples (the INC-03 case among them) and three jqwik properties, 1,000
  tries each: the indemnity is never negative, never above the estimate, and above the
  deductible indemnity plus deductible is the estimate. The naive formula fails them at once
  and shrinks to estimate 0.00 and deductible 0.01 (lesson 03-09).
- `MoneyTest.refusesASubtractionThatWouldGoBelowZero`.
- Module 04: `Claim` registers the claim without indemnity and skips the deductible in a total
  loss. Module 08: the `calculateIndemnity` tool goes through `Deductible`, and its argument
  type is not a JSON number.

## Lessons

- A rule that decides money belongs to a type, not to each caller.
- A model that writes a calculation is one more caller: give it a tool, and keep the rule in
  the code the tool calls.
- A property test finds the counterexample nobody wrote; an example test pins the decision
  nobody should change by accident.
