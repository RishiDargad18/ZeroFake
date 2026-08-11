package com.zerofake.product.constant;

/**
 * Tracks whether a product's identity has been anchored on the blockchain.
 *
 * <p>A product is created as {@link #PENDING} and is promoted to
 * {@link #REGISTERED} by the blockchain service once the on-chain registration
 * transaction has been committed.
 */
public enum BlockchainStatus {

    PENDING,

    REGISTERED,

    FAILED

}
