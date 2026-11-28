package br.com.aurora.policy.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import br.com.aurora.shared.domain.Money;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class PolicyTest {

    private static final PolicyNumber NUMBER = PolicyNumber.parse("AUR-2026-000123");
    private static final LicensePlate PLATE = LicensePlate.parse("BRA2E19");
    private static final CoveragePeriod YEAR =
            new CoveragePeriod(LocalDate.parse("2026-01-10"), LocalDate.parse("2027-01-10"));
    private static final AdjustmentFactor FULL_TABLE = AdjustmentFactor.of("1.00");
    private static final Coverage GLASS = new Coverage(CoverageType.GLASS, new Deductible(Money.of("2500.00")));
    private static final Coverage COLLISION = new Coverage(CoverageType.COLLISION, new Deductible(Money.of("2500.00")));

    private static final Coverage THEFT = new Coverage(CoverageType.THEFT, new Deductible(Money.of("4000.00")));

    private static Policy policyWith(Coverage... coverages) {
        return new Policy(NUMBER, PLATE, YEAR, FULL_TABLE, Coverages.of(List.of(coverages)));
    }

    @Test
    void isTheSamePolicyWhenTheNumberIsTheSame() {
        assertThat(policyWith(GLASS)).isEqualTo(policyWith(GLASS, COLLISION));
        assertThat(policyWith(GLASS)).hasSameHashCodeAs(policyWith(GLASS, COLLISION));
    }

    @Test
    void answersTheCoverageOnADateInsideThePeriod() {
        assertThat(policyWith(GLASS).coverageFor(CoverageType.GLASS, LocalDate.parse("2026-06-15")))
                .contains(GLASS);
    }

    @Test
    void answersNothingOnADateOutsideThePeriod() {
        assertThat(policyWith(GLASS).coverageFor(CoverageType.GLASS, LocalDate.parse("2027-01-11")))
                .isEmpty();
    }

    @Test
    void answersNothingForACoverageItDoesNotHave() {
        assertThat(policyWith(GLASS).coverageFor(CoverageType.THEFT, LocalDate.parse("2026-06-15")))
                .isEmpty();
    }

    @Test
    void insuresOnlyItsOwnVehicle() {
        assertThat(policyWith(GLASS).insures(LicensePlate.parse("BRA2E19"))).isTrue();
        assertThat(policyWith(GLASS).insures(LicensePlate.parse("ABC1D23"))).isFalse();
    }

    @Test
    void anEndorsementAddsACoverageToTheSamePolicy() {
        var policy = policyWith(GLASS);
        policy.endorse(THEFT, LocalDate.parse("2026-03-01"));
        assertThat(policy.coverageFor(CoverageType.THEFT, LocalDate.parse("2026-06-15")))
                .contains(THEFT);
    }

    @Test
    void rejectsAnEndorsementOutsideThePeriod() {
        var policy = policyWith(GLASS);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> policy.endorse(THEFT, LocalDate.parse("2027-02-01")))
                .withMessage("A policy can only be endorsed inside its coverage period");
        assertThat(policy.coverageFor(CoverageType.THEFT, LocalDate.parse("2026-06-15")))
                .isEmpty();
    }

    @Test
    void rejectsAnEndorsementWithACoverageThePolicyAlreadyHas() {
        var policy = policyWith(GLASS);
        var cheaperGlass = new Coverage(CoverageType.GLASS, new Deductible(Money.of("800.00")));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> policy.endorse(cheaperGlass, LocalDate.parse("2026-03-01")))
                .withMessage("Coverage GLASS appears more than once");
        assertThat(policy.coverageFor(CoverageType.GLASS, LocalDate.parse("2026-06-15")))
                .contains(GLASS);
    }
}
