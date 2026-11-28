package br.com.aurora.policy.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import br.com.aurora.shared.domain.Money;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CoveragesTest {

    private static final Coverage GLASS = new Coverage(CoverageType.GLASS, new Deductible(Money.of("2500.00")));
    private static final Coverage COLLISION = new Coverage(CoverageType.COLLISION, new Deductible(Money.of("2500.00")));
    private static final Coverage THEFT = new Coverage(CoverageType.THEFT, new Deductible(Money.of("4000.00")));

    @Test
    void findsACoverageByItsType() {
        assertThat(Coverages.of(List.of(GLASS, COLLISION)).find(CoverageType.GLASS))
                .contains(GLASS);
    }

    @Test
    void aTypeThePolicyDoesNotHaveIsNotFound() {
        assertThat(Coverages.of(List.of(GLASS, COLLISION)).find(CoverageType.THEFT))
                .isEmpty();
    }

    @Test
    void rejectsTheSameTypeTwice() {
        var otherGlass = new Coverage(CoverageType.GLASS, new Deductible(Money.of("800.00")));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Coverages.of(List.of(GLASS, otherGlass)))
                .withMessage("Coverage GLASS appears more than once");
    }

    @Test
    void rejectsAPolicyWithoutCoverages() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Coverages.of(List.of()))
                .withMessage("A policy needs at least one coverage");
    }

    @Test
    void addingACoverageKeepsTheOriginalUntouched() {
        var original = Coverages.of(List.of(GLASS));
        var endorsed = original.with(THEFT);
        assertThat(endorsed.find(CoverageType.THEFT)).contains(THEFT);
        assertThat(original.find(CoverageType.THEFT)).isEmpty();
    }

    @Test
    void doesNotChangeWhenTheListItCameFromChanges() {
        var source = new ArrayList<>(List.of(GLASS));
        var coverages = Coverages.of(source);
        source.add(THEFT);
        assertThat(coverages.find(CoverageType.THEFT)).isEmpty();
    }
}
