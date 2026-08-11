package com.zerofake.fraud.service;

import com.zerofake.fraud.constant.FraudType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The scoring and prioritisation rules of the fraud engine.
 *
 * <p>These are the numbers a verification verdict is built on, so they are
 * asserted explicitly rather than left to be inferred from the enum.
 */
class FraudAssessmentTest {

    @Nested
    @DisplayName("risk scoring")
    class RiskScoring {

        @Test
        @DisplayName("a clean scan scores zero")
        void cleanScanScoresZero() {

            FraudAssessment assessment =
                    FraudAssessment.of(EnumSet.noneOf(FraudType.class));

            assertThat(assessment.riskScore()).isZero();
            assertThat(assessment.triggeredRules()).isEmpty();
            assertThat(assessment.headlineFinding()).isEmpty();
        }

        @Test
        @DisplayName("a single rule contributes exactly its own weight")
        void singleRuleContributesItsWeight() {

            assertThat(FraudAssessment.of(EnumSet.of(FraudType.DUPLICATE_QR)).riskScore())
                    .isEqualTo(30);

            assertThat(FraudAssessment.of(EnumSet.of(FraudType.MULTIPLE_LOCATION_SCAN)).riskScore())
                    .isEqualTo(35);

            assertThat(FraudAssessment.of(EnumSet.of(FraudType.INVALID_OWNER)).riskScore())
                    .isEqualTo(40);

            assertThat(FraudAssessment.of(EnumSet.of(FraudType.EXPIRED_PRODUCT)).riskScore())
                    .isEqualTo(25);

            assertThat(FraudAssessment.of(EnumSet.of(FraudType.SUSPICIOUS_ACTIVITY)).riskScore())
                    .isEqualTo(15);
        }

        @Test
        @DisplayName("multiple rules accumulate")
        void multipleRulesAccumulate() {

            // The regression this guards: MULTIPLE_LOCATION_SCAN was previously
            // reported as triggered but never added to the score.
            FraudAssessment assessment = FraudAssessment.of(
                    EnumSet.of(FraudType.DUPLICATE_QR, FraudType.MULTIPLE_LOCATION_SCAN)
            );

            assertThat(assessment.riskScore()).isEqualTo(65);
        }

        @Test
        @DisplayName("the score is capped at 100")
        void scoreIsCappedAtOneHundred() {

            FraudAssessment assessment = FraudAssessment.of(
                    EnumSet.of(
                            FraudType.INVALID_OWNER,
                            FraudType.MULTIPLE_LOCATION_SCAN,
                            FraudType.DUPLICATE_QR,
                            FraudType.SUSPICIOUS_ACTIVITY,
                            FraudType.EXPIRED_PRODUCT
                    )
            );

            // 40 + 35 + 30 + 15 + 25 = 145, capped.
            assertThat(assessment.riskScore()).isEqualTo(100);
        }

        @Test
        @DisplayName("a fatal rule alone reaches the counterfeit threshold")
        void fatalRuleReachesCounterfeitThreshold() {

            assertThat(FraudAssessment.of(EnumSet.of(FraudType.PRODUCT_NOT_FOUND)).riskScore())
                    .isEqualTo(100);

            assertThat(FraudAssessment.of(EnumSet.of(FraudType.BLOCKCHAIN_MISMATCH)).riskScore())
                    .isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("headline finding")
    class HeadlineFinding {

        @Test
        @DisplayName("the most severe rule wins")
        void mostSevereRuleWins() {

            FraudAssessment assessment = FraudAssessment.of(
                    EnumSet.of(
                            FraudType.EXPIRED_PRODUCT,
                            FraudType.BLOCKCHAIN_MISMATCH,
                            FraudType.DUPLICATE_QR
                    )
            );

            assertThat(assessment.headlineFinding())
                    .contains(FraudType.BLOCKCHAIN_MISMATCH);
        }

        @Test
        @DisplayName("a missing product outranks everything else")
        void missingProductOutranksEverything() {

            FraudAssessment assessment = FraudAssessment.of(EnumSet.allOf(FraudType.class));

            assertThat(assessment.headlineFinding())
                    .contains(FraudType.PRODUCT_NOT_FOUND);
        }

        @Test
        @DisplayName("a location scan outranks a duplicate scan")
        void locationScanOutranksDuplicateScan() {

            // Two parties scanning in two places is a stronger signal than the
            // duplicate alone, so it must be the finding that is reported.
            FraudAssessment assessment = FraudAssessment.of(
                    EnumSet.of(FraudType.DUPLICATE_QR, FraudType.MULTIPLE_LOCATION_SCAN)
            );

            assertThat(assessment.headlineFinding())
                    .contains(FraudType.MULTIPLE_LOCATION_SCAN);
        }
    }

    @Nested
    @DisplayName("rule names")
    class RuleNames {

        @Test
        @DisplayName("are ordered by severity, not by declaration or hash order")
        void areOrderedBySeverity() {

            FraudAssessment assessment = FraudAssessment.of(
                    EnumSet.of(
                            FraudType.EXPIRED_PRODUCT,
                            FraudType.DUPLICATE_QR,
                            FraudType.INVALID_OWNER
                    )
            );

            assertThat(assessment.triggeredRuleNames()).containsExactly(
                    "INVALID_OWNER",
                    "DUPLICATE_QR",
                    "EXPIRED_PRODUCT"
            );
        }

        @Test
        @DisplayName("are empty for a clean scan")
        void areEmptyForCleanScan() {

            assertThat(FraudAssessment.of(EnumSet.noneOf(FraudType.class)).triggeredRuleNames())
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("severity ordering")
    class SeverityOrdering {

        @Test
        @DisplayName("matches the platform's agreed rule priority")
        void matchesAgreedPriority() {

            List<FraudType> bySeverity = EnumSet.allOf(FraudType.class).stream()
                    .sorted(java.util.Comparator.comparingInt(FraudType::getSeverity))
                    .toList();

            assertThat(bySeverity).containsExactly(
                    FraudType.PRODUCT_NOT_FOUND,
                    FraudType.BLOCKCHAIN_MISMATCH,
                    FraudType.INVALID_OWNER,
                    FraudType.MULTIPLE_LOCATION_SCAN,
                    FraudType.DUPLICATE_QR,
                    FraudType.SUSPICIOUS_ACTIVITY,
                    FraudType.EXPIRED_PRODUCT
            );
        }

        @Test
        @DisplayName("assigns every rule a distinct severity")
        void assignsDistinctSeverities() {

            long distinct = EnumSet.allOf(FraudType.class).stream()
                    .map(FraudType::getSeverity)
                    .distinct()
                    .count();

            assertThat(distinct).isEqualTo(FraudType.values().length);
        }
    }

    @Test
    @DisplayName("the triggered rule set is defensively copied")
    void triggeredRuleSetIsDefensivelyCopied() {

        EnumSet<FraudType> source = EnumSet.of(FraudType.DUPLICATE_QR);

        FraudAssessment assessment = FraudAssessment.of(source);

        source.add(FraudType.PRODUCT_NOT_FOUND);

        assertThat(assessment.triggeredRules()).containsExactly(FraudType.DUPLICATE_QR);
        assertThat(assessment.headlineFinding()).contains(FraudType.DUPLICATE_QR);

        Optional<FraudType> unchanged = assessment.headlineFinding();
        assertThat(unchanged).isNotEqualTo(Optional.of(FraudType.PRODUCT_NOT_FOUND));
    }
}
