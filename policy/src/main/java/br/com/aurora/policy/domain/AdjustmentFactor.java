package br.com.aurora.policy.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The adjustment factor of the policy: the percentage of the Aurora table value the insured contracted (RN09). The
 * Aurora general conditions allow 80% to 120%, in whole points, so the factor has at most two decimals.
 */
public record AdjustmentFactor(BigDecimal value) {

    private static final BigDecimal MIN = new BigDecimal("0.80");
    private static final BigDecimal MAX = new BigDecimal("1.20");

    public AdjustmentFactor {
        try {
            value = value.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Adjustment factor must have at most two decimals", e);
        }
        if (value.compareTo(MIN) < 0 || value.compareTo(MAX) > 0) {
            throw new IllegalArgumentException("Adjustment factor must be between 0.80 and 1.20");
        }
    }

    public static AdjustmentFactor of(String value) {
        return new AdjustmentFactor(new BigDecimal(value));
    }
}
