package br.com.aurora.policy.domain;

import br.com.aurora.shared.domain.Money;

/** The deductible of a coverage: the part of a partial loss the insured pays. */
public record Deductible(Money amount) {

    /**
     * RN05, partial loss: the deductible comes off the authorized estimate and the result is never negative. An estimate
     * at or below the deductible pays nothing, and the claim is registered without indemnity. In a total loss the
     * deductible does not apply, so this method is not called (module 04).
     */
    public Money applyTo(Money estimate) {
        if (!amount.isLessThan(estimate)) {
            return Money.ZERO;
        }
        return estimate.minus(amount);
    }
}
