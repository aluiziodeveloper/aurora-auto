package br.com.aurora.policy.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** The coverages of a policy: a first-class collection, with the rules of the collection. */
public final class Coverages {

    private final List<Coverage> items;

    private Coverages(List<Coverage> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("A policy needs at least one coverage");
        }
        for (var type : CoverageType.values()) {
            if (items.stream().filter(c -> c.type() == type).count() > 1) {
                throw new IllegalArgumentException("Coverage " + type + " appears more than once");
            }
        }
        // A copy, and an unmodifiable one: whoever built the list cannot change the policy behind its back.
        this.items = List.copyOf(items);
    }

    public static Coverages of(List<Coverage> items) {
        return new Coverages(items);
    }

    public Optional<Coverage> find(CoverageType type) {
        return items.stream().filter(c -> c.type() == type).findFirst();
    }

    @Override
    public String toString() {
        return "Coverages" + items.stream().map(Coverage::type).toList();
    }

    public Coverages with(Coverage coverage) {
        var next = new ArrayList<>(items);
        next.add(coverage);
        return new Coverages(next);
    }
}
