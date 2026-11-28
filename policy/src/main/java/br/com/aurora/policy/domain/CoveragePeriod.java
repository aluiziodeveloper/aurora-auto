package br.com.aurora.policy.domain;

import java.time.LocalDate;

/** The days a policy is in force, the first and the last included (RN01). */
public record CoveragePeriod(LocalDate start, LocalDate end) {

    public CoveragePeriod {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Coverage period cannot end before it starts");
        }
    }

    /** RN01 asks about the date of the event, not the date the claim was reported. */
    public boolean covers(LocalDate eventDate) {
        return !eventDate.isBefore(start) && !eventDate.isAfter(end);
    }
}
