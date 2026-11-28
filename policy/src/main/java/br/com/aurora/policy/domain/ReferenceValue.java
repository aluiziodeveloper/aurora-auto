package br.com.aurora.policy.domain;

import br.com.aurora.shared.domain.Money;

/**
 * The reference value of the insured vehicle: the base of the total loss rule (RN08, module 04) and of a total loss
 * indemnity. RN09: the Aurora table value times the adjustment factor the insured contracted.
 */
public record ReferenceValue(Money amount) {

    public static ReferenceValue of(Money tableValue, AdjustmentFactor factor) {
        return new ReferenceValue(tableValue.times(factor.value()));
    }
}
