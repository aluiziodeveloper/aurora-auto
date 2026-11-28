package br.com.aurora.policy.domain;

import java.time.LocalDate;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * The policy: the root of the aggregate that holds the vehicle, the period and the coverages. It is an entity, not a
 * value object: two policies with the same number are the same policy, whatever their coverages are today.
 */
public final class Policy {

    private final PolicyNumber number;
    private final LicensePlate vehicle;
    private final CoveragePeriod period;
    private final AdjustmentFactor adjustmentFactor;
    private Coverages coverages;

    public Policy(
            PolicyNumber number,
            LicensePlate vehicle,
            CoveragePeriod period,
            AdjustmentFactor adjustmentFactor,
            Coverages coverages) {
        this.number = number;
        this.vehicle = vehicle;
        this.period = period;
        this.adjustmentFactor = adjustmentFactor;
        this.coverages = coverages;
    }

    public PolicyNumber number() {
        return number;
    }

    public AdjustmentFactor adjustmentFactor() {
        return adjustmentFactor;
    }

    public Optional<Coverage> coverageFor(CoverageType type, LocalDate eventDate) {
        if (!period.covers(eventDate)) {
            return Optional.empty();
        }
        return coverages.find(type);
    }

    public boolean insures(LicensePlate plate) {
        return vehicle.equals(plate);
    }

    /**
     * An endorsement adds a coverage during the period. The root checks the rule that spans the group, the period, and
     * the collection checks its own, the repeated type; if either fails, the policy does not change.
     */
    public void endorse(Coverage coverage, LocalDate effectiveDate) {
        if (!period.covers(effectiveDate)) {
            throw new IllegalArgumentException("A policy can only be endorsed inside its coverage period");
        }
        coverages = coverages.with(coverage);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof Policy policy && number.equals(policy.number);
    }

    @Override
    public int hashCode() {
        return number.hashCode();
    }

    @Override
    public String toString() {
        return "Policy[" + number.value() + "]";
    }
}
