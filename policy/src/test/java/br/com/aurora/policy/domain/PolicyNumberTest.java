package br.com.aurora.policy.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PolicyNumberTest {

    @Test
    void acceptsTheAuroraFormatInAnyCase() {
        assertThat(PolicyNumber.parse(" aur-2026-000123 ").value()).isEqualTo("AUR-2026-000123");
    }

    @ParameterizedTest
    @ValueSource(
            strings = {"", "AUR-26-000123", "AUR-2026-123", "XYZ-2026-000123", "AUR2026000123", "AUR-2026-0001234"})
    void rejectsAnythingElse(String text) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> PolicyNumber.parse(text))
                .withMessage("Policy number must look like AUR-2026-000123");
    }
}
