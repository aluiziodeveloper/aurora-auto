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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ReferenceValueTest {

    @ParameterizedTest
    @CsvSource({
        "80000.00, 1.05, 84000.00",
        "52345.67, 0.95, 49728.39",
        "10000.10, 1.05, 10500.10",
        "64990.00, 1.00, 64990.00"
    })
    void isTheTableValueTimesTheContractedFactor(String table, String factor, String expected) {
        var reference = ReferenceValue.of(Money.of(table), AdjustmentFactor.of(factor));
        assertThat(reference.amount()).isEqualTo(Money.of(expected));
    }

    @Property
    void theNeutralFactorKeepsTheTableValue(@ForAll("tableValues") Money table) {
        assertThat(ReferenceValue.of(table, AdjustmentFactor.of("1.00")).amount())
                .isEqualTo(table);
    }

    @Property
    void aHigherTableValueNeverGivesALowerReferenceValue(
            @ForAll("tableValues") Money lower,
            @ForAll("tableValues") Money higher,
            @ForAll("factors") AdjustmentFactor factor) {
        Assume.that(!higher.isLessThan(lower));
        var fromLower = ReferenceValue.of(lower, factor).amount();
        var fromHigher = ReferenceValue.of(higher, factor).amount();
        assertThat(fromHigher.isLessThan(fromLower)).isFalse();
    }

    @Property
    void staysInsideTheBandOfTheGeneralConditions(
            @ForAll("tableValues") Money table, @ForAll("factors") AdjustmentFactor factor) {
        var reference = ReferenceValue.of(table, factor).amount();
        assertThat(reference.isLessThan(table.times(new BigDecimal("0.80")))).isFalse();
        assertThat(table.times(new BigDecimal("1.20")).isLessThan(reference)).isFalse();
    }

    /** From zero to five million reais, always with cents: the Aurora table goes up to luxury cars. */
    @Provide
    Arbitrary<Money> tableValues() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.ZERO, new BigDecimal("5000000.00"))
                .ofScale(2)
                .map(Money::new);
    }

    /** Every factor the general conditions allow: 0.80 to 1.20, in whole points. */
    @Provide
    Arbitrary<AdjustmentFactor> factors() {
        return Arbitraries.integers()
                .between(80, 120)
                .map(points -> new AdjustmentFactor(BigDecimal.valueOf(points, 2)));
    }
}
