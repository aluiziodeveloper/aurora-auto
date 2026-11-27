package br.com.aurora.labs.modelchoice;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Checks the arguments of a tool call against the ones a case expects. Only the expected
 * arguments count: numbers by value, "~text" by containing it, the rest without accents, case or
 * punctuation.
 */
class ArgumentsMatcher {

    private final JsonMapper json;

    ArgumentsMatcher(JsonMapper json) {
        this.json = json;
    }

    boolean matches(JsonNode expected, String actualJson) {
        JsonNode actual;
        try {
            actual = json.readTree(actualJson);
        } catch (JacksonException e) {
            return false;
        }
        for (Map.Entry<String, JsonNode> argument : expected.properties()) {
            JsonNode got = actual.get(argument.getKey());
            if (got == null || got.isNull() || !same(argument.getValue(), got)) {
                return false;
            }
        }
        return true;
    }

    private static boolean same(JsonNode want, JsonNode got) {
        if (want.isNumber()) {
            try {
                return new BigDecimal(got.asString().replace(",", ".")).compareTo(want.decimalValue()) == 0;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        String text = want.asString();
        if (text.startsWith("~")) {
            return normalize(got.asString()).contains(normalize(text.substring(1)));
        }
        return normalize(got.asString()).equals(normalize(text));
    }

    private static String normalize(String text) {
        String withoutAccents = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
