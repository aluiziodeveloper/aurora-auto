package br.com.aurora.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** An amount in Brazilian reais, always with two decimal places. */
public record Money(BigDecimal amount) {

    /** Half-even: a half cent goes to the even neighbour, so roundings do not pile up in one direction. */
    public static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money {
        amount = amount.setScale(2, ROUNDING);
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Money cannot be negative");
        }
    }

    public static Money of(String amount) {
        return new Money(new BigDecimal(amount));
    }

    public Money plus(Money other) {
        return new Money(amount.add(other.amount));
    }

    /** Goes through the constructor: a result below zero is refused, it never becomes a credit (INC-03). */
    public Money minus(Money other) {
        return new Money(amount.subtract(other.amount));
    }

    /** Multiplies by a factor, such as the adjustment factor of RN09, and rounds the result to the cent. */
    public Money times(BigDecimal factor) {
        return new Money(amount.multiply(factor));
    }

    public boolean isLessThan(Money other) {
        return amount.compareTo(other.amount) < 0;
    }
}
