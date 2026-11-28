package br.com.aurora.policy.domain;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** A Brazilian license plate, always kept in the Mercosul format: three letters, digit, letter, two digits. */
public record LicensePlate(String value) {

    public LicensePlate {
        if (!value.matches("[A-Z]{3}[0-9][A-Z][0-9]{2}")) {
            throw new IllegalArgumentException("License plate must look like BRA2E19 or ABC1234");
        }
    }

    /**
     * Accepts the plate in any case, with or without the dash. An old plate (ABC1234) becomes Mercosul: its second digit
     * turns into a letter, 0 into A up to 9 into J (ABC1C34).
     */
    public static LicensePlate parse(String text) {
        var plate = text.replace("-", "").toUpperCase(Locale.ROOT);
        if (plate.matches("[A-Z]{3}[0-9]{4}")) {
            plate = plate.substring(0, 4) + (char) ('A' + plate.charAt(4) - '0') + plate.substring(5);
        }
        return new LicensePlate(plate);
    }

    /**
     * RN04: the plate is pseudonymized before it reaches the model. The same plate and key always give the same
     * pseudonym, so a conversation can refer to the vehicle; without the key, the pseudonym does not lead back to it.
     */
    public String pseudonymWith(byte[] key) {
        if (key.length < 32) {
            throw new IllegalArgumentException("Pseudonym key must have at least 32 bytes");
        }
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            var digest = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return "plate-" + HexFormat.of().formatHex(digest, 0, 6);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 is part of every JDK", e);
        }
    }

    /** The plate identifies the vehicle and, through it, a person: logs never see it. */
    @Override
    public String toString() {
        return "LicensePlate[*******]";
    }
}
