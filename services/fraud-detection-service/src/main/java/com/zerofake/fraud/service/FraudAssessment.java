package com.zerofake.fraud.service;

import com.zerofake.fraud.constant.FraudType;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The outcome of evaluating the fraud rules against a single scan.
 *
 * @param riskScore      the accumulated risk, capped at 100
 * @param triggeredRules every rule that fired, most severe first
 */
public record FraudAssessment(int riskScore, Set<FraudType> triggeredRules) {

    private static final int MAX_RISK_SCORE = 100;

    public static FraudAssessment of(Set<FraudType> triggeredRules) {

        int riskScore = triggeredRules.stream()
                .mapToInt(FraudType::getRiskWeight)
                .sum();

        return new FraudAssessment(
                Math.min(riskScore, MAX_RISK_SCORE),
                triggeredRules.isEmpty() ? EnumSet.noneOf(FraudType.class) : EnumSet.copyOf(triggeredRules)
        );
    }

    /** The most severe rule that fired, which becomes the headline finding. */
    public Optional<FraudType> headlineFinding() {
        return triggeredRules.stream()
                .min(Comparator.comparingInt(FraudType::getSeverity));
    }

    /** Rule names ordered by severity, for display and for the verification log. */
    public List<String> triggeredRuleNames() {
        return triggeredRules.stream()
                .sorted(Comparator.comparingInt(FraudType::getSeverity))
                .map(Enum::name)
                .toList();
    }
}
