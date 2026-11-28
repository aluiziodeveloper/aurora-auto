package br.com.aurora.policy.domain;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.aurora.shared.domain.Money;
import java.math.BigDecimal;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Assume;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;

class DeductibleTest {

    @Test
    void deductsTheDeductibleFromTheEstimate() {
        var deductible = new Deductible(Money.of("2500.00"));
        assertThat(deductible.applyTo(Money.of("8000.00"))).isEqualTo(Money.of("5500.00"));
    }

    @Test
    void keepsTheCents() {
        var deductible = new Deductible(Money.of("3000.00"));
        assertThat(deductible.applyTo(Money.of("12300.50"))).isEqualTo(Money.of("9300.50"));
    }

    @Test
    void anEstimateBelowTheDeductiblePaysNothing() {
        var deductible = new Deductible(Money.of("2200.00"));
        assertThat(deductible.applyTo(Money.of("1800.00"))).isEqualTo(Money.ZERO);
    }

    @Test
    void anEstimateEqualToTheDeductiblePaysNothing() {
        var deductible = new Deductible(Money.of("2500.00"));
        assertThat(deductible.applyTo(Money.of("2500.00"))).isEqualTo(Money.ZERO);
    }

    @Property
    void theIndemnityIsNeverNegative(@ForAll("amounts") Money estimate, @ForAll("amounts") Money deductible) {
        assertThat(new Deductible(deductible).applyTo(estimate).amount()).isNotNegative();
    }

    @Property
    void theIndemnityNeverExceedsTheEstimate(@ForAll("amounts") Money estimate, @ForAll("amounts") Money deductible) {
        assertThat(estimate.isLessThan(new Deductible(deductible).applyTo(estimate)))
                .isFalse();
    }

    @Property
    void aboveTheDeductibleTheIndemnityPlusTheDeductibleIsTheEstimate(
            @ForAll("amounts") Money estimate, @ForAll("amounts") Money deductible) {
        Assume.that(!estimate.isLessThan(deductible));
        assertThat(new Deductible(deductible).applyTo(estimate).plus(deductible))
                .isEqualTo(estimate);
    }

    /** From zero to one million reais, always with cents. */
    @Provide
    Arbitrary<Money> amounts() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.ZERO, new BigDecimal("1000000.00"))
                .ofScale(2)
                .map(Money::new);
    }
}
