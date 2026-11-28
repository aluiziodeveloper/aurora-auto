package br.com.aurora.claims.legacy;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * Characterization tests: they pin what the legacy service does today, right or wrong. A test that pins a wrong
 * behavior says so in its name, and changes only on purpose, in the lesson that fixes it.
 */
class ClaimServiceCharacterizationTest {

    private final ClaimService service = new ClaimService(new InMemoryClaimDao());

    private long claimOf(String referenceValue, String deductible, String estimate) {
        long id = service.report(
                "AUR-2026-000123",
                "BRA2E19",
                LocalDate.of(2026, 11, 2),
                LocalDate.of(2026, 11, 3),
                new BigDecimal(referenceValue),
                new BigDecimal(deductible));
        service.process(id, "INSPECT", null);
        service.process(id, "ESTIMATE", new BigDecimal(estimate));
        return id;
    }

    @Test
    void aPartialLossPaysTheEstimateMinusTheDeductible() {
        long id = claimOf("58400.00", "2500.00", "8000.00");

        service.process(id, "DECIDE", null);

        assertThat(service.find(id).getStatus()).isEqualTo("REPAIR_AUTHORIZED");
        assertThat(service.find(id).getIndemnity()).isEqualTo(new BigDecimal("5500.00"));
    }

    @Test
    void aTotalLossPaysTheReferenceValueWithoutTheDeductible() {
        long id = claimOf("58400.00", "2500.00", "47300.00");

        service.process(id, "DECIDE", null);

        assertThat(service.find(id).getStatus()).isEqualTo("TOTAL_LOSS");
        assertThat(service.find(id).getIndemnity()).isEqualTo(new BigDecimal("58400.00"));
    }

    @Test
    void inc04_aSecondPaymentIsAcceptedAndPaysAgain() {
        long id = claimOf("58400.00", "2500.00", "47300.00");
        service.process(id, "DECIDE", null);
        service.process(id, "PAY", null);

        service.process(id, "DECIDE", null);
        service.process(id, "PAY", null);

        assertThat(service.totalPaid(id)).isEqualTo(new BigDecimal("116800.00"));
    }
}
