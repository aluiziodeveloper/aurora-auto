package br.com.aurora.customers.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CpfTest {

    @Test
    void acceptsTheSameCpfWithOrWithoutPunctuation() {
        assertThat(Cpf.parse("123.456.789-09")).isEqualTo(Cpf.parse("12345678909"));
    }

    @Test
    void rejectsWrongCheckDigits() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Cpf.parse("123.456.789-00"))
                .withMessage("CPF check digits do not match");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "123.456.789", "123.456.789-0", "123.456.789-091", "123.456.78a-09"})
    void rejectsAnythingButElevenDigits(String text) {
        assertThatIllegalArgumentException().isThrownBy(() -> Cpf.parse(text)).withMessage("CPF must have 11 digits");
    }

    @Test
    void rejectsRepeatedDigitsEvenWithMatchingCheckDigits() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Cpf.parse("111.111.111-11"))
                .withMessage("CPF cannot repeat a single digit");
    }

    @Test
    void masksAllButTheMiddleDigits() {
        assertThat(Cpf.parse("123.456.789-09").masked()).isEqualTo("***.456.789-**");
    }

    @Test
    void neverShowsTheDigitsInToString() {
        assertThat(Cpf.parse("123.456.789-09").toString()).isEqualTo("Cpf[***.456.789-**]");
    }
}
