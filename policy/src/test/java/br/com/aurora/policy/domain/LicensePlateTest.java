package br.com.aurora.policy.domain;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LicensePlateTest {

    private static final byte[] KEY = "k".repeat(32).getBytes(UTF_8);
    private static final byte[] OTHER_KEY = "x".repeat(32).getBytes(UTF_8);

    @Test
    void acceptsAMercosulPlateInAnyCaseWithOrWithoutTheDash() {
        assertThat(LicensePlate.parse("bra-2e19").value()).isEqualTo("BRA2E19");
    }

    @Test
    void convertsAnOldPlateToTheMercosulFormat() {
        assertThat(LicensePlate.parse("ABC-1234")).isEqualTo(LicensePlate.parse("ABC1C34"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "BR2E19", "BRA2E1", "BRA2EE9", "1RA2E19", "BRA2E19X"})
    void rejectsAnythingElse(String text) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> LicensePlate.parse(text))
                .withMessage("License plate must look like BRA2E19 or ABC1234");
    }

    @Test
    void neverShowsThePlateInToString() {
        assertThat(LicensePlate.parse("BRA2E19").toString()).isEqualTo("LicensePlate[*******]");
    }

    @Test
    void givesTheSamePseudonymToTheSameVehicleUnderTheSameKey() {
        assertThat(LicensePlate.parse("ABC-1234").pseudonymWith(KEY))
                .isEqualTo(LicensePlate.parse("ABC1C34").pseudonymWith(KEY));
    }

    @Test
    void givesAnotherPseudonymUnderAnotherKey() {
        var plate = LicensePlate.parse("BRA2E19");
        assertThat(plate.pseudonymWith(KEY)).isNotEqualTo(plate.pseudonymWith(OTHER_KEY));
    }

    @Test
    void pseudonymNeverCarriesThePlate() {
        assertThat(LicensePlate.parse("BRA2E19").pseudonymWith(KEY))
                .matches("plate-[0-9a-f]{12}")
                .doesNotContain("BRA", "2E19");
    }

    @Test
    void rejectsKeysShorterThan32Bytes() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> LicensePlate.parse("BRA2E19").pseudonymWith(new byte[31]))
                .withMessage("Pseudonym key must have at least 32 bytes");
    }
}
