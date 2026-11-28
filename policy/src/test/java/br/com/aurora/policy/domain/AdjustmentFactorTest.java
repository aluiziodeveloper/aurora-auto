package br.com.aurora.policy.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AdjustmentFactorTest {

    @ParameterizedTest
    @ValueSource(strings = {"0.80", "1.00", "1.20"})
    void acceptsTheBandOfTheGeneralConditions(String value) {
        assertThat(AdjustmentFactor.of(value).value()).isEqualTo(new BigDecimal(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.79", "1.21", "0", "105"})
    void rejectsAFactorOutsideTheBand(String value) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> AdjustmentFactor.of(value))
                .withMessage("Adjustment factor must be between 0.80 and 1.20");
    }

    @Test
    void theSameFactorWithFewerDecimalsIsTheSameFactor() {
        assertThat(AdjustmentFactor.of("1.1")).isEqualTo(AdjustmentFactor.of("1.10"));
    }

    @Test
    void rejectsMoreThanTwoDecimals() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> AdjustmentFactor.of("1.055"))
                .withMessage("Adjustment factor must have at most two decimals");
    }
}
