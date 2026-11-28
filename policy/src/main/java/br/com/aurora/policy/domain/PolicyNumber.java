package br.com.aurora.policy.domain;

import java.util.Locale;

/** The policy number: AUR, the year it was issued and a six-digit sequence, such as AUR-2026-000123. */
public record PolicyNumber(String value) {

    public PolicyNumber {
        if (!value.matches("AUR-\\d{4}-\\d{6}")) {
            throw new IllegalArgumentException("Policy number must look like AUR-2026-000123");
        }
    }

    /** Accepts the number in any case, with blanks around it, as a person would type it. */
    public static PolicyNumber parse(String text) {
        return new PolicyNumber(text.strip().toUpperCase(Locale.ROOT));
    }
}
