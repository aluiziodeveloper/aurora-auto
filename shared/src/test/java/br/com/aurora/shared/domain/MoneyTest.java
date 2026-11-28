package br.com.aurora.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MoneyTest {

    @Test
    void isTheSameAmountWhateverTheScaleItWasWrittenWith() {
        assertThat(Money.of("10")).isEqualTo(Money.of("10.00"));
    }

    @Test
    void keepsTwoDecimalPlaces() {
        assertThat(Money.of("12300.5").amount()).hasToString("12300.50");
    }

    @Test
    void rejectsNegativeAmounts() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Money.of("-0.01"))
                .withMessage("Money cannot be negative");
    }

    @ParameterizedTest
    @CsvSource({"0.125, 0.12", "0.135, 0.14", "0.005, 0.00", "0.015, 0.02", "0.0149, 0.01"})
    void roundsHalfEvenToTheCent(String written, String expected) {
        assertThat(Money.of(written).amount()).hasToString(expected);
    }

    @Test
    void addsAndSubtracts() {
        var repair = Money.of("1800.00");
        assertThat(repair.plus(Money.of("200.00"))).isEqualTo(Money.of("2000.00"));
        assertThat(repair.minus(Money.of("300.00"))).isEqualTo(Money.of("1500.00"));
    }

    @Test
    void refusesASubtractionThatWouldGoBelowZero() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Money.of("1800.00").minus(Money.of("2200.00")))
                .withMessage("Money cannot be negative");
    }

    @Test
    void multipliesByAFactorAndRoundsTheResult() {
        assertThat(Money.of("10.25").times(new BigDecimal("0.5"))).isEqualTo(Money.of("5.12"));
    }

    @Test
    void comparesAmounts() {
        assertThat(Money.of("1800.00").isLessThan(Money.of("2200.00"))).isTrue();
        assertThat(Money.of("2200.00").isLessThan(Money.of("2200.00"))).isFalse();
        assertThat(Money.ZERO).isEqualTo(Money.of("0"));
    }
}
