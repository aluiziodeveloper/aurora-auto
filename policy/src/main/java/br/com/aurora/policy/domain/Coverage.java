package br.com.aurora.policy.domain;

/** One coverage of a policy, with its own deductible. */
public record Coverage(CoverageType type, Deductible deductible) {}
