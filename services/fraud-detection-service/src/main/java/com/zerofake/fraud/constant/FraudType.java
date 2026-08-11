package com.zerofake.fraud.constant;

/**
 * The fraud rules evaluated during a verification.
 *
 * <p>Each rule carries the risk it contributes and its severity. When several
 * rules fire, the one with the lowest {@code severity} value is the headline
 * finding reported to the user and recorded on the fraud report — a product
 * missing from the catalogue is a more fundamental problem than one whose batch
 * has expired, and should be described as such.
 *
 * <p>Declaration order matches the platform's agreed rule priority.
 */
public enum FraudType {

    /** No record of the product exists in the catalogue at all. */
    PRODUCT_NOT_FOUND(1, 100),

    /** The product exists in the catalogue but has no identity on the ledger. */
    BLOCKCHAIN_MISMATCH(2, 100),

    /** A supply chain participant scanned goods they do not hold custody of. */
    INVALID_OWNER(3, 40),

    /** The same item has been scanned in more than one place. */
    MULTIPLE_LOCATION_SCAN(4, 35),

    /** The same QR code has been scanned by more than one party. */
    DUPLICATE_QR(5, 30),

    /** An unusual burst of scans in a short window. */
    SUSPICIOUS_ACTIVITY(6, 15),

    /** Every manufacturing batch for this product is past its expiry date. */
    EXPIRED_PRODUCT(7, 25);

    private final int severity;
    private final int riskWeight;

    FraudType(int severity, int riskWeight) {
        this.severity = severity;
        this.riskWeight = riskWeight;
    }

    /** Lower is more severe. */
    public int getSeverity() {
        return severity;
    }

    /** Points this rule contributes to the risk score. */
    public int getRiskWeight() {
        return riskWeight;
    }
}
