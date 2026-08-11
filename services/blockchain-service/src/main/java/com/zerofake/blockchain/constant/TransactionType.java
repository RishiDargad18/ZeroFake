package com.zerofake.blockchain.constant;

/**
 * The kinds of transaction ZeroFake writes to the ledger.
 *
 * <p>Only operations that actually commit a transaction to Hyperledger Fabric
 * appear here. Product verification is a ledger <em>query</em> — it produces no
 * transaction and is therefore recorded by the fraud detection service as a
 * verification log, not as a blockchain transaction.
 */
public enum TransactionType {

    PRODUCT_REGISTERED,

    OWNERSHIP_TRANSFERRED

}
