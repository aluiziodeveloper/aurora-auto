package br.com.aurora.labs.modelchoice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class ArgumentsMatcherTest {

    private final JsonMapper json = JsonMapper.builder().build();
    private final ArgumentsMatcher matcher = new ArgumentsMatcher(json);

    private JsonNode expected(String expectedJson) {
        return json.readTree(expectedJson);
    }

    @Test
    void comparesTextWithoutAccentsCaseOrPunctuation() {
        assertThat(matcher.matches(expected("{\"licensePlate\": \"BRA2E19\"}"), "{\"licensePlate\": \"bra-2e19\"}"))
                .isTrue();
    }

    @Test
    void comparesNumbersByValue() {
        assertThat(matcher.matches(expected("{\"repairCost\": 1800}"), "{\"repairCost\": \"1800.00\"}"))
                .isTrue();
    }

    @Test
    void acceptsTextThatContainsTheExpectedPartWhenMarkedWithTilde() {
        assertThat(matcher.matches(
                        expected("{\"pickupAddress\": \"~paulista\"}"),
                        "{\"pickupAddress\": \"Av. Paulista, 1000, São Paulo\"}"))
                .isTrue();
    }

    @Test
    void ignoresArgumentsThatTheCaseDoesNotExpect() {
        assertThat(matcher.matches(
                        expected("{\"policyNumber\": \"AUR-2026-000123\"}"),
                        "{\"policyNumber\": \"AUR-2026-000123\", \"location\": \"Campinas\"}"))
                .isTrue();
    }

    @Test
    void rejectsAWrongValue() {
        assertThat(matcher.matches(expected("{\"eventDate\": \"2026-11-24\"}"), "{\"eventDate\": \"2026-11-25\"}"))
                .isFalse();
    }

    @Test
    void rejectsAMissingArgument() {
        assertThat(matcher.matches(expected("{\"coverage\": \"GLASS\"}"), "{\"policyNumber\": \"AUR-2026-000123\"}"))
                .isFalse();
    }

    @Test
    void rejectsArgumentsThatAreNotJson() {
        assertThat(matcher.matches(expected("{\"coverage\": \"GLASS\"}"), "GLASS"))
                .isFalse();
    }
}
