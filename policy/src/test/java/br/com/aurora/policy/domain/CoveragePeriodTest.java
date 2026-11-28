package br.com.aurora.policy.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CoveragePeriodTest {

    private static final CoveragePeriod ONE_YEAR =
            new CoveragePeriod(LocalDate.parse("2026-01-10"), LocalDate.parse("2027-01-10"));

    @ParameterizedTest
    @CsvSource({"2026-01-09, false", "2026-01-10, true", "2026-07-01, true", "2027-01-10, true", "2027-01-11, false"})
    void coversEveryDayFromTheFirstToTheLastInclusive(LocalDate eventDate, boolean covered) {
        assertThat(ONE_YEAR.covers(eventDate)).isEqualTo(covered);
    }

    @Test
    void aOneDayPeriodCoversThatDay() {
        var day = LocalDate.parse("2026-03-15");
        assertThat(new CoveragePeriod(day, day).covers(day)).isTrue();
    }

    @Test
    void rejectsAPeriodThatEndsBeforeItStarts() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new CoveragePeriod(LocalDate.parse("2026-01-10"), LocalDate.parse("2026-01-09")))
                .withMessage("Coverage period cannot end before it starts");
    }
}
