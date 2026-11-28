package br.com.aurora.customers.domain;

/** The Brazilian individual taxpayer number: eleven digits, the last two are check digits. */
public record Cpf(String digits) {

    public Cpf {
        if (!digits.matches("\\d{11}")) {
            throw new IllegalArgumentException("CPF must have 11 digits");
        }
        if (digits.chars().distinct().count() == 1) {
            throw new IllegalArgumentException("CPF cannot repeat a single digit");
        }
        if (checkDigit(digits, 9) != digits.charAt(9) - '0' || checkDigit(digits, 10) != digits.charAt(10) - '0') {
            throw new IllegalArgumentException("CPF check digits do not match");
        }
    }

    /** Accepts the CPF with or without the dots and the dash: 123.456.789-09 or 12345678909. */
    public static Cpf parse(String text) {
        return new Cpf(text.replace(".", "").replace("-", ""));
    }

    /** RN04: only the middle six digits stay visible. */
    public String masked() {
        return "***." + digits.substring(3, 6) + "." + digits.substring(6, 9) + "-**";
    }

    /** The record's default would print the digits: logs and messages only ever see the mask. */
    @Override
    public String toString() {
        return "Cpf[" + masked() + "]";
    }

    private static int checkDigit(String digits, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += (digits.charAt(i) - '0') * (length + 1 - i);
        }
        int remainder = sum * 10 % 11;
        return remainder == 10 ? 0 : remainder;
    }
}
